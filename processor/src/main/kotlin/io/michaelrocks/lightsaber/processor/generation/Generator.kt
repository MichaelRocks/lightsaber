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
import io.michaelrocks.grip.mirrors.isPublic
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.*
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.*
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
    val generationContext = composeGeneratorModel(injectionContext)
    generateProviders(injectionContext, generationContext)
    generateLightsaberConfigurator(generationContext)
    generateInjectorConfigurators(generationContext)
    generateInjectors(generationContext)
    generatePackageInvaders(generationContext)
    generateKeyRegistry(generationContext)
  }

  private fun composeGeneratorModel(context: InjectionContext) =
      GenerationContext(
          composePackageInjectorConfigurators(context),
          composeInjectorConfigurators(context),
          composeMembersInjectors(context),
          composePackageInvaders(context),
          composeKeyRegistry(context)
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

  private fun composePackageInvaders(context: InjectionContext): Collection<PackageInvader> =
      context.allModules.asSequence()
          .flatMap { module -> module.providers.asSequence() }
          .asIterable()
          .groupNotNullByTo(
              HashMap(),
              { provider -> Types.getPackageName(provider.moduleType) },
              { provider ->
                val type = provider.dependency.type.rawType.box()
                given (!classRegistry.getClassMirror(type).isPublic) { type }
              }
          )
          .map {
            val (packageName, types) = it
            val type = Type.getObjectType("$packageName/Lightsaber\$PackageInvader")
            val fields = types.associateByIndexedTo(HashMap(),
                { index, type -> type.box() },
                { index, type -> FieldDescriptor("class$index", Types.CLASS_TYPE) }
            )
            PackageInvader(type, packageName, fields)
          }

  private fun composeKeyRegistry(context: InjectionContext): KeyRegistry {
    val type = Type.getObjectType("io/michaelrocks/lightsaber/KeyRegistry")
    val fields = context.allModules.asSequence()
        .flatMap { module -> module.providers.asSequence() }
        .asIterable()
        .associateByIndexedTo(
            HashMap(),
            { index, provider -> provider.dependency.box() },
            { index, provider -> FieldDescriptor("key$index", Types.KEY_TYPE) }
        )
    return KeyRegistry(type, fields)
  }

  private fun generateProviders(injectionContext: InjectionContext, generationContext: GenerationContext) {
    val generator = ProvidersGenerator(classProducer, classRegistry)
    generator.generate(injectionContext, generationContext)
  }

  private fun generateLightsaberConfigurator(generationContext: GenerationContext) {
    val generator = LightsaberRegistryClassGenerator(classProducer, classRegistry)
    generator.generate(generationContext)
  }

  private fun generateInjectorConfigurators(generationContext: GenerationContext) {
    val generator = InjectorConfiguratorsGenerator(classProducer, classRegistry)
    generator.generate(generationContext)
  }

  private fun generateInjectors(generationContext: GenerationContext) {
    val typeAgentsGenerator = MembersInjectorsGenerator(classProducer, classRegistry)
    typeAgentsGenerator.generate(generationContext)
  }

  private fun generatePackageInvaders(generationContext: GenerationContext) {
    val packageInvadersGenerator = PackageInvadersGenerator(classProducer, classRegistry)
    packageInvadersGenerator.generate(generationContext)
  }

  private fun generateKeyRegistry(generationContext: GenerationContext) {
    val generator = KeyRegistryClassGenerator(classProducer, classRegistry, annotationCreator, generationContext)
    generator.generate()
  }
}
