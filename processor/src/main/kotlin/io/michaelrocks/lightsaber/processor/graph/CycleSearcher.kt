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

fun <T> findCycles(graph: DirectedGraph<T>): Collection<Collection<T>> {
  val gray = HashSet<T>()
  val cycles = HashSet<Collection<T>>()
  val cycle = ArrayList<T>()

  graph.traverseDepthFirst(
      beforeAdjacent = { vertex ->
        cycle.add(vertex)

        if (!gray.add(vertex)) {
          cycles.add(cycle.toList())
        }
      },
      afterAdjacent = { vertex ->
        gray.remove(vertex)
        cycle.removeAt(cycle.lastIndex)
      }
  )

  return cycles
}
