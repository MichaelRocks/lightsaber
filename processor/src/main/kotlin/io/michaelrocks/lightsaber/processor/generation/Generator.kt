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

import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.box
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.generation.model.GenerationConfiguration
import io.michaelrocks.lightsaber.processor.generation.model.InjectorConfigurator
import io.michaelrocks.lightsaber.processor.generation.model.MembersInjector
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.io.FileSink
import io.michaelrocks.lightsaber.processor.model.InjectionConfiguration
import io.michaelrocks.lightsaber.processor.model.Module
import org.objectweb.asm.Type
import java.util.*

class Generator(
    private val processorContext: ProcessorContext,
    fileSink: FileSink
) {
  private val classProducer = ProcessorClassProducer(fileSink, processorContext)
  private val annotationCreator = AnnotationCreator(processorContext, classProducer)

  fun generate(injectionConfiguration: InjectionConfiguration) {
    val generationConfiguration = composeGeneratorModel(injectionConfiguration)
    generateProviders(injectionConfiguration)
    generateLightsaberConfigurator(generationConfiguration)
    generateInjectorConfigurators(generationConfiguration)
    generateInjectors(generationConfiguration)
    generatePackageInvaders(generationConfiguration)
  }

  private fun composeGeneratorModel(configuration: InjectionConfiguration) =
      GenerationConfiguration(
          composePackageInjectorConfigurators(configuration),
          composeInjectorConfigurators(configuration),
          composeMembersInjectors(configuration),
          composePackageInvaders(configuration)
      )

  private fun composePackageInjectorConfigurators(configuration: InjectionConfiguration) =
      configuration.packageModules.map { module ->
        val configuratorType = composeConfiguratorType(module)
        InjectorConfigurator(configuratorType, module)
      }

  private fun composeInjectorConfigurators(configuration: InjectionConfiguration) =
      configuration.modules.map { module ->
        val configuratorType = composeConfiguratorType(module)
        InjectorConfigurator(configuratorType, module)
      }

  private fun composeConfiguratorType(module: Module): Type {
    val moduleNameWithDollars = module.type.internalName.replace('/', '$')
    return Type.getObjectType("io/michaelrocks/lightsaber/InjectorConfigurator\$$moduleNameWithDollars")
  }

  private fun composeMembersInjectors(configuration: InjectionConfiguration) =
      configuration.injectableTargets.map { injectableTarget ->
        val injectorType = Type.getObjectType(injectableTarget.type.internalName + "\$MembersInjector")
        MembersInjector(injectorType, injectableTarget)
      }

  private fun composePackageInvaders(configuration: InjectionConfiguration): Collection<PackageInvader> {
    val builders = HashMap<String, PackageInvader.Builder>()
    for (module in configuration.allModules) {
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

  private fun generateProviders(injectionConfiguration: InjectionConfiguration) {
    val generator = ProvidersGenerator(classProducer, processorContext.classRegistry, annotationCreator)
    generator.generate(injectionConfiguration)
  }

  private fun generateLightsaberConfigurator(generationConfiguration: GenerationConfiguration) {
    val generator = LightsaberRegistryClassGenerator(classProducer, processorContext.classRegistry)
    generator.generate(generationConfiguration)
  }

  private fun generateInjectorConfigurators(generationConfiguration: GenerationConfiguration) {
    val generator = InjectorConfiguratorsGenerator(classProducer, processorContext.classRegistry, annotationCreator)
    generator.generate(generationConfiguration)
  }

  private fun generateInjectors(generationConfiguration: GenerationConfiguration) {
    val typeAgentsGenerator = TypeAgentsGenerator(classProducer, processorContext.classRegistry, annotationCreator)
    typeAgentsGenerator.generate(generationConfiguration)
  }

  private fun generatePackageInvaders(generationConfiguration: GenerationConfiguration) {
    val packageInvadersGenerator = PackageInvadersGenerator(classProducer, processorContext.classRegistry)
    packageInvadersGenerator.generate(generationConfiguration)
  }
}
