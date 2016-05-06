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

package io.michaelrocks.lightsaber.processor.graph

import java.util.*

class CycleSearcher<T>(private val graph: DirectedGraph<T>) {
  fun findCycles(): Collection<T> = findCycles(HashMap(), HashSet())

  private fun findCycles(
      colors: MutableMap<T, VertexColor>,
      cycles: MutableSet<T>
  ): Collection<T> {
    fun traverse(vertex: T) {
      val color = colors[vertex]
      if (color == VertexColor.BLACK) {
        return
      }

      if (color == VertexColor.GRAY) {
        cycles.add(vertex)
        return
      }

      colors.put(vertex, VertexColor.GRAY)
      graph.getAdjacentVertices(vertex)?.forEach { traverse(it) }
      colors.put(vertex, VertexColor.BLACK)
    }

    graph.vertices.forEach { traverse(it) }
    return Collections.unmodifiableSet(cycles)
  }

  private enum class VertexColor {
    GRAY, BLACK
  }
}
