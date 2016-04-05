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
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.box
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.generation.model.GenerationContext
import io.michaelrocks.lightsaber.processor.generation.model.InjectorConfigurator
import io.michaelrocks.lightsaber.processor.generation.model.MembersInjector
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.io.FileSink
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import io.michaelrocks.lightsaber.processor.model.Module
import org.objectweb.asm.Type
import java.util.*

class Generator(
    private val classRegistry: ClassRegistry,
    private val errorReporter: ErrorReporter,
    fileSink: FileSink
) {
  private val classProducer = ProcessorClassProducer(fileSink, errorReporter)
  private val annotationCreator = AnnotationCreator(classProducer, classRegistry)

  fun generate(injectionContext: InjectionContext) {
    val generationConfiguration = composeGeneratorModel(injectionContext)
    generateProviders(injectionContext)
    generateLightsaberConfigurator(generationConfiguration)
    generateInjectorConfigurators(generationConfiguration)
    generateInjectors(generationConfiguration)
    generatePackageInvaders(generationConfiguration)
  }

  private fun composeGeneratorModel(context: InjectionContext) =
      GenerationContext(
          composePackageInjectorConfigurators(context),
          composeInjectorConfigurators(context),
          composeMembersInjectors(context),
          composePackageInvaders(context)
      )

  private fun composePackageInjectorConfigurators(context: InjectionContext) =
      context.packageModules.map { module ->
        val configuratorType = composeConfiguratorType(module)
        InjectorConfigurator(configuratorType, module)
      }

  private fun composeInjectorConfigurators(context: InjectionContext) =
      context.modules.map { module ->
        val configuratorType = composeConfiguratorType(module)
        InjectorConfigurator(configuratorType, module)
      }

  private fun composeConfiguratorType(module: Module): Type {
    val moduleNameWithDollars = module.type.internalName.replace('/', '$')
    return Type.getObjectType("io/michaelrocks/lightsaber/InjectorConfigurator\$$moduleNameWithDollars")
  }

  private fun composeMembersInjectors(context: InjectionContext) =
      context.injectableTargets.map { injectableTarget ->
        val injectorType = Type.getObjectType(injectableTarget.type.internalName + "\$MembersInjector")
        MembersInjector(injectorType, injectableTarget)
      }

  private fun composePackageInvaders(context: InjectionContext): Collection<PackageInvader> {
    val builders = HashMap<String, PackageInvader.Builder>()
    for (module in context.allModules) {
      for (provider in module.providers) {
        val providableType = provider.dependency.type.rawType.box()
        val packageName = Types.getPackageName(module.type)
        val builder = builders[packageName] ?: PackageInvader.Builder(packageName).apply {
          builders.put(packageName, this)
        }
        builder.addClass(providableType)
      }
    }

    return builders.values.map { it.build() }
  }

  private fun generateProviders(injectionContext: InjectionContext) {
    val generator = ProvidersGenerator(classProducer, classRegistry, annotationCreator)
    generator.generate(injectionContext)
  }

  private fun generateLightsaberConfigurator(generationContext: GenerationContext) {
    val generator = LightsaberRegistryClassGenerator(classProducer, classRegistry)
    generator.generate(generationContext)
  }

  private fun generateInjectorConfigurators(generationContext: GenerationContext) {
    val generator = InjectorConfiguratorsGenerator(classProducer, classRegistry, annotationCreator)
    generator.generate(generationContext)
  }

  private fun generateInjectors(generationContext: GenerationContext) {
    val typeAgentsGenerator = TypeAgentsGenerator(classProducer, classRegistry, annotationCreator)
    typeAgentsGenerator.generate(generationContext)
  }

  private fun generatePackageInvaders(generationContext: GenerationContext) {
    val packageInvadersGenerator = PackageInvadersGenerator(classProducer, classRegistry)
    packageInvadersGenerator.generate(generationContext)
  }
}
