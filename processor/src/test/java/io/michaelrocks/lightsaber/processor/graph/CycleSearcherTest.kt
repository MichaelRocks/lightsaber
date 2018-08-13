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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CycleSearcherTest {
  @Test
  fun testMinimalCycle() {
    val graph = HashDirectedGraph<Int>()
    graph.put(1, 2)
    graph.put(2, 1)
    val cycles = graph.findCycles()
    assertEquals(1, cycles.size)
    assertEquals(3, cycles.first().size)
  }

  @Test
  fun testCycle() {
    val graph = HashDirectedGraph<Int>()
    graph.put(1, 2)
    graph.put(2, 3)
    graph.put(3, 1)
    val cycles = graph.findCycles()
    assertEquals(1, cycles.size)
    assertEquals(4, cycles.first().size)
  }

  @Test
  fun testMinimalCycleWithTail() {
    val graph = HashDirectedGraph<Int>()
    graph.put(1, 2)
    graph.put(2, 3)
    graph.put(3, 2)
    val cycles = graph.findCycles()
    assertEquals(1, cycles.size)
    assertEquals(3, cycles.first().size)
  }

  @Test
  fun testCycleWithTail() {
    val graph = HashDirectedGraph<Int>()
    graph.put(1, 2)
    graph.put(2, 3)
    graph.put(3, 4)
    graph.put(4, 2)
    val cycles = graph.findCycles()
    assertEquals(1, cycles.size)
    assertEquals(4, cycles.first().size)
  }

  @Test
  fun testButterflyCycle() {
    val graph = HashDirectedGraph<Int>()
    graph.put(1, 2)
    graph.put(2, 3)
    graph.put(3, 2)
    graph.put(2, 4)
    graph.put(4, 2)
    val cycles = graph.findCycles().toList()
    assertEquals(2, cycles.size)
    assertEquals(3, cycles[0].size)
    assertEquals(3, cycles[1].size)
  }

  @Test
  fun testAcyclicGraph() {
    val graph = HashDirectedGraph<Int>()
    graph.put(1, 2)
    graph.put(2, 3)
    graph.put(3, 4)
    val cycles = graph.findCycles()
    assertTrue(cycles.isEmpty())
  }
}
