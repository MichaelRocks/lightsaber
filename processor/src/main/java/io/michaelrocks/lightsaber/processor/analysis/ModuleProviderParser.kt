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
import io.michaelrocks.grip.and
import io.michaelrocks.grip.annotatedWith
import io.michaelrocks.grip.fields
import io.michaelrocks.grip.from
import io.michaelrocks.grip.methodType
import io.michaelrocks.grip.methods
import io.michaelrocks.grip.mirrors.Annotated
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.not
import io.michaelrocks.grip.or
import io.michaelrocks.grip.returns
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.ProcessingException
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.ModuleProvider
import io.michaelrocks.lightsaber.processor.model.ModuleProvisionPoint

interface ModuleProviderParser {
  fun parseModuleProviders(mirror: ClassMirror, moduleRegistry: ModuleRegistry, isComponentDefaultModule: Boolean): Collection<ModuleProvider>
}

class ModuleProviderParserImpl(
  private val grip: Grip,
  private val errorReporter: ErrorReporter
) : ModuleProviderParser {

  private val logger = getLogger()

  override fun parseModuleProviders(
    mirror: ClassMirror,
    moduleRegistry: ModuleRegistry,
    isComponentDefaultModule: Boolean
  ): Collection<ModuleProvider> {
    val isImportable = createImportAnnotationMatcher(includeProvidesAnnotation = !isComponentDefaultModule)
    val methodsQuery = grip select methods from mirror where (isImportable and methodType(not(returns(Type.Primitive.Void))))
    val fieldsQuery = grip select fields from mirror where isImportable

    val kind = if (isComponentDefaultModule) "Component" else "Module"
    logger.debug("{}: {}", kind, mirror.type.className)
    val methods = methodsQuery.execute()[mirror.type].orEmpty().mapNotNull { method ->
      logger.debug("  Method: {}", method)
      tryParseModuleProvider(method, moduleRegistry)
    }

    val fields = fieldsQuery.execute()[mirror.type].orEmpty().mapNotNull { field ->
      logger.debug("  Field: {}", field)
      tryParseModuleProvider(field, moduleRegistry)
    }

    return methods + fields
  }

  private fun createImportAnnotationMatcher(includeProvidesAnnotation: Boolean): (Grip, Annotated) -> Boolean {
    val annotatedWithImport = annotatedWith(Types.IMPORT_TYPE)
    return if (includeProvidesAnnotation) annotatedWithImport or annotatedWith(Types.PROVIDES_TYPE) else annotatedWithImport
  }

  private fun tryParseModuleProvider(method: MethodMirror, moduleRegistry: ModuleRegistry): ModuleProvider? {
    val module = tryParseModule(method.signature.returnType, moduleRegistry) ?: return null
    return ModuleProvider(module, ModuleProvisionPoint.Method(method))
  }

  private fun tryParseModuleProvider(field: FieldMirror, moduleRegistry: ModuleRegistry): ModuleProvider? {
    val module = tryParseModule(field.signature.type, moduleRegistry) ?: return null
    return ModuleProvider(module, ModuleProvisionPoint.Field(field))
  }

  private fun tryParseModule(generic: GenericType, moduleRegistry: ModuleRegistry): Module? {
    if (generic !is GenericType.Raw) {
      errorReporter.reportError("Module provider cannot have a generic type: $generic")
      return null
    }

    val type = generic.type
    if (type !is Type.Object) {
      errorReporter.reportError("Module provider cannot have an array type: $generic")
      return null
    }

    return try {
      moduleRegistry.getModule(type)
    } catch (exception: ProcessingException) {
      errorReporter.reportError(exception)
      null
    }
  }
}

