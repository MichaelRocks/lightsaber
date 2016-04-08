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
import io.michaelrocks.lightsaber.processor.commons.*
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.descriptor
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.model.Injectee
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint
import io.michaelrocks.lightsaber.processor.model.isConstructorProvider
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

class ProviderClassGenerator(
    private val classRegistry: ClassRegistry,
    private val keyRegistry: KeyRegistry,
    private val provider: Provider
) {
  companion object {
    private const val MODULE_FIELD_NAME = "module"

    private val NULL_POINTER_EXCEPTION_TYPE = getType<NullPointerException>()

    private val INJECTOR_FIELD = FieldDescriptor("injector", Types.INJECTOR_TYPE)

    private val GET_METHOD = MethodDescriptor.forMethod("get", Types.OBJECT_TYPE)
    private val GET_PROVIDER_METHOD = MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.KEY_TYPE)
    private val INJECT_MEMBERS_METHOD = MethodDescriptor.forMethod("injectMembers", Type.VOID_TYPE, Types.OBJECT_TYPE)
  }

  private val providerConstructor: MethodDescriptor
    get() {
      if (provider.provisionPoint is ProvisionPoint.Constructor) {
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
        provider.type.internalName,
        null,
        Types.OBJECT_TYPE.internalName,
        arrayOf(Types.PROVIDER_TYPE.internalName))

    generateFields(classVisitor)
    generateConstructor(classVisitor)
    generateGetMethod(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateFields(classVisitor: ClassVisitor) {
    if (!provider.isConstructorProvider) {
      generateModuleField(classVisitor)
    }
    generateInjectorField(classVisitor)
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

  private fun generateConstructor(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, providerConstructor)
    generator.visitCode()
    generator.loadThis()
    generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forConstructor())

    if (provider.isConstructorProvider) {
      generator.loadThis()
      generator.loadArg(0)
      generator.putField(provider.type, INJECTOR_FIELD)
    } else {
      generator.loadThis()
      generator.loadArg(0)
      generator.putField(provider.type, MODULE_FIELD_NAME, provider.moduleType)
      generator.loadThis()
      generator.loadArg(1)
      generator.putField(provider.type, INJECTOR_FIELD)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateGetMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, GET_METHOD)
    generator.visitCode()

    if (provider.provisionPoint is ProvisionPoint.Field) {
      generateProviderFieldRetrieval(generator)
    } else if (provider.isConstructorProvider) {
      generateConstructorInvocation(generator)
      generateInjectMembersInvocation(generator)
    } else {
      generateProviderMethodInvocation(generator)
    }

    generator.valueOf(provider.dependency.type.rawType)

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateProviderFieldRetrieval(generator: GeneratorAdapter) {
    generator.loadThis()
    generator.getField(provider.type, MODULE_FIELD_NAME, provider.moduleType)
    val field = provider.provisionPoint.cast<ProvisionPoint.Field>().field.toFieldDescriptor()
    generator.getField(provider.moduleType, field)
  }

  private fun generateConstructorInvocation(generator: GeneratorAdapter) {
    generator.newInstance(provider.dependency.type.rawType)
    generator.dup()
    generateProvideMethodArguments(generator)
    val method = provider.provisionPoint.cast<ProvisionPoint.AbstractMethod>().method.toMethodDescriptor()
    generator.invokeConstructor(provider.dependency.type.rawType, method)
  }

  private fun generateProviderMethodInvocation(generator: GeneratorAdapter) {
    generator.loadThis()
    generator.getField(provider.type, MODULE_FIELD_NAME, provider.moduleType)
    generateProvideMethodArguments(generator)
    val method = provider.provisionPoint.cast<ProvisionPoint.AbstractMethod>().method.toMethodDescriptor()
    generator.invokeVirtual(provider.moduleType, method)

    if (Types.isPrimitive(provider.dependency.type.rawType)) {
      return
    }

    val resultIsNullLabel = generator.newLabel()
    generator.dup()
    generator.ifNonNull(resultIsNullLabel)
    generator.throwException(NULL_POINTER_EXCEPTION_TYPE, "Provider method returned null")

    generator.visitLabel(resultIsNullLabel)
  }

  private fun generateProvideMethodArguments(generator: GeneratorAdapter) {
    val injectees = provider.provisionPoint.cast<ProvisionPoint.AbstractMethod>().injectionPoint.injectees
    injectees.forEach { injectee ->
      generateProviderMethodArgument(generator, injectee)
    }
  }

  private fun generateProviderMethodArgument(generator: GeneratorAdapter, injectee: Injectee) {
    generator.loadThis()
    generator.getField(provider.type, INJECTOR_FIELD)
    generator.getKey(keyRegistry, injectee.dependency)
    generator.invokeInterface(Types.INJECTOR_TYPE, GET_PROVIDER_METHOD)
    generator.convertDependencyToTargetType(injectee)
  }

  private fun generateInjectMembersInvocation(generator: GeneratorAdapter) {
    generator.dup()
    generator.loadThis()
    generator.getField(provider.type, INJECTOR_FIELD)
    generator.swap()
    generator.invokeInterface(Types.INJECTOR_TYPE, INJECT_MEMBERS_METHOD)
  }
}
