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

package io.michaelrocks.lightsaber.processor.analysis

import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.annotatedWith
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.lightsaber.ImportedBy
import io.michaelrocks.lightsaber.ProvidedBy
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.model.Factory
import io.michaelrocks.lightsaber.processor.model.InjectionTarget
import io.michaelrocks.lightsaber.processor.model.Module
import java.io.File
import java.util.HashMap

interface ModuleRegistry {
  fun getModule(moduleType: Type.Object): Module
}

class ModuleRegistryImpl(
  private val grip: Grip,
  private val moduleParser: ModuleParser,
  private val errorReporter: ErrorReporter,
  providableTargets: Collection<InjectionTarget>,
  factories: Collection<Factory>,
  files: Collection<File>
) : ModuleRegistry {

  private val externals by lazy(LazyThreadSafetyMode.NONE) {
    val modulesQuery = grip select classes from files where annotatedWith(Types.MODULE_TYPE)
    val modules = modulesQuery.execute().classes

    val defaultModuleTypes = modules.mapNotNull { mirror ->
      val annotation = checkNotNull(mirror.annotations[Types.MODULE_TYPE])
      if (annotation.values[io.michaelrocks.lightsaber.Module::isDefault.name] == true) mirror.type else null
    }

    Externals(
      importeeModulesByImporterModules = groupImporteeModulesByImporterModules(modules),
      providableTargetsByModules = groupProvidableTargetsByModules(providableTargets, defaultModuleTypes),
      factoriesByModules = groupFactoriesByModules(factories, defaultModuleTypes)
    )
  }

  private val modulesByTypes = HashMap<Type.Object, Module>()

  private val moduleTypeStack = ArrayList<Type.Object>()

  override fun getModule(moduleType: Type.Object): Module {
    return withModuleTypeInStack(moduleType) {
      maybeParseModule(moduleType)
    }
  }

  private fun groupImporteeModulesByImporterModules(modules: Collection<ClassMirror>): Map<Type.Object, Collection<Type.Object>> {
    return HashMap<Type.Object, MutableList<Type.Object>>().also { importeeModulesByImporterModules ->
      modules.forEach { importee ->
        extractImporterModulesFromModule(importee).forEach { importerType ->
          importeeModulesByImporterModules.getOrPut(importerType, ::ArrayList).add(importee.type)
        }
      }
    }
  }

  private fun extractImporterModulesFromModule(importee: ClassMirror): List<Type.Object> {
    val annotation = importee.annotations[Types.IMPORTED_BY_TYPE] ?: return emptyList()
    val importerTypes = annotation.values[ImportedBy::value.name] as List<*>

    if (importerTypes.isEmpty()) {
      errorReporter.reportError("Module ${importee.type.className} should be imported by at least one module")
      return emptyList()
    } else {
      return importerTypes.mapNotNull {
        val importerType = it as? Type.Object
        if (importerType == null) {
          errorReporter.reportError("A non-class type is specified in @ProvidedBy annotation for ${importee.type.className}")
          return@mapNotNull null
        }

        val importer = grip.classRegistry.getClassMirror(importerType)
        if (Types.MODULE_TYPE !in importer.annotations && Types.COMPONENT_TYPE !in importer.annotations) {
          errorReporter.reportError("Module ${importee.type.className} is imported by ${importerType.className}, which isn't a module")
          return@mapNotNull null
        }

        importerType
      }
    }
  }

  private fun groupProvidableTargetsByModules(
    providableTargets: Collection<InjectionTarget>,
    defaultModuleTypes: Collection<Type.Object>
  ): Map<Type.Object, List<InjectionTarget>> {
    return HashMap<Type.Object, MutableList<InjectionTarget>>().also { providableTargetsByModule ->
      providableTargets.forEach { target ->
        val mirror = grip.classRegistry.getClassMirror(target.type)
        val providedByAnnotation = mirror.annotations[Types.PROVIDED_BY_TYPE]
        val moduleTypes = if (providedByAnnotation != null) providedByAnnotation.values[ProvidedBy::value.name] as List<*> else defaultModuleTypes

        if (moduleTypes.isEmpty()) {
          errorReporter.reportError(
            "Class ${target.type.className} should be bounds to at least one module. " +
                "You can annotate it with @ProvidedBy with a module list " +
                "or make some of your modules default with @Module(isDefault = true)"
          )
        } else {
          moduleTypes.forEach { moduleType ->
            if (moduleType is Type.Object) {
              providableTargetsByModule.getOrPut(moduleType, ::ArrayList).add(target)
            } else {
              errorReporter.reportError(
                "A non-class type is specified in @ProvidedBy annotation for ${mirror.type.className}"
              )
            }
          }
        }
      }
    }
  }

  private fun groupFactoriesByModules(
    factories: Collection<Factory>,
    defaultModuleTypes: Collection<Type.Object>
  ): Map<Type.Object, List<Factory>> {
    return HashMap<Type.Object, MutableList<Factory>>().also { factoriesByModule ->
      factories.forEach { factory ->
        val mirror = grip.classRegistry.getClassMirror(factory.type)
        val providedByAnnotation = mirror.annotations[Types.PROVIDED_BY_TYPE]
        val moduleTypes = if (providedByAnnotation != null) providedByAnnotation.values[ProvidedBy::value.name] as List<*> else defaultModuleTypes

        if (moduleTypes.isEmpty()) {
          errorReporter.reportError(
            "Class ${factory.type.className} should be bound to at least one module. " +
                "You can annotate it with @ProvidedBy with a module list " +
                "or make some of your modules default with @Module(isDefault = true)"
          )
        } else {
          moduleTypes.forEach { moduleType ->
            if (moduleType is Type.Object) {
              factoriesByModule.getOrPut(moduleType, ::ArrayList).add(factory)
            } else {
              errorReporter.reportError("A non-class type is specified in @ProvidedBy annotation for ${mirror.type.className}")
            }
          }
        }
      }
    }
  }

  private fun maybeParseModule(moduleType: Type.Object): Module {
    val externals = externals
    val importeeModuleTypes = externals.importeeModulesByImporterModules[moduleType].orEmpty()
    val providableTargetsForModuleType = externals.providableTargetsByModules[moduleType].orEmpty()
    val factoriesForModuleType = externals.factoriesByModules[moduleType].orEmpty()
    return modulesByTypes.getOrPut(moduleType) {
      moduleParser.parseModule(moduleType, importeeModuleTypes, providableTargetsForModuleType, factoriesForModuleType, this)
    }
  }

  private inline fun <T : Any> withModuleTypeInStack(moduleType: Type.Object, action: () -> T): T {
    moduleTypeStack += moduleType
    return try {
      if (moduleTypeStack.indexOf(moduleType) == moduleTypeStack.lastIndex) {
        action()
      } else {
        val cycle = moduleTypeStack.joinToString(" -> ") { it.className }
        throw ModuleParserException("Module cycle: $cycle")
      }
    } finally {
      val removedModuleType = moduleTypeStack.removeAt(moduleTypeStack.lastIndex)
      check(removedModuleType === moduleType)
    }
  }

  private class Externals(
    val importeeModulesByImporterModules: Map<Type.Object, Collection<Type.Object>>,
    val providableTargetsByModules: Map<Type.Object, Collection<InjectionTarget>>,
    val factoriesByModules: Map<Type.Object, Collection<Factory>>
  )
}
