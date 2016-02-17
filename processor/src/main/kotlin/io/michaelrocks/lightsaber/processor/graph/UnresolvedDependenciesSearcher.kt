/*
 * Copyright 2015 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.processor.graph

import io.michaelrocks.lightsaber.processor.descriptors.QualifiedType
import java.util.*

class UnresolvedDependenciesSearcher(private val graph: DependencyGraph) {
  fun findUnresolvedDependencies(): Collection<QualifiedType> = findUnresolvedDependencies(HashSet())

  private fun findUnresolvedDependencies(
      visitedTypes: MutableSet<QualifiedType>
  ): Collection<QualifiedType> {
    fun traverse(type: QualifiedType) {
      if (visitedTypes.add(type)) {
        val dependencies = graph.getTypeDependencies(type)
        dependencies?.forEach { traverse(it) }
      }
    }

    graph.types.forEach { traverse(it) }
    return graph.types.filterNot { it in visitedTypes }
  }
}
