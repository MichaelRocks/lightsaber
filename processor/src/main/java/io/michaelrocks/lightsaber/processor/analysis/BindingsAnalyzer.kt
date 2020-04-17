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
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.ProvidedAs
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.model.Binding
import io.michaelrocks.lightsaber.processor.model.Dependency
import java.io.File

interface BindingsAnalyzer {
  fun analyze(files: Collection<File>): BindingRegistry
}

class BindingsAnalyzerImpl(
  private val grip: Grip,
  private val analyzerHelper: AnalyzerHelper,
  private val errorReporter: ErrorReporter
) : BindingsAnalyzer {

  override fun analyze(files: Collection<File>): BindingRegistry {
    val bindingRegistry = BindingRegistryImpl()
    val bindingsQuery = grip select classes from files where annotatedWith(Types.PROVIDED_AS_TYPE)
    bindingsQuery.execute().classes.forEach { mirror ->
      createBindingsForClass(mirror).forEach { binding ->
        bindingRegistry.registerBinding(binding)
      }
    }

    return bindingRegistry
  }

  private fun createBindingsForClass(mirror: ClassMirror): Collection<Binding> {
    return extractAncestorTypesFromClass(mirror).map { ancestorType ->
      val dependency = Dependency(GenericType.Raw(mirror.type))
      val qualifier = analyzerHelper.findQualifier(mirror)
      val ancestor = Dependency(GenericType.Raw(ancestorType), qualifier)
      Binding(dependency, ancestor)
    }
  }

  private fun extractAncestorTypesFromClass(mirror: ClassMirror): Collection<Type.Object> {
    val providedAs = mirror.annotations[Types.PROVIDED_AS_TYPE] ?: return emptyList()

    @Suppress("UNCHECKED_CAST")
    val ancestorTypes = providedAs.values[ProvidedAs::value.name] as? List<*>
    if (ancestorTypes == null) {
      error("Class ${mirror.type.className} has invalid type in its @ProvidedAs annotation: $ancestorTypes")
      return emptyList()
    }

    return ancestorTypes.mapNotNull {
      val ancestorType = it as? Type.Object
      if (ancestorType == null) {
        error("Class ${mirror.type.className} has a non-class type in its @ProvidedAs annotation: $it")
        return@mapNotNull null
      }

      ancestorType
    }
  }

  private fun error(message: String) {
    errorReporter.reportError(message)
  }
}
