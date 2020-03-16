/*
 * Copyright 2019 Michael Rozumyanskiy
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
import io.michaelrocks.grip.FileRegistry
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.grip.mirrors.isPublic
import io.michaelrocks.grip.mirrors.packageName
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.associateByIndexedNotNullTo
import io.michaelrocks.lightsaber.processor.commons.associateByIndexedTo
import io.michaelrocks.lightsaber.processor.commons.boxed
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.GenerationContext
import io.michaelrocks.lightsaber.processor.generation.model.Key
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import java.util.HashMap

class GenerationContextFactory(
  private val fileRegistry: FileRegistry,
  private val classRegistry: ClassRegistry,
  private val projectName: String
) {

  fun createGenerationContext(injectionContext: InjectionContext): GenerationContext {
    val dependencies = findAllDependencies(injectionContext)
    return GenerationContext(
      composePackageInvaders(dependencies),
      composeKeyRegistry(dependencies)
    )
  }

  private fun findAllDependencies(context: InjectionContext): Collection<Dependency> {
    return context.components.asSequence()
      .flatMap { it.modules.asSequence() }
      .flatMap { it.providers.asSequence() }
      .map { it.dependency }
      .toSet()
  }

  private fun composePackageInvaders(dependencies: Collection<Dependency>): Collection<PackageInvader> {
    return dependencies
      .flatMap { extractObjectTypes(it.type) }
      .distinct()
      .filterNot { isPublicType(it) }
      .groupByTo(
        HashMap(),
        { extractPackageName(it) },
        { it }
      )
      .map {
        val (packageName, types) = it
        val packageInvaderType =
          createUniqueObjectTypeByInternalName("$packageName/Lightsaber\$PackageInvader\$$projectName")
        val fields = types.associateByIndexedTo(
          HashMap(),
          { _, type -> type },
          { index, _ -> FieldDescriptor("class$index", Types.CLASS_TYPE) }
        )
        PackageInvader(packageInvaderType, packageName, fields)
      }
  }

  private fun extractObjectTypes(type: GenericType): List<Type> {
    return when (type) {
      is GenericType.Raw -> extractObjectTypes(type.type)
      is GenericType.TypeVariable -> extractObjectTypes(type.classBound) + type.interfaceBounds.flatMap { extractObjectTypes(it) }
      is GenericType.Array -> extractObjectTypes(type.elementType)
      is GenericType.Parameterized -> listOf(type.type) + type.typeArguments.flatMap { extractObjectTypes(it) }
      is GenericType.Inner -> extractObjectTypes(type.ownerType) + extractObjectTypes(type.type)
      is GenericType.UpperBounded -> extractObjectTypes(type.upperBound)
      is GenericType.LowerBounded -> extractObjectTypes(type.lowerBound)
    }
  }

  private fun extractObjectTypes(type: Type): List<Type> {
    return when (type) {
      is Type.Primitive -> emptyList()
      else -> listOf(type)
    }
  }

  private fun isPublicType(type: Type): Boolean {
    return when (type) {
      is Type.Primitive -> true
      is Type.Array -> isPublicType(type.elementType)
      is Type.Object -> classRegistry.getClassMirror(type).isPublic
      is Type.Method -> error("Method handles aren't supported")
    }
  }

  private fun extractPackageName(type: Type): String {
    return when (type) {
      is Type.Primitive -> error("Cannot get a package for a primitive type $type")
      is Type.Array -> extractPackageName(type.elementType)
      is Type.Object -> type.packageName
      is Type.Method -> error("Method handles aren't supported")
    }
  }

  private fun composeKeyRegistry(dependencies: Collection<Dependency>): KeyRegistry {
    val type = createUniqueObjectTypeByInternalName("io/michaelrocks/lightsaber/KeyRegistry\$$projectName")
    val keys = dependencies.associateByIndexedNotNullTo(
      HashMap(),
      { _, dependency -> dependency.boxed() },
      { index, dependency -> maybeComposeKey("key$index", dependency) }
    )
    return KeyRegistry(type, keys)
  }

  private fun maybeComposeKey(name: String, dependency: Dependency): Key? {
    return when {
      dependency.qualifier != null -> Key.QualifiedType(FieldDescriptor(name, Types.KEY_TYPE))
      dependency.type !is GenericType.Raw -> Key.Type(FieldDescriptor(name, Types.TYPE_TYPE))
      else -> null
    }
  }

  private fun createUniqueObjectTypeByInternalName(internalName: String): Type.Object {
    val type = getObjectTypeByInternalName(internalName)
    return if (type !in fileRegistry) type else createUniqueObjectTypeByInternalName(internalName, 0)
  }

  private tailrec fun createUniqueObjectTypeByInternalName(internalName: String, index: Int): Type.Object {
    val type = getObjectTypeByInternalName(internalName + index)
    return if (type !in fileRegistry) type else createUniqueObjectTypeByInternalName(internalName, index + 1)
  }
}
