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
import io.michaelrocks.lightsaber.processor.commons.cast
import io.michaelrocks.lightsaber.processor.generation.box
import io.michaelrocks.lightsaber.processor.graph.*
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import java.util.*

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
      validateDependencyGraph(context, it)
    }

    val injectionGraph = buildInjectionGraph(injectionGraphs)
    context.components.forEach { component ->
      if (InjectionGraphVertex.ComponentVertex(component) !in injectionGraph) {
        errorReporter.reportError("Abandoned component: ${component.type.className}")
      }
    }
  }

  private fun validateDependencyGraph(context: InjectionContext, injectionGraph: DirectedGraph<InjectionGraphVertex>) {
    val modules = injectionGraph.vertices.mapNotNull { (it as? InjectionGraphVertex.ModuleVertex)?.module }
    val dependencyGraph = buildDependencyGraph(modules)

    val packageComponent = InjectionGraphVertex.ComponentVertex(context.packageComponent)
    val componentChain = extractComponentChain(injectionGraph, packageComponent)
    val unresolvedDependencies =
        dependencyGraph.findUnresolvedDependencies(componentChain.subList(1, componentChain.size))
    if (unresolvedDependencies.isNotEmpty()) {
      val componentChainString = componentChain.joinToString(" -> ") { it.type.className }
      for (unresolvedDependency in unresolvedDependencies) {
        errorReporter.reportError("Unresolved dependency: $unresolvedDependency in $componentChainString")
      }
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

  private fun extractComponentChain(
      graph: DirectedGraph<InjectionGraphVertex>,
      componentVertex: InjectionGraphVertex.ComponentVertex
  ): List<Component> {
    val chain = ArrayList<Component>()
    val traversal = DepthFirstTraversal<InjectionGraphVertex>()
    val delegate = object : AbstractMarkingTraversal.SimpleDelegate<InjectionGraphVertex>() {
      override fun isVisited(vertex: InjectionGraphVertex): Boolean {
        return vertex !is InjectionGraphVertex.ComponentVertex || super.isVisited(vertex)
      }

      override fun onBeforeAdjacentVertices(vertex: InjectionGraphVertex) {
        chain += vertex.cast<InjectionGraphVertex.ComponentVertex>().component
      }
    }
    traversal.traverse(graph, delegate, componentVertex)
    return chain
  }

  private fun DirectedGraph<Dependency>.findUnresolvedDependencies(
      components: Collection<Component>
  ): Collection<Dependency> {
    val unresolvedDependencies = HashSet<Dependency>()

    val traversal = DepthFirstTraversal<Dependency>()
    val delegate = object : AbstractMarkingTraversal.SimpleDelegate<Dependency>() {
      override fun onBeforeAdjacentVertices(vertex: Dependency) {
        if (getAdjacentVertices(vertex) == null) {
          unresolvedDependencies.add(vertex)
        }
      }
    }

    components.forEach { component ->
      component.modules.forEach { module ->
        module.providers.forEach { provider ->
          traversal.traverse(this, delegate, provider.dependency.box())
        }
      }
    }

    return unresolvedDependencies
  }
}
