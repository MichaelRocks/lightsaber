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

import io.michaelrocks.lightsaber.processor.graph.DirectedGraph
import io.michaelrocks.lightsaber.processor.graph.HashDirectedGraph
import io.michaelrocks.lightsaber.processor.graph.putAll
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import io.michaelrocks.lightsaber.processor.model.Module
import java.util.*

fun buildInjectionGraph(graphs: Collection<DirectedGraph<InjectionGraphVertex>>): DirectedGraph<InjectionGraphVertex> {
  val graph = HashDirectedGraph<InjectionGraphVertex>()
  graphs.forEach { graph.putAll(it) }
  return graph
}

fun buildInjectionGraphs(injectionContext: InjectionContext): Collection<DirectedGraph<InjectionGraphVertex>> {
  val graphs = ArrayList<DirectedGraph<InjectionGraphVertex>>()
  val graph = HashDirectedGraph<InjectionGraphVertex>()
  buildInjectionGraphs(injectionContext, injectionContext.packageComponent, graph, graphs)
  return graphs
}

private fun buildInjectionGraphs(
    injectionContext: InjectionContext,
    component: Component,
    base: DirectedGraph<InjectionGraphVertex>,
    graphs: MutableList<DirectedGraph<InjectionGraphVertex>>
) {
  val graph = base.withComponent(component)

  if (component.subcomponents.isEmpty()) {
    graphs += graph
  }

  component.subcomponents.forEach {
    val childComponent = injectionContext.findComponentByType(it)!!
    buildInjectionGraphs(injectionContext, childComponent, graph, graphs)
  }
}

private fun DirectedGraph<InjectionGraphVertex>.withComponent(component: Component): DirectedGraph<InjectionGraphVertex> {
  val graph = HashDirectedGraph(this)
  val componentVertex = InjectionGraphVertex.ComponentVertex(component)
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
  class ComponentVertex(val component: Component) : InjectionGraphVertex() {
    override fun equals(other: Any?): Boolean {
      if (this === other) {
        return true
      }

      val that = other as? ComponentVertex ?: return false
      return component == that.component
    }

    override fun hashCode(): Int {
      return 17 + 31 * component.hashCode()
    }

    override fun toString(): String = component.toString()
  }

  class ModuleVertex(val module: Module) : InjectionGraphVertex() {
    override fun equals(other: Any?): Boolean {
      if (this === other) {
        return true
      }

      val that = other as? ModuleVertex ?: return false
      return module == that.module
    }

    override fun hashCode(): Int {
      return 17 + 31 * module.hashCode()
    }

    override fun toString(): String = module.toString()
  }

  class DependencyVertex(val dependency: Dependency) : InjectionGraphVertex() {
    override fun equals(other: Any?): Boolean {
      if (this === other) {
        return true
      }

      val that = other as? DependencyVertex ?: return false
      return dependency == that.dependency
    }

    override fun hashCode(): Int {
      return 17 + 31 * dependency.hashCode()
    }

    override fun toString(): String = dependency.toString()
  }
}
