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
import io.michaelrocks.lightsaber.processor.generation.model.MembersInjector
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.io.FileSink
import org.objectweb.asm.Type
import java.util.*

class Generator(
    private val processorContext: ProcessorContext,
    fileSink: FileSink
) {
  private val classProducer = ProcessorClassProducer(fileSink, processorContext)
  private val annotationCreator = AnnotationCreator(processorContext, classProducer)

  fun generate() {
    composeGeneratorModel()
    generateProviders()
    generateLightsaberConfigurator()
    generateInjectorConfigurators()
    generateInjectors()
    generatePackageInvaders()
  }

  private fun composeGeneratorModel() {
    composeInjectors()
    composePackageInvaders()
  }

  private fun composeInjectors() {
    for (injectableTarget in processorContext.getInjectableTargets()) {
      val injectorType = Type.getObjectType(injectableTarget.type.internalName + "\$MembersInjector")
      val injector = MembersInjector(injectorType, injectableTarget)
      processorContext.addMembersInjector(injector)
    }
  }

  private fun composePackageInvaders() {
    val builders = HashMap<String, PackageInvader.Builder>()
    for (module in processorContext.allModules) {
      for (provider in module.providers) {
        val providableType = provider.dependency.type.rawType.box()
        val packageName = Types.getPackageName(module.type)
        val builder = builders[packageName] ?: PackageInvader.Builder(packageName).apply {
          builders.put(packageName, this)
        }
        builder.addClass(providableType)
      }
    }

    for (builder in builders.values) {
      processorContext.addPackageInvader(builder.build())
    }
  }


  private fun generateProviders() {
    val providersGenerator = ProvidersGenerator(classProducer, processorContext, annotationCreator)
    providersGenerator.generateProviders()
  }

  private fun generateLightsaberConfigurator() {
    val lightsaberRegistryClassGenerator = LightsaberRegistryClassGenerator(classProducer, processorContext)
    lightsaberRegistryClassGenerator.generateLightsaberRegistry()
  }

  private fun generateInjectorConfigurators() {
    val injectorConfiguratorsGenerator = InjectorConfiguratorsGenerator(classProducer, processorContext,
        annotationCreator)
    injectorConfiguratorsGenerator.generateInjectorConfigurators()
  }

  private fun generateInjectors() {
    val typeAgentsGenerator = TypeAgentsGenerator(classProducer, processorContext, annotationCreator)
    typeAgentsGenerator.generateInjectors()
  }

  private fun generatePackageInvaders() {
    val packageInvadersGenerator = PackageInvadersGenerator(classProducer, processorContext)
    packageInvadersGenerator.generatePackageInvaders()
  }
}
