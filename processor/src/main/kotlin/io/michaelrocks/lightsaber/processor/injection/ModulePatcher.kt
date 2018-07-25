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

package io.michaelrocks.lightsaber.processor.injection

import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.isStatic
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.invokeMethod
import io.michaelrocks.lightsaber.processor.commons.newMethod
import io.michaelrocks.lightsaber.processor.commons.toFieldDescriptor
import io.michaelrocks.lightsaber.processor.commons.toMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.generation.registerProvider
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint
import io.michaelrocks.lightsaber.processor.model.isConstructorProvider
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SYNTHETIC

class ModulePatcher(
    classVisitor: ClassVisitor,
    private val keyRegistry: KeyRegistry,
    private val module: Module
) : BaseInjectionClassVisitor(classVisitor) {
  private val providableFields: MutableSet<FieldDescriptor>
  private val providableMethods: MutableSet<MethodDescriptor>

  private var isInjectorConfigurator = false

  init {
    providableFields = HashSet(module.providers.size)
    providableMethods = HashSet(module.providers.size)
    for (provider in module.providers) {
      val provisionPoint = provider.provisionPoint
      when (provisionPoint) {
        is ProvisionPoint.Field -> providableFields.add(provisionPoint.field.toFieldDescriptor())
        is ProvisionPoint.AbstractMethod -> providableMethods.add(provisionPoint.method.toMethodDescriptor())
      }
    }
  }

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?,
      interfaces: Array<String>?) {
    val injectorConfiguratorType = LightsaberTypes.INJECTOR_CONFIGURATOR_TYPE.internalName
    if (interfaces == null || injectorConfiguratorType !in interfaces) {
      val newInterfaces =
          if (interfaces == null) arrayOf(injectorConfiguratorType) else interfaces + injectorConfiguratorType
      super.visit(version, access, name, signature, superName, newInterfaces)
      isDirty = true
    } else {
      super.visit(version, access, name, signature, superName, interfaces)
      isInjectorConfigurator = true
    }
  }

  override fun visitEnd() {
    if (!isInjectorConfigurator) {
      generateBridges()
      newMethod(ACC_PUBLIC, CONFIGURE_INJECTOR_METHOD) { configureInjector() }
    }
    super.visitEnd()
  }

  private fun generateBridges() {
    module.providers.forEach { provider ->
      val provisionPoint = provider.provisionPoint
      val bridge = provisionPoint.bridge
      if (bridge != null) {
        newMethod(ACC_PUBLIC or ACC_SYNTHETIC, bridge.method.toMethodDescriptor()) {
          generateBridge(provisionPoint)
        }
      }
    }
  }

  private fun GeneratorAdapter.generateBridge(provisionPoint: ProvisionPoint) {
    return when (provisionPoint) {
      is ProvisionPoint.Field -> getBridgedField(provisionPoint.field)
      is ProvisionPoint.Method -> invokeBridgedMethod(provisionPoint.method)
      else -> error("Unexpected provision point $provisionPoint for bridge method")
    }
  }

  private fun GeneratorAdapter.getBridgedField(field: FieldMirror) {
    loadThis()
    getField(module.type, field.toFieldDescriptor())
  }

  private fun GeneratorAdapter.invokeBridgedMethod(method: MethodMirror) {
    if (method.isStatic) {
      loadArgs()
      invokeStatic(module.type, method.toMethodDescriptor())
      return
    }

    loadThis()
    loadArgs()
    invokeMethod(module.type, method)
  }

  private fun GeneratorAdapter.configureInjector() {
    module.providers.forEach { provider ->
      loadArg(0)
      registerProvider(keyRegistry, provider) {
        if (provider.isConstructorProvider) {
          newConstructorProvider(provider)
        } else {
          newModuleProvider(provider)
        }
      }
    }
  }

  private fun GeneratorAdapter.newModuleProvider(provider: Provider) {
    newInstance(provider.type)
    dup()
    loadThis()
    loadArg(0)
    val constructor = MethodDescriptor.forConstructor(provider.moduleType, Types.INJECTOR_TYPE)
    invokeConstructor(provider.type, constructor)
  }

  private fun GeneratorAdapter.newConstructorProvider(provider: Provider) {
    newInstance(provider.type)
    dup()
    loadArg(0)
    val constructor = MethodDescriptor.forConstructor(Types.INJECTOR_TYPE)
    invokeConstructor(provider.type, constructor)
  }

  companion object {
    private val CONFIGURE_INJECTOR_METHOD =
        MethodDescriptor.forMethod("configureInjector", Type.Primitive.Void, LightsaberTypes.LIGHTSABER_INJECTOR_TYPE)
  }
}
