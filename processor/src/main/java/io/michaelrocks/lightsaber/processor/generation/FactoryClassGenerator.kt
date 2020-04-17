/*
 * Copyright 2020 Michael Rozumyanskiy
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
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.newMethod
import io.michaelrocks.lightsaber.processor.commons.toMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.model.Factory
import io.michaelrocks.lightsaber.processor.model.FactoryInjectee
import io.michaelrocks.lightsaber.processor.model.FactoryProvisionPoint
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6

class FactoryClassGenerator(
  private val classRegistry: ClassRegistry,
  private val keyRegistry: KeyRegistry,
  private val injectionContext: InjectionContext,
  private val factory: Factory
) {

  fun generate(): ByteArray {
    val classWriter = StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
      V1_6,
      ACC_PUBLIC or ACC_SUPER,
      factory.implementationType.internalName,
      null,
      Types.OBJECT_TYPE.internalName,
      arrayOf(factory.type.internalName)
    )

    generateFields(classVisitor)
    generateConstructor(classVisitor)
    generateMethods(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateFields(classVisitor: ClassVisitor) {
    generateInjectorField(classVisitor)
  }

  private fun generateInjectorField(classVisitor: ClassVisitor) {
    val fieldVisitor = classVisitor.visitField(
      ACC_PRIVATE or ACC_FINAL,
      INJECTOR_FIELD.name,
      INJECTOR_FIELD.type.descriptor,
      null,
      null
    )
    fieldVisitor.visitEnd()
  }

  private fun generateConstructor(classVisitor: ClassVisitor) {
    classVisitor.newMethod(ACC_PUBLIC, CONSTRUCTOR) {
      loadThis()
      invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forDefaultConstructor())

      loadThis()
      loadArg(0)
      putField(factory.implementationType, INJECTOR_FIELD)
    }
  }

  private fun generateMethods(classVisitor: ClassVisitor) {
    factory.provisionPoints.forEach { provisionPoint ->
      classVisitor.newMethod(ACC_PUBLIC, provisionPoint.method.toMethodDescriptor()) {
        newProvisionPoint(provisionPoint)
      }
    }
  }

  private fun GeneratorAdapter.newProvisionPoint(provisionPoint: FactoryProvisionPoint) {
    val dependencyType = provisionPoint.injectionPoint.containerType
    newInstance(dependencyType)
    dup()
    provisionPoint.injectionPoint.injectees.forEach { loadArgument(it) }
    invokeConstructor(dependencyType, provisionPoint.injectionPoint.method.toMethodDescriptor())
    if (dependencyType != provisionPoint.method.type.returnType) {
      checkCast(provisionPoint.method.type.returnType)
    }

    injectionContext.findInjectableTargetByType(dependencyType)?.also {
      injectMembers()
    }
  }

  private fun GeneratorAdapter.loadArgument(injectee: FactoryInjectee) {
    return when (injectee) {
      is FactoryInjectee.FromInjector -> loadArgumentFromInjector(injectee)
      is FactoryInjectee.FromMethod -> loadArgumentFromMethod(injectee)
    }
  }

  private fun GeneratorAdapter.loadArgumentFromInjector(injectee: FactoryInjectee.FromInjector) {
    loadThis()
    getField(factory.implementationType, INJECTOR_FIELD)
    getDependency(keyRegistry, injectee.injectee)
  }

  private fun GeneratorAdapter.loadArgumentFromMethod(injectee: FactoryInjectee.FromMethod) {
    loadArg(injectee.argumentIndex)
  }

  private fun GeneratorAdapter.injectMembers() {
    dup()
    loadThis()
    getField(factory.implementationType, INJECTOR_FIELD)
    swap()
    invokeInterface(Types.INJECTOR_TYPE, INJECT_MEMBERS_METHOD)
  }

  companion object {
    private val INJECTOR_FIELD = FieldDescriptor("injector", Types.INJECTOR_TYPE)
    private val CONSTRUCTOR = MethodDescriptor.forConstructor(Types.INJECTOR_TYPE)

    private val INJECT_MEMBERS_METHOD =
      MethodDescriptor.forMethod("injectMembers", Type.Primitive.Void, Types.OBJECT_TYPE)
  }
}
