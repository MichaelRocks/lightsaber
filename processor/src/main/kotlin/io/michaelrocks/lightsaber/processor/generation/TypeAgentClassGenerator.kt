/*
 * Copyright 2016 Michael Rozumyanskiy
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
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.*
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.MembersInjector
import io.michaelrocks.lightsaber.processor.model.InjectionPoint
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.util.*

class TypeAgentClassGenerator(
    private val classRegistry: ClassRegistry,
    private val annotationCreator: AnnotationCreator,
    private val injector: MembersInjector
) {
  companion object {
    private const val KEY_FIELD_NAME_PREFIX = "key"

    private val KEY_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE)
    private val INJECT_FIELDS_METHOD =
        MethodDescriptor.forMethod("injectFields", Type.VOID_TYPE, Types.INJECTOR_TYPE, Types.OBJECT_TYPE)
    private val INJECT_METHODS_METHOD =
        MethodDescriptor.forMethod("injectMethods", Type.VOID_TYPE, Types.INJECTOR_TYPE, Types.OBJECT_TYPE)
    private val GET_PROVIDER_METHOD = MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.KEY_TYPE)
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

    generateKeyFields(classVisitor)
    generateStaticInitializer(classVisitor)
    generateConstructor(classVisitor)
    generateInjectFieldsMethod(classVisitor)
    generateInjectMethodsMethod(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateKeyFields(classVisitor: ClassVisitor) {
    for (i in 0..fields.size - 1) {
      val fieldVisitor = classVisitor.visitField(
          ACC_PRIVATE or ACC_STATIC or ACC_FINAL,
          KEY_FIELD_NAME_PREFIX + i,
          Types.KEY_TYPE.descriptor,
          null,
          null)
      fieldVisitor.visitEnd()
    }

    methods.forEachIndexed { index, method ->
      val keyFieldNamePrefix = KEY_FIELD_NAME_PREFIX + index + '_'
      method.injectees.forEachIndexed { index, injectee ->
        val fieldVisitor = classVisitor.visitField(
            ACC_PRIVATE or ACC_STATIC or ACC_FINAL,
            keyFieldNamePrefix + index,
            Types.KEY_TYPE.descriptor,
            null,
            null)
        fieldVisitor.visitEnd()
      }
    }
  }

  private fun generateStaticInitializer(classVisitor: ClassVisitor) {
    val staticInitializer = MethodDescriptor.forStaticInitializer()
    val generator = GeneratorAdapter(classVisitor, ACC_STATIC, staticInitializer)
    generator.visitCode()

    initializeFieldKeys(generator)
    initializeMethodKeys(generator)

    generator.returnValue()
    generator.endMethod()
  }

  private fun initializeFieldKeys(generator: GeneratorAdapter) {
    fields.forEachIndexed { index, field ->
      generator.newInstance(Types.KEY_TYPE)
      generator.dup()
      generator.push(field.injectee.dependency.type.rawType.box())
      if (field.injectee.dependency.qualifier == null) {
        generator.pushNull()
      } else {
        annotationCreator.newAnnotation(generator, field.injectee.dependency.qualifier)
      }
      generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR)
      generator.putStatic(injector.type, KEY_FIELD_NAME_PREFIX + index, Types.KEY_TYPE)
    }
  }

  private fun initializeMethodKeys(generator: GeneratorAdapter) {
    methods.forEachIndexed { index, method ->
      val keyFieldNamePrefix = KEY_FIELD_NAME_PREFIX + index + '_'
      method.injectees.forEachIndexed { index, injectee ->
        generator.newInstance(Types.KEY_TYPE)
        generator.dup()
        generator.push(injectee.dependency.type.rawType.box())
        if (injectee.dependency.qualifier == null) {
          generator.pushNull()
        } else {
          annotationCreator.newAnnotation(generator, injectee.dependency.qualifier)
        }
        generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR)
        generator.putStatic(injector.type, keyFieldNamePrefix + index, Types.KEY_TYPE)
      }
    }
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

      fields.forEachIndexed { index, field ->
        generateFieldInitializer(generator, injectableTargetLocal, field, index)
      }
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateFieldInitializer(generator: GeneratorAdapter, injectableTargetLocal: Int,
      field: InjectionPoint.Field, fieldIndex: Int) {
    generator.loadLocal(injectableTargetLocal)
    generator.loadArg(0)

    generator.getStatic(injector.type, KEY_FIELD_NAME_PREFIX + fieldIndex, Types.KEY_TYPE)

    generator.invokeInterface(Types.INJECTOR_TYPE, GET_PROVIDER_METHOD)
    GenerationHelper.convertDependencyToTargetType(generator, field.injectee)
    generator.putField(injector.target.type, field.field.toFieldDescriptor())
  }

  private fun generateInjectMethodsMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, INJECT_METHODS_METHOD)
    generator.visitCode()

    generator.loadArg(1)
    generator.checkCast(injector.target.type)
    val injectableTargetLocal = generator.newLocal(injector.target.type)
    generator.storeLocal(injectableTargetLocal)

    methods.forEachIndexed { index, method ->
      generateMethodInvocation(generator, injectableTargetLocal, method, index)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateMethodInvocation(generator: GeneratorAdapter, injectableTargetLocal: Int,
      method: InjectionPoint.Method, methodIndex: Int) {
    generator.loadLocal(injectableTargetLocal)

    method.injectees.forEachIndexed { index, injectee ->
      generator.loadArg(0)
      generator.getStatic(
          injector.type, KEY_FIELD_NAME_PREFIX + methodIndex + '_' + index, Types.KEY_TYPE)
      generator.invokeInterface(Types.INJECTOR_TYPE, GET_PROVIDER_METHOD)
      GenerationHelper.convertDependencyToTargetType(generator, injectee)
    }
    generator.invokeVirtual(injector.target.type, method.method.toMethodDescriptor())
  }
}
