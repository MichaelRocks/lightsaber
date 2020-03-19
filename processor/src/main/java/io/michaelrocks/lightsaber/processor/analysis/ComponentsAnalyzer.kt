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
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.graph.DirectedGraph
import io.michaelrocks.lightsaber.processor.graph.HashDirectedGraph
import io.michaelrocks.lightsaber.processor.graph.reversed
import io.michaelrocks.lightsaber.processor.model.Component
import java.io.File

interface ComponentsAnalyzer {
  fun analyze(files: Collection<File>): Collection<Component>
}

class ComponentsAnalyzerImpl(
  private val grip: Grip,
  private val moduleRegistry: ModuleRegistry,
  private val moduleProviderParser: ModuleProviderParser,
  private val errorReporter: ErrorReporter
) : ComponentsAnalyzer {

  override fun analyze(files: Collection<File>): Collection<Component> {
    val componentsQuery = grip select classes from files where annotatedWith(Types.COMPONENT_TYPE)
    val graph = buildComponentGraph(componentsQuery.execute().types)
    val reversedGraph = graph.reversed()
    return graph.vertices
      .filterNot { it == Types.COMPONENT_NONE_TYPE }
      .map { type ->
        val mirror = grip.classRegistry.getClassMirror(type)
        val parent = reversedGraph.getAdjacentVertices(type)?.first()?.takeIf { it != Types.COMPONENT_NONE_TYPE }
        val subcomponents = graph.getAdjacentVertices(type).orEmpty().toList()
        val providers = moduleProviderParser.parseModuleProviders(mirror, moduleRegistry, includeProvidesAnnotation = true)
        Component(type, providers, parent, subcomponents)
      }
  }

  private fun buildComponentGraph(types: Collection<Type.Object>): DirectedGraph<Type.Object> {
    val graph = HashDirectedGraph<Type.Object>()

    for (type in types) {
      val mirror = grip.classRegistry.getClassMirror(type)
      if (mirror.signature.typeVariables.isNotEmpty()) {
        errorReporter.reportError("Component cannot have a type parameters: ${type.className}")
        continue
      }

      val annotation = mirror.annotations[Types.COMPONENT_TYPE]
      if (annotation == null) {
        errorReporter.reportError("Class ${type.className} is not a component")
        continue
      }

      val parent = annotation.values["parent"] as Type?
      if (parent != null && parent != Types.COMPONENT_NONE_TYPE) {
        if (parent is Type.Object) {
          graph.put(parent, type)
        } else {
          errorReporter.reportError("Parent component of ${type.className} is not a class")
        }
      } else {
        graph.put(Types.COMPONENT_NONE_TYPE, type)
      }
    }

    return graph
  }
}
