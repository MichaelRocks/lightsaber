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
  fun findCycles(): Collection<Collection<T>> = findCycles(HashMap(), HashSet())

  private fun findCycles(
      colors: MutableMap<T, VertexColor>,
      cycles: MutableSet<Collection<T>>
  ): Collection<Collection<T>> {
    fun traverse(vertex: T, cycle: MutableList<T>) {
      val color = colors[vertex]
      if (color == VertexColor.BLACK) {
        return
      }

      try {
        cycle.add(vertex)

        if (color == VertexColor.GRAY) {
          cycles.add(cycle.toList())
          return
        }

        colors.put(vertex, VertexColor.GRAY)
        graph.getAdjacentVertices(vertex)?.forEach { traverse(it, cycle) }
        colors.put(vertex, VertexColor.BLACK)
      } finally {
        cycle.removeAt(cycle.lastIndex)
      }
    }

    graph.vertices.forEach { traverse(it, ArrayList()) }
    return cycles
  }

  private enum class VertexColor {
    GRAY, BLACK
  }
}
