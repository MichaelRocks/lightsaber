/*
 * Copyright 2017 Michael Rozumyanskiy
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
import io.michaelrocks.grip.mirrors.isPublic
import io.michaelrocks.grip.mirrors.packageName
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.associateByIndexedTo
import io.michaelrocks.lightsaber.processor.commons.boxedOrElementType
import io.michaelrocks.lightsaber.processor.commons.given
import io.michaelrocks.lightsaber.processor.commons.groupNotNullByTo
import io.michaelrocks.lightsaber.processor.commons.mergeWith
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.GenerationContext
import io.michaelrocks.lightsaber.processor.generation.model.InjectorConfigurator
import io.michaelrocks.lightsaber.processor.generation.model.Key
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.generation.model.MembersInjector
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.io.FileSink
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import java.util.HashMap

class Generator(
    private val classRegistry: ClassRegistry,
    private val errorReporter: ErrorReporter,
    fileSink: FileSink,
    sourceSink: FileSink
) {
  private val classProducer = ProcessorClassProducer(fileSink, errorReporter)
  private val sourceProducer = ProcessorSourceProducer(sourceSink, errorReporter)
  private val annotationCreator = AnnotationCreator(classProducer, classRegistry)

  fun generate(injectionContext: InjectionContext) {
    val generationContext = composeGeneratorModel(injectionContext)
    generateProviders(injectionContext, generationContext)
    generateInjectorConfigurators(generationContext)
    generateInjectors(generationContext)
    generatePackageInvaders(generationContext)
    generateKeyRegistry(generationContext)
    generateInjectionDispatcher(generationContext)
  }

  private fun composeGeneratorModel(context: InjectionContext) =
      GenerationContext(
          composePackageInjectorConfigurator(context),
          composeInjectorConfigurators(context),
          composeMembersInjectors(context),
          composePackageInvaders(context),
          composeKeyRegistry(context)
      )

  private fun composePackageInjectorConfigurator(context: InjectionContext): InjectorConfigurator {
    val configuratorType = composeConfiguratorType(context.packageComponent)
    return InjectorConfigurator(configuratorType, context.packageComponent)
  }

  private fun composeInjectorConfigurators(context: InjectionContext): Collection<InjectorConfigurator> {
    return context.components
        .map { component ->
          val configuratorType = composeConfiguratorType(component)
          InjectorConfigurator(configuratorType, component)
        }
  }

  private fun composeConfiguratorType(component: Component): Type.Object {
    val componentNameWithDollars = component.type.internalName.replace('/', '$')
    return getObjectTypeByInternalName("io/michaelrocks/lightsaber/InjectorConfigurator\$$componentNameWithDollars")
  }

  private fun composeMembersInjectors(context: InjectionContext): Collection<MembersInjector> {
    return context.injectableTargets.map { injectableTarget ->
      val injectorType = getObjectTypeByInternalName(injectableTarget.type.internalName + "\$MembersInjector")
      MembersInjector(injectorType, injectableTarget)
    }
  }

  private fun composePackageInvaders(context: InjectionContext): Collection<PackageInvader> =
      context.allComponents.asSequence()
          .flatMap { it.modules.asSequence() }
          .flatMap { it.providers.asSequence() }
          .asIterable()
          .groupNotNullByTo(
              HashMap(),
              { provider -> provider.moduleType.packageName },
              { provider ->
                val type = provider.dependency.type.rawType
                given (!classRegistry.getClassMirror(type.boxedOrElementType()).isPublic) { type }
              }
          )
          .mergeWith(
              context.components.groupNotNullByTo(
                  HashMap<String, MutableList<Type>>(),
                  { component -> component.type.packageName },
                  { component ->
                    given (!classRegistry.getClassMirror(component.type).isPublic) { component.type }
                  }
              )
          )
          .mergeWith(
              context.injectableTargets.groupNotNullByTo(
                  HashMap(),
                  { target -> target.type.packageName },
                  { target ->
                    given (!classRegistry.getClassMirror(target.type).isPublic) { target.type }
                  }
              )
          )
          .map {
            val (packageName, types) = it
            val type = getObjectTypeByInternalName("$packageName/Lightsaber\$PackageInvader")
            val fields = types.associateByIndexedTo(HashMap(),
                { _, type -> type },
                { index, _ -> FieldDescriptor("class$index", Types.CLASS_TYPE) }
            )
            PackageInvader(type, packageName, fields)
          }

  private fun composeKeyRegistry(context: InjectionContext): KeyRegistry {
    val type = getObjectTypeByInternalName("io/michaelrocks/lightsaber/KeyRegistry")
    val keys = context.allComponents.asSequence()
        .flatMap { it.modules.asSequence() }
        .flatMap { it.providers.asSequence() }
        .asIterable()
        .associateByIndexedTo(
            HashMap(),
            { _, provider -> provider.dependency.box() },
            { index, provider -> composeKey("key$index", provider.dependency) }
        )
    val injectorDependency = Dependency(GenericType.Raw(Types.INJECTOR_TYPE))
    keys.put(injectorDependency, composeKey("injectorKey", injectorDependency))
    return KeyRegistry(type, keys)
  }

  private fun composeKey(name: String, dependency: Dependency): Key {
    return when {
      dependency.qualifier != null -> Key.QualifiedType(FieldDescriptor(name, Types.KEY_TYPE))
      dependency.type is GenericType.Raw -> Key.Class(FieldDescriptor(name, Types.CLASS_TYPE))
      else -> Key.Type(FieldDescriptor(name, Types.TYPE_TYPE))
    }
  }

  private fun generateProviders(injectionContext: InjectionContext, generationContext: GenerationContext) {
    val generator = ProvidersGenerator(classProducer, classRegistry)
    generator.generate(injectionContext, generationContext)
  }

  private fun generateInjectorConfigurators(generationContext: GenerationContext) {
    val generator = InjectorConfiguratorsGenerator(classProducer, classRegistry)
    generator.generate(generationContext)
  }

  private fun generateInjectors(generationContext: GenerationContext) {
    val generator = MembersInjectorsGenerator(classProducer, classRegistry)
    generator.generate(generationContext)
  }

  private fun generatePackageInvaders(generationContext: GenerationContext) {
    val generator = PackageInvadersGenerator(classProducer, classRegistry)
    generator.generate(generationContext)
  }

  private fun generateKeyRegistry(generationContext: GenerationContext) {
    val generator = KeyRegistryClassGenerator(classProducer, classRegistry, annotationCreator, generationContext)
    generator.generate()
  }

  private fun generateInjectionDispatcher(generationContext: GenerationContext) {
    val generator = InjectorDispatcherSourceGenerator(sourceProducer, classRegistry)
    generator.generate(generationContext)
  }
}
