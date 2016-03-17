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
import io.michaelrocks.lightsaber.MembersInjector
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.*
import io.michaelrocks.lightsaber.processor.descriptors.*
import io.michaelrocks.lightsaber.processor.signature.TypeSignature
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

class TypeAgentClassGenerator(
    private val classRegistry: ClassRegistry,
    private val annotationCreator: AnnotationCreator,
    private val injector: InjectorDescriptor
) {
  companion object {
    private const val KEY_FIELD_NAME_PREFIX = "key"

    private val KEY_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE)
    private val INJECT_FIELDS_METHOD =
        MethodDescriptor.forMethod("injectFields", Type.VOID_TYPE, Types.INJECTOR_TYPE, Types.OBJECT_TYPE)
    private val INJECT_METHODS_METHOD =
        MethodDescriptor.forMethod("injectMethods", Type.VOID_TYPE, Types.INJECTOR_TYPE, Types.OBJECT_TYPE)
    private val GET_INSTANCE_METHOD = MethodDescriptor.forMethod("getInstance", Types.OBJECT_TYPE, Types.KEY_TYPE)
    private val GET_PROVIDER_METHOD = MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.KEY_TYPE)

    private fun getDependencyTypeForType(type: TypeSignature): Type =
        type.parameterType ?: type.rawType.box()

    private fun getInjectorMethodForType(type: TypeSignature): MethodDescriptor =
        if (type.isParameterized) GET_PROVIDER_METHOD else GET_INSTANCE_METHOD
  }

  fun generate(): ByteArray {
    val classWriter = StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
        V1_6,
        ACC_PUBLIC or ACC_SUPER,
        injector.injectorType.internalName,
        null,
        Types.OBJECT_TYPE.internalName,
        arrayOf(MembersInjector::class.internalName))

    generateKeyFields(classVisitor)
    generateStaticInitializer(classVisitor)
    generateConstructor(classVisitor)
    generateInjectFieldsMethod(classVisitor)
    generateInjectMethodsMethod(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateKeyFields(classVisitor: ClassVisitor) {
    val injectableFields = injector.injectableTarget.injectableFields
    for (i in 0 until injectableFields.size) {
      val fieldVisitor = classVisitor.visitField(
          ACC_PRIVATE or ACC_STATIC or ACC_FINAL,
          KEY_FIELD_NAME_PREFIX + i,
          Types.KEY_TYPE.descriptor,
          null,
          null)
      fieldVisitor.visitEnd()
    }

    injector.injectableTarget.injectableMethods.values.forEachIndexed { i, injectableMethod ->
      for (j in 0 until injectableMethod.argumentTypes.size) {
        val fieldVisitor = classVisitor.visitField(
            ACC_PRIVATE or ACC_STATIC or ACC_FINAL,
            KEY_FIELD_NAME_PREFIX + i + '_' + j,
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
    injector.injectableTarget.injectableFields.values.forEachIndexed { i, injectableField ->
      val dependencyType = getDependencyTypeForType(injectableField.signature)

      generator.newInstance(Types.KEY_TYPE)
      generator.dup()
      generator.push(dependencyType)
      if (injectableField.qualifier == null) {
        generator.pushNull()
      } else {
        annotationCreator.newAnnotation(generator, injectableField.qualifier)
      }
      generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR)
      generator.putStatic(injector.injectorType, KEY_FIELD_NAME_PREFIX + i, Types.KEY_TYPE)
    }
  }

  private fun initializeMethodKeys(generator: GeneratorAdapter) {
    injector.injectableTarget.injectableMethods.values.forEachIndexed { i, injectableMethod ->
      val argumentTypes = injectableMethod.argumentTypes
      val parameterQualifiers = injectableMethod.parameterQualifiers
      check(argumentTypes.size == parameterQualifiers.size)

      for (j in 0 until argumentTypes.size) {
        val dependencyType = getDependencyTypeForType(argumentTypes[j])
        val parameterQualifier = parameterQualifiers[j]

        generator.newInstance(Types.KEY_TYPE)
        generator.dup()
        generator.push(dependencyType)
        if (parameterQualifier == null) {
          generator.pushNull()
        } else {
          annotationCreator.newAnnotation(generator, parameterQualifier)
        }
        generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR)
        generator.putStatic(injector.injectorType, KEY_FIELD_NAME_PREFIX + i + '_' + j, Types.KEY_TYPE)
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

    if (!injector.injectableTarget.injectableFields.isEmpty()) {
      generator.loadArg(1)
      generator.checkCast(injector.injectableTarget.targetType)
      val injectableTargetLocal = generator.newLocal(injector.injectableTarget.targetType)
      generator.storeLocal(injectableTargetLocal)

      injector.injectableTarget.injectableFields.values.forEachIndexed { fieldIndex, injectableField ->
        generateFieldInitializer(generator, injectableTargetLocal, injectableField, fieldIndex)
      }
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateFieldInitializer(generator: GeneratorAdapter, injectableTargetLocal: Int,
      qualifiedField: QualifiedFieldDescriptor, fieldIndex: Int) {
    generator.loadLocal(injectableTargetLocal)
    generator.loadArg(0)

    generator.getStatic(injector.injectorType, KEY_FIELD_NAME_PREFIX + fieldIndex, Types.KEY_TYPE)

    val method = getInjectorMethodForType(qualifiedField.signature)
    generator.invokeInterface(Types.INJECTOR_TYPE, method)
    GenerationHelper.convertDependencyToTargetType(generator, qualifiedField.signature)
    generator.putField(injector.injectableTarget.targetType, qualifiedField.field)
  }

  private fun generateInjectMethodsMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, INJECT_METHODS_METHOD)
    generator.visitCode()

    generator.loadArg(1)
    generator.checkCast(injector.injectableTarget.targetType)
    val injectableTargetLocal = generator.newLocal(injector.injectableTarget.targetType)
    generator.storeLocal(injectableTargetLocal)

    injector.injectableTarget.injectableMethods.values.forEachIndexed { methodIndex, injectableMethod ->
      generateMethodInvocation(generator, injectableTargetLocal, injectableMethod, methodIndex)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateMethodInvocation(generator: GeneratorAdapter, injectableTargetLocal: Int,
      qualifiedMethod: QualifiedMethodDescriptor, methodIndex: Int) {
    generator.loadLocal(injectableTargetLocal)

    qualifiedMethod.argumentTypes.forEachIndexed { i, argumentType ->
      generator.loadArg(0)
      generator.getStatic(
          injector.injectorType, KEY_FIELD_NAME_PREFIX + methodIndex + '_' + i, Types.KEY_TYPE)
      val method = getInjectorMethodForType(argumentType)
      generator.invokeInterface(Types.INJECTOR_TYPE, method)
      GenerationHelper.convertDependencyToTargetType(generator, argumentType)
    }
    generator.invokeVirtual(injector.injectableTarget.targetType, qualifiedMethod.method)
  }
}
