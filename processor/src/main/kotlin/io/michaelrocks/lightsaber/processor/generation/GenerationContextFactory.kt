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
import io.michaelrocks.grip.mirrors.isPublic
import io.michaelrocks.grip.mirrors.packageName
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.associateByIndexedTo
import io.michaelrocks.lightsaber.processor.commons.boxedOrElementType
import io.michaelrocks.lightsaber.processor.commons.given
import io.michaelrocks.lightsaber.processor.commons.groupNotNullByTo
import io.michaelrocks.lightsaber.processor.commons.mergeWith
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.GenerationContext
import io.michaelrocks.lightsaber.processor.generation.model.Key
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import java.util.HashMap

class GenerationContextFactory(
    private val classRegistry: ClassRegistry
) {
  fun createGenerationContext(injectionContext: InjectionContext): GenerationContext {
    return GenerationContext(
        composePackageInvaders(injectionContext),
        composeKeyRegistry(injectionContext)
    )
  }

  private fun composePackageInvaders(context: InjectionContext): Collection<PackageInvader> =
      context.components.asSequence()
          .flatMap { it.modules.asSequence() }
          .flatMap { it.providers.asSequence() }
          .asIterable()
          .groupNotNullByTo(
              HashMap(),
              { provider -> provider.moduleType.packageName },
              { provider ->
                val type = provider.dependency.type.rawType
                given(!classRegistry.getClassMirror(type.boxedOrElementType()).isPublic) { type }
              }
          )
          .mergeWith(
              context.components.groupNotNullByTo(
                  HashMap<String, MutableList<Type>>(),
                  { component -> component.type.packageName },
                  { component ->
                    given(!classRegistry.getClassMirror(component.type).isPublic) { component.type }
                  }
              )
          )
          .mergeWith(
              context.injectableTargets.groupNotNullByTo(
                  HashMap(),
                  { target -> target.type.packageName },
                  { target ->
                    given(!classRegistry.getClassMirror(target.type).isPublic) { target.type }
                  }
              )
          )
          .map {
            val (packageName, types) = it
            val packageInvaderType = getObjectTypeByInternalName("$packageName/Lightsaber\$PackageInvader")
            val fields = types.associateByIndexedTo(
                HashMap(),
                { _, type -> type },
                { index, _ -> FieldDescriptor("class$index", Types.CLASS_TYPE) }
            )
            PackageInvader(packageInvaderType, packageName, fields)
          }

  private fun composeKeyRegistry(context: InjectionContext): KeyRegistry {
    val type = getObjectTypeByInternalName("io/michaelrocks/lightsaber/KeyRegistry")
    val keys = context.components.asSequence()
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
}
