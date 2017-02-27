/*
 * Copyright 2017 Michael Rozumyanskiy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.michaelrocks.lightsaber.processor.generation

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.toFieldDescriptor
import io.michaelrocks.lightsaber.processor.commons.toMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.generation.model.MembersInjector
import io.michaelrocks.lightsaber.processor.model.InjectionPoint
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6
import java.util.ArrayList

class MembersInjectorClassGenerator(
    private val classRegistry: ClassRegistry,
    private val keyRegistry: KeyRegistry,
    private val injector: MembersInjector
) {
  companion object {
    private val INJECT_FIELDS_METHOD =
        MethodDescriptor.forMethod("injectFields", Type.Primitive.Void, Types.INJECTOR_TYPE, Types.OBJECT_TYPE)
    private val INJECT_METHODS_METHOD =
        MethodDescriptor.forMethod("injectMethods", Type.Primitive.Void, Types.INJECTOR_TYPE, Types.OBJECT_TYPE)
  }

  private val fields: Collection<InjectionPoint.Field>
  private val methods: Collection<InjectionPoint.Method>

  init {
    val fields = ArrayList<InjectionPoint.Field>()
    val methods = ArrayList<InjectionPoint.Method>()

    injector.target.injectionPoints.forEach {
      when (it) {
        is InjectionPoint.Field -> fields += it
        is InjectionPoint.Method -> methods += it
      }
    }

    this.fields = fields
    this.methods = methods
  }

  fun generate(): ByteArray {
    val classWriter = StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
        V1_6,
        ACC_PUBLIC or ACC_SUPER,
        injector.type.internalName,
        null,
        Types.OBJECT_TYPE.internalName,
        arrayOf(LightsaberTypes.MEMBERS_INJECTOR_TYPE.internalName))

    generateConstructor(classVisitor)
    generateInjectFieldsMethod(classVisitor)
    generateInjectMethodsMethod(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateConstructor(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, MethodDescriptor.forConstructor())
    generator.visitCode()
    generator.loadThis()
    generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forConstructor())
    generator.returnValue()
    generator.endMethod()
  }

  private fun generateInjectFieldsMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, INJECT_FIELDS_METHOD)
    generator.visitCode()

    if (!fields.isEmpty()) {
      generator.loadArg(1)
      generator.checkCast(injector.target.type)
      val injectableTargetLocal = generator.newLocal(injector.target.type)
      generator.storeLocal(injectableTargetLocal)

      fields.forEach { field ->
        generateFieldInitializer(generator, injectableTargetLocal, field)
      }
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateFieldInitializer(generator: GeneratorAdapter, injectableTargetLocal: Int,
      field: InjectionPoint.Field) {
    generator.loadLocal(injectableTargetLocal)
    generator.loadArg(0)
    generator.getProvider(keyRegistry, field.injectee.dependency)
    generator.convertDependencyToTargetType(field.injectee)
    generator.putField(injector.target.type, field.field.toFieldDescriptor())
  }

  private fun generateInjectMethodsMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, INJECT_METHODS_METHOD)
    generator.visitCode()

    generator.loadArg(1)
    generator.checkCast(injector.target.type)
    val injectableTargetLocal = generator.newLocal(injector.target.type)
    generator.storeLocal(injectableTargetLocal)

    methods.forEach { method ->
      generateMethodInvocation(generator, injectableTargetLocal, method)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateMethodInvocation(generator: GeneratorAdapter, injectableTargetLocal: Int,
      method: InjectionPoint.Method) {
    generator.loadLocal(injectableTargetLocal)

    method.injectees.forEach { injectee ->
      generator.loadArg(0)
      generator.getProvider(keyRegistry, injectee.dependency)
      generator.convertDependencyToTargetType(injectee)
    }
    generator.invokeVirtual(injector.target.type, method.method.toMethodDescriptor())
  }
}
