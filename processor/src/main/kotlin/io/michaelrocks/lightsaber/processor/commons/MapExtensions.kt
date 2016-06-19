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

fun <K, V> Map<K, Collection<V>>.mergeWith(
    map: Map<K, Collection<V>>,
    collection: () -> MutableCollection<V> = { ArrayList() }
): Map<K, Collection<V>> {
  val result = mapValuesTo(HashMap()) { entry ->
    collection().apply { addAll(entry.value) }
  }
  map.mergeTo(result, collection)
  return result
}

fun <K, V> Map<K, Collection<V>>.mergeTo(
    destination: MutableMap<K, MutableCollection<V>>,
    collection: () -> MutableCollection<V> = { ArrayList() }
): Map<K, Collection<V>> {
  for ((key, value) in this) {
    destination.getOrPut(key, collection).addAll(value)
  }
  return destination
}
