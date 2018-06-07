/*
 * Copyright 2018 Michael Rozumyanskiy
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
import io.michaelrocks.lightsaber.processor.commons.newMethod
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
        null
    )
    fieldVisitor.visitEnd()
  }

  private fun generateConstructor(classVisitor: ClassVisitor) {
    classVisitor.newMethod(ACC_PUBLIC, providerConstructor) {
      loadThis()

      if (provider.isConstructorProvider) {
        loadArg(0)
        invokeConstructor(ABSTRACT_INJECTING_PROVIDER_TYPE, SUPER_CONSTRUCTOR)
      } else {
        loadArg(1)
        invokeConstructor(ABSTRACT_INJECTING_PROVIDER_TYPE, SUPER_CONSTRUCTOR)

        loadThis()
        loadArg(0)
        putField(provider.type, MODULE_FIELD_NAME, provider.moduleType)
      }
    }
  }

  private fun generateGetWithInjectorMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, GET_WITH_INJECTOR_METHOD)
    generator.visitCode()

    val bridge = provider.provisionPoint.bridge
    if (bridge != null) {
      generator.provideFromMethod(bridge)
    } else {
      val provisionPoint = provider.provisionPoint
      when (provisionPoint) {
        is ProvisionPoint.Field -> generator.provideFromField(provisionPoint)
        is ProvisionPoint.Constructor -> generator.provideFromConstructor(provisionPoint)
        is ProvisionPoint.Method -> generator.provideFromMethod(provisionPoint)
      }
    }

    generator.valueOf(provider.dependency.type.rawType)

    generator.returnValue()
    generator.endMethod()
  }

  private fun GeneratorAdapter.provideFromField(provisionPoint: ProvisionPoint.Field) {
    loadThis()
    getField(provider.type, MODULE_FIELD_NAME, provider.moduleType)
    val field = provisionPoint.field.toFieldDescriptor()
    getField(provider.moduleType, field)
  }

  private fun GeneratorAdapter.provideFromConstructor(provisionPoint: ProvisionPoint.Constructor) {
    invokeConstructor(provisionPoint)
    injectMembers()
  }

  private fun GeneratorAdapter.invokeConstructor(provisionPoint: ProvisionPoint.Constructor) {
    newInstance(provider.dependency.type.rawType)
    dup()
    loadArguments(provisionPoint)
    val method = provisionPoint.method.toMethodDescriptor()
    invokeConstructor(provider.dependency.type.rawType, method)
  }

  private fun GeneratorAdapter.provideFromMethod(provisionPoint: ProvisionPoint.Method) {
    loadThis()
    getField(provider.type, MODULE_FIELD_NAME, provider.moduleType)
    loadArguments(provisionPoint)
    invokeVirtual(provider.moduleType, provisionPoint.method.toMethodDescriptor())

    if (provider.dependency.type.rawType.isPrimitive) {
      return
    }

    val resultIsNullLabel = newLabel()
    dup()
    ifNonNull(resultIsNullLabel)
    throwException(NULL_POINTER_EXCEPTION_TYPE, "Provider method returned null")

    visitLabel(resultIsNullLabel)
  }

  private fun GeneratorAdapter.loadArguments(provisionPoint: ProvisionPoint.AbstractMethod) {
    val injectees = provisionPoint.injectionPoint.injectees
    injectees.forEach { loadArgument(it) }
  }

  private fun GeneratorAdapter.loadArgument(injectee: Injectee) {
    loadArg(0)
    getDependency(keyRegistry, injectee)
  }

  private fun GeneratorAdapter.injectMembers() {
    dup()
    loadArg(0)
    swap()
    invokeInterface(Types.INJECTOR_TYPE, INJECT_MEMBERS_METHOD)
  }
}
