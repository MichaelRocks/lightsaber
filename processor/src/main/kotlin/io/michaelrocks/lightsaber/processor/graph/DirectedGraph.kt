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

interface DirectedGraph<T> {
  val size: Int
    get() = vertices.size
  val vertices: Collection<T>

  fun isEmpty(): Boolean = vertices.isEmpty()
  operator fun contains(vertex: T): Boolean = vertex in vertices

  fun getAdjacentVertices(vertex: T): Collection<T>?
}

interface MutableDirectedGraph<T> : DirectedGraph<T> {
  fun clear()
  fun put(from: T, to: T)
  fun put(from: T, to: Collection<T>)
  fun putAll(from: Map<T, Collection<T>>)
  fun putAll(graph: DirectedGraph<T>)
  fun remove(from: T, to: T)
  fun removeAll(from: T, to: T)
  fun removeAll(from: T)

  override fun getAdjacentVertices(vertex: T): MutableCollection<T>?
}