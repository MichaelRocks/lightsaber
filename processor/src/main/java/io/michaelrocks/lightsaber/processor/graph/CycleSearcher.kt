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

package io.michaelrocks.lightsaber.processor.graph

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet

fun <T> DirectedGraph<T>.findCycles(): Collection<List<T>> {
  val cycles = HashSet<List<T>>()
  val delegate = object : CycleSearcherTraversal.Delegate<T>() {
    override fun onCycle(cycle: List<T>) {
      cycles += cycle
    }
  }

  val traversal = CycleSearcherTraversal<T>()
  traversal.traverse(this, delegate)
  return cycles
}


class CycleSearcherTraversal<T> : AbstractTraversal<T, CycleSearcherTraversal.Delegate<T>>() {
  override fun performTraversal(graph: DirectedGraph<T>, delegate: Delegate<T>, vertex: T) {
    val color = delegate.getVertexColor(vertex)
    if (color == VertexColor.BLACK) {
      return
    }

    try {
      delegate.pushVertexToPath(vertex)

      if (color == VertexColor.GRAY) {
        delegate.extractCycle(vertex)
        return
      }

      delegate.setVertexColor(vertex, VertexColor.GRAY)
      graph.getAdjacentVertices(vertex)?.forEach { performTraversal(graph, delegate, it) }
      delegate.setVertexColor(vertex, VertexColor.BLACK)
    } finally {
      delegate.popVertexFromPath()
    }
  }

  abstract class Delegate<T> : Traversal.Delegate<T> {
    private val colors = HashMap<T, VertexColor>()
    private val path = ArrayList<T>()

    fun getVertexColor(vertex: T): VertexColor {
      return colors[vertex] ?: VertexColor.WHITE
    }

    fun setVertexColor(vertex: T, color: VertexColor) {
      colors[vertex] = color
    }

    fun pushVertexToPath(vertex: T) {
      path.add(vertex)
    }

    fun popVertexFromPath() {
      path.removeAt(path.lastIndex)
    }

    fun extractCycle(vertex: T) {
      val cycleStartIndex = path.indexOf(vertex)
      val cycle = path.subList(cycleStartIndex, path.size).toList()
      onCycle(cycle)
    }

    abstract fun onCycle(cycle: List<T>)
  }

  enum class VertexColor {
    WHITE, GRAY, BLACK
  }
}
