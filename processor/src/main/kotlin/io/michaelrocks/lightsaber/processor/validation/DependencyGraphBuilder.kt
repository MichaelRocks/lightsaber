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

package io.michaelrocks.lightsaber.processor.validation

import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.box
import io.michaelrocks.lightsaber.processor.graph.DirectedGraph
import io.michaelrocks.lightsaber.processor.graph.HashDirectedGraph
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.Converter
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.Injectee
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint

class DependencyGraphBuilder(
    private val omitWrappedDependencies: Boolean = false
) {
  private val graph = HashDirectedGraph<Dependency>()

  init {
    val rootType = Dependency(GenericType.Raw(Types.INJECTOR_TYPE))
    graph.put(rootType, emptyList())
  }

  fun add(module: Module): DependencyGraphBuilder = apply {
    for (provider in module.providers) {
      val returnType = provider.dependency.box()
      val method = provider.provisionPoint as? ProvisionPoint.AbstractMethod
      val injectees = method?.injectionPoint?.injectees?.maybeOmitWrappedDependencies()
      val dependencies = injectees?.map { it.dependency.box() }
      graph.put(returnType, dependencies.orEmpty())
    }
  }

  fun add(modules: Iterable<Module>): DependencyGraphBuilder = apply {
    modules.forEach { add(it) }
  }

  fun add(component: Component): DependencyGraphBuilder = apply {
    add(component.modules)
  }

  fun build(): DirectedGraph<Dependency> {
    return HashDirectedGraph(graph)
  }

  private fun List<Injectee>.maybeOmitWrappedDependencies(): List<Injectee> {
    if (!omitWrappedDependencies) {
      return this
    }

    return filter { it.converter === Converter.Instance }
  }
}
