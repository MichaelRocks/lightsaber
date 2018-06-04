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

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.newLocal
import io.michaelrocks.lightsaber.processor.commons.newMethod
import io.michaelrocks.lightsaber.processor.commons.toFieldDescriptor
import io.michaelrocks.lightsaber.processor.commons.toMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.generation.registerProvider
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.ModuleProvider
import io.michaelrocks.lightsaber.processor.model.ModuleProvisionPoint
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.isConstructorProvider
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ACC_PUBLIC

class ComponentPatcher(
    classVisitor: ClassVisitor,
    private val keyRegistry: KeyRegistry,
    private val component: Component
) : BaseInjectionClassVisitor(classVisitor) {

  private var isInjectorConfigurator = false

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
      newMethod(ACC_PUBLIC, CONFIGURE_INJECTOR_METHOD) { configureInjector() }
    }
    super.visitEnd()
  }

  private fun GeneratorAdapter.configureInjector() {
    component.providers.forEach { configureInjectorWithModule(it) }
  }

  private fun GeneratorAdapter.configureInjectorWithModule(moduleProvider: ModuleProvider) {
    loadThis()
    val moduleLocal = getModule(moduleProvider.provisionPoint)

    moduleProvider.module.providers.forEach { provider ->
      loadArg(0)
      registerProvider(keyRegistry, provider) {
        if (provider.isConstructorProvider) {
          newConstructorProvider(provider)
        } else {
          newModuleProvider(provider) {
            loadLocal(moduleLocal)
          }
        }
      }
    }
  }

  private fun GeneratorAdapter.getModule(provisionPoint: ModuleProvisionPoint): Int {
    return when (provisionPoint) {
      is ModuleProvisionPoint.Method -> getModule(provisionPoint)
      is ModuleProvisionPoint.Field -> getModule(provisionPoint)
    }
  }

  private fun GeneratorAdapter.getModule(provisionPoint: ModuleProvisionPoint.Method): Int {
    return newLocal(provisionPoint.method.type.returnType) {
      invokeVirtual(component.type, provisionPoint.method.toMethodDescriptor())
    }
  }

  private fun GeneratorAdapter.getModule(provisionPoint: ModuleProvisionPoint.Field): Int {
    return newLocal(provisionPoint.field.type) {
      getField(component.type, provisionPoint.field.toFieldDescriptor())
    }
  }

  private fun GeneratorAdapter.newModuleProvider(provider: Provider, moduleRetriever: () -> Unit) {
    newInstance(provider.type)
    dup()
    moduleRetriever()
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
