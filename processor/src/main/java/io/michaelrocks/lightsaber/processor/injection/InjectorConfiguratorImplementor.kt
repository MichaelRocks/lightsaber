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

package io.michaelrocks.lightsaber.processor.injection

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.isStatic
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.invokeMethod
import io.michaelrocks.lightsaber.processor.commons.newMethod
import io.michaelrocks.lightsaber.processor.commons.toFieldDescriptor
import io.michaelrocks.lightsaber.processor.commons.toMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.model.ModuleProvider
import io.michaelrocks.lightsaber.processor.model.ModuleProvisionPoint
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

class InjectorConfiguratorImplementor(
  private val classVisitor: ClassVisitor,
  private val containerType: Type.Object
) {

  fun implementInjectorConfigurator(moduleProviders: Collection<ModuleProvider>, configurator: GeneratorAdapter.() -> Unit = {}) {
    classVisitor.newMethod(Opcodes.ACC_PUBLIC, CONFIGURE_INJECTOR_METHOD) {
      configurator()
      configureInjector(moduleProviders)
    }
  }

  private fun GeneratorAdapter.configureInjector(moduleProviders: Collection<ModuleProvider>) {
    moduleProviders.forEach { configureInjectorWithModule(it) }
  }

  private fun GeneratorAdapter.configureInjectorWithModule(moduleProvider: ModuleProvider) {
    loadModule(moduleProvider.provisionPoint)
    // TODO: It would be better to throw ConfigurationException here.
    checkCast(LightsaberTypes.INJECTOR_CONFIGURATOR_TYPE)
    loadArg(0)
    invokeInterface(LightsaberTypes.INJECTOR_CONFIGURATOR_TYPE, CONFIGURE_INJECTOR_METHOD)
  }

  private fun GeneratorAdapter.loadModule(provisionPoint: ModuleProvisionPoint) {
    return when (provisionPoint) {
      is ModuleProvisionPoint.Method -> loadModule(provisionPoint)
      is ModuleProvisionPoint.Field -> loadModule(provisionPoint)
    }
  }

  private fun GeneratorAdapter.loadModule(provisionPoint: ModuleProvisionPoint.Method) {
    if (!provisionPoint.method.isStatic) {
      loadThis()
      invokeMethod(containerType, provisionPoint.method)
    } else {
      invokeStatic(containerType, provisionPoint.method.toMethodDescriptor())
    }
  }

  private fun GeneratorAdapter.loadModule(provisionPoint: ModuleProvisionPoint.Field) {
    if (!provisionPoint.field.isStatic) {
      loadThis()
      getField(containerType, provisionPoint.field.toFieldDescriptor())
    } else {
      getStatic(containerType, provisionPoint.field.toFieldDescriptor())
    }
  }

  companion object {
    private val CONFIGURE_INJECTOR_METHOD =
      MethodDescriptor.forMethod("configureInjector", Type.Primitive.Void, LightsaberTypes.LIGHTSABER_INJECTOR_TYPE)
  }
}
