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

import io.michaelrocks.lightsaber.processor.generation.model.GenerationContext
import io.michaelrocks.lightsaber.processor.generation.model.InjectorConfigurator
import io.michaelrocks.lightsaber.processor.generation.model.MembersInjector
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.templates.TemplateLoader
import org.objectweb.asm.Type

private val INJECTION_DISPATCHER_TYPE = Type.getObjectType("io/michaelrocks/lightsaber/InjectionDispatcher")
private const val STATIC_INITIALIZER_PARAMETER = "STATIC_INITIALIZER"

class InjectorDispatcherSourceGenerator(
    private val sourceProducer: SourceProducer
) {
  private val InjectorConfigurator.className: String
    get() = type.internalName.substringAfterLast('/')

  fun generate(generationContext: GenerationContext) {
    val template = TemplateLoader().loadTemplate(INJECTION_DISPATCHER_TYPE)
    val sourceCode = template.newRenderer()
        .substitute(STATIC_INITIALIZER_PARAMETER, composeStaticInitializer(generationContext))
        .render()
    sourceProducer.produceSourceFile(INJECTION_DISPATCHER_TYPE.internalName, sourceCode)
  }

  private fun composeStaticInitializer(generationContext: GenerationContext): String {
    return buildString {
      putPackageInjectorConfigurator(generationContext.packageInjectorConfigurator)
      generationContext.injectorConfigurators.forEach { injectorConfigurator ->
        val packageInvader = generationContext.findPackageInvaderByTargetType(injectorConfigurator.component.type)
        putInjectorConfigurator(injectorConfigurator, packageInvader)
      }
      appendln()
      generationContext.membersInjectors.forEach { putMembersInjector(it) }
    }
  }

  private fun StringBuilder.putPackageInjectorConfigurator(injectorConfigurator: InjectorConfigurator) {
    val configurator = injectorConfigurator.className
    putToInjectorConfigurators("null", "new $configurator()")
  }

  private fun StringBuilder.putInjectorConfigurator(injectorConfigurator: InjectorConfigurator,
      packageInvader: PackageInvader?) {
    val component = injectorConfigurator.component.getClassReference(packageInvader)
    val configurator = injectorConfigurator.className
    putToInjectorConfigurators("$component", "new $configurator()")
  }

  private fun Component.getClassReference(packageInvader: PackageInvader?): String {
    val componentField = packageInvader?.fields?.get(type)
    if (componentField == null) {
      return "${type.className}.class"
    } else {
      return "${packageInvader!!.type.className}.${componentField.name}"
    }
  }

  private fun StringBuilder.putToInjectorConfigurators(key: String, value: String) {
    appendln("injectorConfigurators.put($key, $value);")
  }

  private fun StringBuilder.putMembersInjector(membersInjector: MembersInjector) {
    val target = membersInjector.target.type.className
    val injector = membersInjector.type.className
    putToMembersInjectors("$target.class", "new $injector()")
  }

  private fun StringBuilder.putToMembersInjectors(key: String, value: String) {
    appendln("membersInjectors.put($key, $value);")
  }
}
