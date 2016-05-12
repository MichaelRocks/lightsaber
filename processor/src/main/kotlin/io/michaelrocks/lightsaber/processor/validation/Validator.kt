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

package io.michaelrocks.lightsaber.processor.validation

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.graph.DirectedGraph
import io.michaelrocks.lightsaber.processor.graph.findCycles
import io.michaelrocks.lightsaber.processor.graph.findMissingVertices
import io.michaelrocks.lightsaber.processor.graph.reversed
import io.michaelrocks.lightsaber.processor.model.InjectionContext

class Validator(
    private val classRegistry: ClassRegistry,
    private val errorReporter: ErrorReporter
) {
  fun validate(context: InjectionContext) {
    performSanityChecks(context)
    validateComponentGraph(context)
    validateInjectionGraph(context)
  }

  private fun performSanityChecks(context: InjectionContext) {
    SanityChecker(classRegistry, errorReporter).performSanityChecks(context)
  }

  private fun validateComponentGraph(context: InjectionContext) {
    val componentGraph = buildComponentGraph(context.allComponents)
    val cycles = componentGraph.findCycles()
    for (cycle in cycles) {
      errorReporter.reportError("Cycled component: ${cycle.joinToString(" -> ")}")
    }
  }

  private fun validateInjectionGraph(context: InjectionContext) {
    val injectionGraphs = buildInjectionGraphs(context)
    injectionGraphs.forEach {
      validateNoDuplicatesInInjectionGraph(it)
      validateDependencyGraph(it)
    }

    val injectionGraph = buildInjectionGraph(injectionGraphs)
    context.components.forEach { component ->
      if (InjectionGraphVertex.ComponentVertex(component) !in injectionGraph) {
        errorReporter.reportError("Abandoned component: ${component.type.className}")
      }
    }
  }

  private fun validateDependencyGraph(injectionGraph: DirectedGraph<InjectionGraphVertex>) {
    val modules = injectionGraph.vertices.mapNotNull { (it as? InjectionGraphVertex.ModuleVertex)?.module }
    val dependencyGraph = buildDependencyGraph(modules)

    val unresolvedDependencies = dependencyGraph.findMissingVertices()
    for (unresolvedDependency in unresolvedDependencies) {
      errorReporter.reportError("Unresolved dependency: $unresolvedDependency")
    }

    val cycles = dependencyGraph.findCycles()
    for (cycle in cycles) {
      errorReporter.reportError("Cycled dependency: ${cycle.joinToString(" -> ")}")
    }
  }

  private fun validateNoDuplicatesInInjectionGraph(graph: DirectedGraph<InjectionGraphVertex>) {
    val reversed = graph.reversed()
    reversed.vertices.forEach { vertex ->
      val adjacent = reversed.getAdjacentVertices(vertex)
      if (adjacent != null && adjacent.size > 1) {
        when (vertex) {
          is InjectionGraphVertex.ComponentVertex -> {}
          is InjectionGraphVertex.ModuleVertex -> {
            val module = vertex.module.type.className
            val components =
                adjacent
                    .mapNotNull { it as? InjectionGraphVertex.ComponentVertex }
                    .joinToString { it.component.type.className }
            errorReporter.reportError(
                "Module provided multiple times in a component chain: $module in $components"
            )
          }
          is InjectionGraphVertex.DependencyVertex -> {
            val dependency = vertex.dependency.toString()
            val modules =
                adjacent
                    .mapNotNull { it as? InjectionGraphVertex.ModuleVertex }
                    .joinToString { it.module.type.className }
            errorReporter.reportError(
                "Dependency provided multiple times in a component chain: $dependency: $modules"
            )
          }
        }
      }
    }
  }
}
