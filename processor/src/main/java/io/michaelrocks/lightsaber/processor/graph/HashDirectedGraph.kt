/*
 * Copyright 2019 Michael Rozumyanskiy
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

fun <T> HashDirectedGraph(
  graph: DirectedGraph<T>,
  collection: () -> MutableCollection<T> = { ArrayList() }
): HashDirectedGraph<T> {
  return HashDirectedGraph(collection).apply { putAll(graph) }
}

class HashDirectedGraph<T>(
  private val collection: () -> MutableCollection<T> = { ArrayList() }
) : MutableDirectedGraph<T> {

  private val edges = HashMap<T, MutableCollection<T>>()

  override val vertices: Collection<T>
    get() = edges.keys

  override fun clear() {
    edges.clear()
  }

  override fun put(vertex: T) {
    getOrCreateAdjacentVertices(vertex)
  }

  override fun put(from: T, to: T) {
    put(to)
    getOrCreateAdjacentVertices(from).add(to)
  }

  override fun put(from: T, to: Collection<T>) {
    to.forEach { put(it) }
    getOrCreateAdjacentVertices(from).addAll(to)
  }

  override fun putAll(from: Map<T, Collection<T>>) {
    for ((vertex, vertices) in from) {
      put(vertex, vertices)
    }
  }

  override fun remove(from: T, to: T) {
    getAdjacentVertices(from)?.let { vertices ->
      vertices.remove(to)
      if (vertices.isEmpty()) {
        edges.remove(from)
      }
    }
  }

  override fun removeAll(from: T, to: T) {
    getAdjacentVertices(from)?.let { vertices ->
      vertices.removeAll { it == to }
      if (vertices.isEmpty()) {
        edges.remove(from)
      }
    }
  }

  override fun removeAll(from: T) {
    edges.remove(from)
  }

  override fun getAdjacentVertices(vertex: T): MutableCollection<T>? {
    return edges[vertex]
  }

  override fun asMap(): Map<T, Collection<T>> {
    return edges
  }

  private fun getOrCreateAdjacentVertices(vertex: T): MutableCollection<T> {
    return edges.getOrPut(vertex) { collection() }
  }
}
