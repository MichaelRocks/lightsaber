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

package io.michaelrocks.lightsaber.processor.commons

import java.util.*

inline fun <T, K, V, M : MutableMap<in K, in V>> Iterable<T>.associateByIndexedTo(destination: M,
    keySelector: (Int, T) -> K, valueSelector: (Int, T) -> V): M {
  forEachIndexed { index, element ->
    destination.put(keySelector(index, element), valueSelector(index, element))
  }
  return destination
}

inline fun <T, K, V, M : MutableMap<in K, MutableList<V>>> Iterable<T>.groupNotNullByTo(destination: M,
    keySelector: (T) -> K, valueTransform: (T) -> V?): M {
  for (element in this) {
    valueTransform(element)?.let { value ->
      val key = keySelector(element)
      val list = destination.getOrPut(key) { ArrayList<V>() }
      list.add(value)
    }
  }
  return destination
}
