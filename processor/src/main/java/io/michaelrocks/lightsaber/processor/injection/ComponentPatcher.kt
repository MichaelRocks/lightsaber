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
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.ModuleProvider
import io.michaelrocks.lightsaber.processor.model.ModuleProvisionPoint
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ACC_PUBLIC

class ComponentPatcher(
  classVisitor: ClassVisitor,
  private val component: Component
) : BaseInjectionClassVisitor(classVisitor) {

  private var isInjectorConfigurator = false

  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<String>?
  ) {
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
      invokeMethod(component.type, provisionPoint.method)
    } else {
      invokeStatic(component.type, provisionPoint.method.toMethodDescriptor())
    }
  }

  private fun GeneratorAdapter.loadModule(provisionPoint: ModuleProvisionPoint.Field) {
    if (!provisionPoint.field.isStatic) {
      loadThis()
      getField(component.type, provisionPoint.field.toFieldDescriptor())
    } else {
      getStatic(component.type, provisionPoint.field.toFieldDescriptor())
    }
  }

  companion object {
    private val CONFIGURE_INJECTOR_METHOD =
      MethodDescriptor.forMethod("configureInjector", Type.Primitive.Void, LightsaberTypes.LIGHTSABER_INJECTOR_TYPE)
  }
}
