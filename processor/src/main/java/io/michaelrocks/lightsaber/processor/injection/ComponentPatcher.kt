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

import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.model.Component
import org.objectweb.asm.ClassVisitor

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
      InjectorConfiguratorImplementor(this, component.type).implementInjectorConfigurator(component.moduleProviders)
    }
    super.visitEnd()
  }
}
