/*
 * Copyright 2019 Michael Rozumyanskiy
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
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.newMethod
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.commons.toFieldDescriptor
import io.michaelrocks.lightsaber.processor.commons.toMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
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

    private val NULL_POINTER_EXCEPTION_TYPE = getObjectType<NullPointerException>()

    private val INJECTOR_FIELD = FieldDescriptor("injector", Types.INJECTOR_TYPE)

    private val GET_METHOD =
      MethodDescriptor.forMethod("get", Types.OBJECT_TYPE)
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
      Types.OBJECT_TYPE.internalName,
      arrayOf(Types.PROVIDER_TYPE.internalName)
    )

    generateFields(classVisitor)
    generateConstructor(classVisitor)
    generateGetMethod(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateFields(classVisitor: ClassVisitor) {
    generateInjectorField(classVisitor)
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
    classVisitor.newMethod(ACC_PUBLIC, providerConstructor) {
      visitCode()
      loadThis()
      invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forDefaultConstructor())

      if (provider.isConstructorProvider) {
        loadThis()
        loadArg(0)
        putField(provider.type, INJECTOR_FIELD)
      } else {
        loadThis()
        loadArg(1)
        putField(provider.type, INJECTOR_FIELD)

        loadThis()
        loadArg(0)
        putField(provider.type, MODULE_FIELD_NAME, provider.moduleType)
      }
    }
  }

  private fun generateGetMethod(classVisitor: ClassVisitor) {
    classVisitor.newMethod(ACC_PUBLIC, GET_METHOD) {
      val bridge = provider.provisionPoint.bridge
      if (bridge != null) {
        provideFromMethod(bridge)
      } else {
        val provisionPoint = provider.provisionPoint
        when (provisionPoint) {
          is ProvisionPoint.Field -> provideFromField(provisionPoint)
          is ProvisionPoint.Constructor -> provideFromConstructor(provisionPoint)
          is ProvisionPoint.Method -> provideFromMethod(provisionPoint)
        }
      }

      valueOf(provider.dependency.type.rawType)
    }
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
    newInstance(provisionPoint.containerType)
    dup()
    loadArguments(provisionPoint)
    val method = provisionPoint.method.toMethodDescriptor()
    invokeConstructor(provisionPoint.containerType, method)
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
    loadThis()
    getField(provider.type, INJECTOR_FIELD)
    getDependency(keyRegistry, injectee)
  }

  private fun GeneratorAdapter.injectMembers() {
    dup()
    loadThis()
    getField(provider.type, INJECTOR_FIELD)
    swap()
    invokeInterface(Types.INJECTOR_TYPE, INJECT_MEMBERS_METHOD)
  }
}
