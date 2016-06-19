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
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectType
import io.michaelrocks.grip.mirrors.isPrimitive
import io.michaelrocks.lightsaber.internal.AbstractInjectingProvider
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.cast
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.commons.toFieldDescriptor
import io.michaelrocks.lightsaber.processor.commons.toMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.model.Injectee
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint
import io.michaelrocks.lightsaber.processor.model.isConstructorProvider
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6

class ProviderClassGenerator(
    private val classRegistry: ClassRegistry,
    private val keyRegistry: KeyRegistry,
    private val provider: Provider
) {
  companion object {
    private const val MODULE_FIELD_NAME = "module"

    private val ABSTRACT_INJECTING_PROVIDER_TYPE = getObjectType<AbstractInjectingProvider<*>>()
    private val NULL_POINTER_EXCEPTION_TYPE = getObjectType<NullPointerException>()

    private val SUPER_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.INJECTOR_TYPE)

    private val GET_WITH_INJECTOR_METHOD =
        MethodDescriptor.forMethod("getWithInjector", Types.OBJECT_TYPE, Types.INJECTOR_TYPE)
    private val GET_PROVIDER_METHOD =
        MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.KEY_TYPE)
    private val INJECT_MEMBERS_METHOD =
        MethodDescriptor.forMethod("injectMembers", Type.Primitive.Void, Types.OBJECT_TYPE)
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
        ABSTRACT_INJECTING_PROVIDER_TYPE.internalName,
        null
    )

    generateFields(classVisitor)
    generateConstructor(classVisitor)
    generateGetWithInjectorMethod(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateFields(classVisitor: ClassVisitor) {
    if (!provider.isConstructorProvider) {
      generateModuleField(classVisitor)
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

  private fun generateConstructor(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, providerConstructor)
    generator.visitCode()
    generator.loadThis()

    if (provider.isConstructorProvider) {
      generator.loadArg(0)
      generator.invokeConstructor(ABSTRACT_INJECTING_PROVIDER_TYPE, SUPER_CONSTRUCTOR)
    } else {
      generator.loadArg(1)
      generator.invokeConstructor(ABSTRACT_INJECTING_PROVIDER_TYPE, SUPER_CONSTRUCTOR)

      generator.loadThis()
      generator.loadArg(0)
      generator.putField(provider.type, MODULE_FIELD_NAME, provider.moduleType)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateGetWithInjectorMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, GET_WITH_INJECTOR_METHOD)
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

    if (provider.dependency.type.rawType.isPrimitive) {
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
    generator.loadArg(0)
    generator.getKey(keyRegistry, injectee.dependency)
    generator.invokeInterface(Types.INJECTOR_TYPE, GET_PROVIDER_METHOD)
    generator.convertDependencyToTargetType(injectee)
  }

  private fun generateInjectMembersInvocation(generator: GeneratorAdapter) {
    generator.dup()
    generator.loadArg(0)
    generator.swap()
    generator.invokeInterface(Types.INJECTOR_TYPE, INJECT_MEMBERS_METHOD)
  }
}
