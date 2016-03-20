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
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.*
import io.michaelrocks.lightsaber.processor.descriptors.*
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

class ProviderClassGenerator(
    private val classRegistry: ClassRegistry,
    private val annotationCreator: AnnotationCreator,
    private val provider: ProviderDescriptor
) {
  companion object {
    private const val KEY_FIELD_NAME_PREFIX = "key"
    private const val MODULE_FIELD_NAME = "module"

    private val NULL_POINTER_EXCEPTION_TYPE = getType<NullPointerException>()

    private val INJECTOR_FIELD = FieldDescriptor("injector", Types.INJECTOR_TYPE)

    private val KEY_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE)
    private val GET_METHOD = MethodDescriptor.forMethod("get", Types.OBJECT_TYPE)
    private val GET_PROVIDER_METHOD = MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.KEY_TYPE)
    private val INJECT_MEMBERS_METHOD = MethodDescriptor.forMethod("injectMembers", Type.VOID_TYPE, Types.OBJECT_TYPE)
  }

  private val providerConstructor: MethodDescriptor
    get() {
      if (provider.isConstructorProvider) {
        return MethodDescriptor.forConstructor(Types.INJECTOR_TYPE)
      } else {
        return MethodDescriptor.forConstructor(provider.moduleType, Types.INJECTOR_TYPE)
      }
    }

  fun generate(): ByteArray {
    val classWriter = StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
        V1_6,
        ACC_PUBLIC or ACC_SUPER,
        provider.providerType.internalName,
        null,
        Types.OBJECT_TYPE.internalName,
        arrayOf(Types.PROVIDER_TYPE.internalName))

    generateFields(classVisitor)
    generateStaticInitializer(classVisitor)
    generateConstructor(classVisitor)
    generateGetMethod(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateFields(classVisitor: ClassVisitor) {
    generateKeyFields(classVisitor)
    if (!provider.isConstructorProvider) {
      generateModuleField(classVisitor)
    }
    generateInjectorField(classVisitor)
  }

  private fun generateKeyFields(classVisitor: ClassVisitor) {
    provider.providerMethod?.argumentTypes?.forEachIndexed { index, argumentType ->
      val fieldVisitor = classVisitor.visitField(
          ACC_PRIVATE or ACC_STATIC or ACC_FINAL,
          KEY_FIELD_NAME_PREFIX + index,
          Types.KEY_TYPE.descriptor,
          null,
          null)
      fieldVisitor.visitEnd()
    }
  }

  private fun generateModuleField(classVisitor: ClassVisitor) {
    val fieldVisitor = classVisitor.visitField(
        ACC_PRIVATE or ACC_FINAL,
        MODULE_FIELD_NAME,
        provider.moduleType.descriptor,
        null,
        null)
    fieldVisitor.visitEnd()
  }

  private fun generateInjectorField(classVisitor: ClassVisitor) {
    val fieldVisitor = classVisitor.visitField(
        ACC_PRIVATE or ACC_FINAL,
        INJECTOR_FIELD.name,
        INJECTOR_FIELD.descriptor,
        null,
        null)
    fieldVisitor.visitEnd()
  }

  private fun generateStaticInitializer(classVisitor: ClassVisitor) {
    if (provider.providerMethod == null) {
      return
    }

    val argumentTypes = provider.providerMethod.argumentTypes
    val parameterQualifiers = provider.providerMethod.parameterQualifiers
    check(argumentTypes.size == parameterQualifiers.size)

    if (argumentTypes.isEmpty()) {
      return
    }

    val staticInitializer = MethodDescriptor.forStaticInitializer()
    val generator = GeneratorAdapter(classVisitor, ACC_STATIC, staticInitializer)
    generator.visitCode()

    argumentTypes.forEachIndexed { index, argumentType ->
      val parameterQualifier = parameterQualifiers[index]
      val dependencyType = argumentType.parameterType ?: Types.box(argumentType.rawType)

      generator.newInstance(Types.KEY_TYPE)
      generator.dup()
      generator.push(dependencyType)
      if (parameterQualifier == null) {
        generator.pushNull()
      } else {
        annotationCreator.newAnnotation(generator, parameterQualifier)
      }
      generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR)
      generator.putStatic(provider.providerType, KEY_FIELD_NAME_PREFIX + index, Types.KEY_TYPE)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateConstructor(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, providerConstructor)
    generator.visitCode()
    generator.loadThis()
    generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forConstructor())

    if (provider.isConstructorProvider) {
      generator.loadThis()
      generator.loadArg(0)
      generator.putField(provider.providerType, INJECTOR_FIELD)
    } else {
      generator.loadThis()
      generator.loadArg(0)
      generator.putField(provider.providerType, MODULE_FIELD_NAME, provider.moduleType)
      generator.loadThis()
      generator.loadArg(1)
      generator.putField(provider.providerType, INJECTOR_FIELD)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateGetMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, GET_METHOD)
    generator.visitCode()

    if (provider.providerField != null) {
      generateProviderFieldRetrieval(generator)
    } else if (provider.providerMethod!!.isConstructor) {
      generateConstructorInvocation(generator)
      generateInjectMembersInvocation(generator)
    } else {
      generateProviderMethodInvocation(generator)
    }

    generator.valueOf(provider.providableType)

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateProviderFieldRetrieval(generator: GeneratorAdapter) {
    generator.loadThis()
    generator.getField(provider.providerType, MODULE_FIELD_NAME, provider.moduleType)
    generator.getField(provider.moduleType, provider.providerField!!.field)
  }

  private fun generateConstructorInvocation(generator: GeneratorAdapter) {
    generator.newInstance(provider.providableType)
    generator.dup()
    generateProvideMethodArguments(generator)
    generator.invokeConstructor(provider.providableType, provider.providerMethod!!.method)
  }

  private fun generateProviderMethodInvocation(generator: GeneratorAdapter) {
    generator.loadThis()
    generator.getField(provider.providerType, MODULE_FIELD_NAME, provider.moduleType)
    generateProvideMethodArguments(generator)
    generator.invokeVirtual(provider.moduleType, provider.providerMethod!!.method)

    if (Types.isPrimitive(provider.providableType)) {
      return
    }

    val resultIsNullLabel = generator.newLabel()
    generator.dup()
    generator.ifNonNull(resultIsNullLabel)
    generator.throwException(NULL_POINTER_EXCEPTION_TYPE, "Provider method returned null")

    generator.visitLabel(resultIsNullLabel)
  }

  private fun generateProvideMethodArguments(generator: GeneratorAdapter) {
    provider.providerMethod!!.argumentTypes.forEachIndexed { index, argumentType ->
      generateProviderMethodArgument(generator, argumentType, index)
    }
  }

  private fun generateProviderMethodArgument(generator: GeneratorAdapter, argumentType: GenericType,
      argumentIndex: Int) {
    generator.loadThis()
    generator.getField(provider.providerType, INJECTOR_FIELD)
    generator.getStatic(provider.providerType, KEY_FIELD_NAME_PREFIX + argumentIndex, Types.KEY_TYPE)
    generator.invokeInterface(Types.INJECTOR_TYPE, GET_PROVIDER_METHOD)
    if (argumentType.parameterType == null) {
      generator.invokeInterface(Types.PROVIDER_TYPE, GET_METHOD)
    }
    GenerationHelper.convertDependencyToTargetType(generator, argumentType)
  }

  private fun generateInjectMembersInvocation(generator: GeneratorAdapter) {
    generator.dup()
    generator.loadThis()
    generator.getField(provider.providerType, INJECTOR_FIELD)
    generator.swap()
    generator.invokeInterface(Types.INJECTOR_TYPE, INJECT_MEMBERS_METHOD)
  }
}
