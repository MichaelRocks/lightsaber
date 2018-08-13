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
import io.michaelrocks.lightsaber.processor.commons.boxed
import io.michaelrocks.lightsaber.processor.graph.DirectedGraph
import io.michaelrocks.lightsaber.processor.graph.HashDirectedGraph
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint

fun buildDependencyGraph(modules: Collection<Module>): DirectedGraph<Dependency> {
  return HashDirectedGraph<Dependency>().apply {
    val rootType = Dependency(GenericType.Raw(Types.INJECTOR_TYPE))
    put(rootType, emptyList())
    for (module in modules) {
      for (provider in module.providers) {
        val returnType = provider.dependency.boxed()
        val method = provider.provisionPoint as? ProvisionPoint.AbstractMethod
        val injectees = method?.injectionPoint?.injectees
        val dependencies = injectees?.map { it.dependency.boxed() }
        put(returnType, dependencies.orEmpty())
      }
    }
  }
}
