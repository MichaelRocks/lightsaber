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

class CycleSearcher(private val graph: DependencyGraph) {
  fun findCycles(): Collection<QualifiedType> = findCycles(HashMap(), HashSet())

  private fun findCycles(
      colors: MutableMap<QualifiedType, VertexColor>,
      cycles: MutableSet<QualifiedType>
  ): Collection<QualifiedType> {
    fun traverse(type: QualifiedType) {
      val color = colors[type]
      if (color == VertexColor.BLACK) {
        return
      }

      if (color == VertexColor.GRAY) {
        cycles.add(type)
        return
      }

      colors.put(type, VertexColor.GRAY)
      graph.getTypeDependencies(type)?.forEach { traverse(it) }
      colors.put(type, VertexColor.BLACK)
    }

    graph.types.forEach { traverse(it) }
    return Collections.unmodifiableSet(cycles)
  }

  private enum class VertexColor {
    GRAY, BLACK
  }
}
