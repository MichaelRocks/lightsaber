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

package io.michaelrocks.lightsaber.processor.validation

import io.michaelrocks.lightsaber.processor.graph.DirectedGraph
import io.michaelrocks.lightsaber.processor.graph.HashDirectedGraph
import io.michaelrocks.lightsaber.processor.graph.putAll
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import io.michaelrocks.lightsaber.processor.model.Module
import java.util.ArrayList

fun buildInjectionGraph(graphs: Collection<DirectedGraph<InjectionGraphVertex>>): DirectedGraph<InjectionGraphVertex> {
  val graph = HashDirectedGraph<InjectionGraphVertex>()
  graphs.forEach { graph.putAll(it) }
  return graph
}

fun buildInjectionGraphs(injectionContext: InjectionContext): Collection<DirectedGraph<InjectionGraphVertex>> {
  val graphs = ArrayList<DirectedGraph<InjectionGraphVertex>>()
  val graph = HashDirectedGraph<InjectionGraphVertex>()
  buildInjectionGraphs(injectionContext, injectionContext.packageComponent, null, graph, graphs)
  return graphs
}

private fun buildInjectionGraphs(
    injectionContext: InjectionContext,
    component: Component,
    parentComponent: Component?,
    base: DirectedGraph<InjectionGraphVertex>,
    graphs: MutableList<DirectedGraph<InjectionGraphVertex>>
) {
  if (InjectionGraphVertex.ComponentVertex(component) in base) {
    graphs += base.withComponentEdge(component, parentComponent)
    return
  }

  val graph = base.withComponent(component, parentComponent)

  if (component.subcomponents.isEmpty()) {
    graphs += graph
  }

  component.subcomponents.forEach {
    injectionContext.findComponentByType(it)?.let { childComponent ->
      buildInjectionGraphs(injectionContext, childComponent, component, graph, graphs)
    }
  }
}

private fun DirectedGraph<InjectionGraphVertex>.withComponentEdge(
    component: Component,
    parentComponent: Component?
): DirectedGraph<InjectionGraphVertex> {
  if (parentComponent == null) {
    return this
  } else {
    val componentVertex = InjectionGraphVertex.ComponentVertex(component)
    val parentComponentVertex = InjectionGraphVertex.ComponentVertex(parentComponent)
    val graph = HashDirectedGraph(this)
    graph.put(parentComponentVertex, componentVertex)
    return graph
  }
}

private fun DirectedGraph<InjectionGraphVertex>.withComponent(
    component: Component,
    parentComponent: Component?
): DirectedGraph<InjectionGraphVertex> {
  val graph = HashDirectedGraph(this)
  val componentVertex = InjectionGraphVertex.ComponentVertex(component)
  parentComponent?.let {
    val parentComponentVertex = InjectionGraphVertex.ComponentVertex(parentComponent)
    graph.put(parentComponentVertex, componentVertex)
  }

  component.modules.forEach { module ->
    val moduleVertex = InjectionGraphVertex.ModuleVertex(module)
    graph.put(componentVertex, moduleVertex)

    module.providers.forEach { provider ->
      val dependencyVertex = InjectionGraphVertex.DependencyVertex(provider.dependency)
      graph.put(moduleVertex, dependencyVertex)
    }
  }
  return graph
}

sealed class InjectionGraphVertex {
  data class ComponentVertex(val component: Component) : InjectionGraphVertex()
  data class ModuleVertex(val module: Module) : InjectionGraphVertex()
  data class DependencyVertex(val dependency: Dependency) : InjectionGraphVertex()
}
