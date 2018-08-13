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

import java.util.HashSet

interface Traversal<T, D : Traversal.Delegate<T>> {
  fun traverse(graph: DirectedGraph<T>, delegate: D)
  fun traverse(graph: DirectedGraph<T>, delegate: D, fromVertex: T)

  interface Delegate<T>
}

abstract class AbstractTraversal<T, D : Traversal.Delegate<T>> : Traversal<T, D> {
  override fun traverse(graph: DirectedGraph<T>, delegate: D) {
    graph.vertices.forEach { performTraversal(graph, delegate, it) }
  }

  override fun traverse(graph: DirectedGraph<T>, delegate: D, fromVertex: T) {
    performTraversal(graph, delegate, fromVertex)
  }

  protected abstract fun performTraversal(graph: DirectedGraph<T>, delegate: D, vertex: T)
}

abstract class AbstractMarkingTraversal<T> : AbstractTraversal<T, AbstractMarkingTraversal.Delegate<T>>() {
  interface Delegate<T> : Traversal.Delegate<T> {
    fun onBeforeVertex(vertex: T) = Unit
    fun onAfterVertex(vertex: T) = Unit
    fun onBeforeAdjacentVertices(vertex: T) = Unit
    fun onAfterAdjacentVertices(vertex: T) = Unit

    fun isVisited(vertex: T): Boolean
    fun markAsVisited(vertex: T)
  }

  open class SimpleDelegate<T> : Delegate<T> {
    private val visited = HashSet<T>()

    fun getVisitedVertices(): Set<T> {
      return HashSet(visited)
    }

    override fun isVisited(vertex: T): Boolean {
      return vertex in visited
    }

    override fun markAsVisited(vertex: T) {
      visited += vertex
    }

    open fun reset() {
      visited.clear()
    }
  }
}

class DepthFirstTraversal<T> : AbstractMarkingTraversal<T>() {
  override fun performTraversal(graph: DirectedGraph<T>, delegate: Delegate<T>, vertex: T) {
    delegate.onBeforeVertex(vertex)
    try {
      if (!delegate.isVisited(vertex)) {
        delegate.markAsVisited(vertex)
        delegate.onBeforeAdjacentVertices(vertex)
        try {
          graph.getAdjacentVertices(vertex)?.forEach { performTraversal(graph, delegate, it) }
        } finally {
          delegate.onAfterAdjacentVertices(vertex)
        }
      }
    } finally {
      delegate.onAfterVertex(vertex)
    }
  }
}
