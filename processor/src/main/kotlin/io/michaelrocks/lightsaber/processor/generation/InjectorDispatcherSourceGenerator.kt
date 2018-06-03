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
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.lightsaber.processor.generation.model.GenerationContext
import io.michaelrocks.lightsaber.processor.generation.model.MembersInjector
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.templates.TemplateLoader
import io.michaelrocks.lightsaber.processor.templates.loadTemplate

private val INJECTION_DISPATCHER_TYPE = getObjectTypeByInternalName("io/michaelrocks/lightsaber/InjectionDispatcher")
private const val STATIC_INITIALIZER_PARAMETER = "STATIC_INITIALIZER"

class InjectorDispatcherSourceGenerator(
    private val sourceProducer: SourceProducer,
    private val classRegistry: ClassRegistry
) {
  fun generate(generationContext: GenerationContext) {
    val template = TemplateLoader().loadTemplate(INJECTION_DISPATCHER_TYPE)
    val sourceCode = template.newRenderer()
        .substitute(STATIC_INITIALIZER_PARAMETER, composeStaticInitializer(generationContext))
        .render()
    sourceProducer.produceSourceFile(INJECTION_DISPATCHER_TYPE.internalName, sourceCode)
  }

  private fun composeStaticInitializer(generationContext: GenerationContext): String {
    return buildString {
      generationContext.membersInjectors.forEach { membersInjector ->
        val packageInvader = generationContext.findPackageInvaderByTargetType(membersInjector.type)
        putMembersInjector(membersInjector, packageInvader)
      }
    }
  }

  private fun StringBuilder.putMembersInjector(membersInjector: MembersInjector, packageInvader: PackageInvader?) {
    val target = membersInjector.target.type.getClassReference(packageInvader)
    val injector = membersInjector.type.className
    putToMembersInjectors(target, "new $injector()")
  }

  private fun StringBuilder.putToMembersInjectors(key: String, value: String) {
    appendln("membersInjectors.put($key, $value);")
  }

  private fun Type.Object.getClassReference(packageInvader: PackageInvader?): String {
    val componentField = packageInvader?.fields?.get(this)
    return if (componentField == null) {
      "${getJavaClassName()}.class"
    } else {
      "${packageInvader.type.className}.${componentField.name}"
    }
  }

  private fun Type.Object.getJavaClassName(): String {
    return classRegistry.getClassMirror(this).name
  }
}
