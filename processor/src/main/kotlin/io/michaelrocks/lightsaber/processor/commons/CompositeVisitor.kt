/*
 * Copyright 2015 Michael Rozumyanskiy
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

interface CompositeVisitor<T> {
  val visitors: List<T>
  val isEmpty: Boolean
    get() = visitors.isEmpty()

  fun addVisitor(visitor: T)
}

fun <T> CompositeVisitor<in T>.addVisitor(visitor: T?) {
  if (visitor != null) {
    addVisitor(visitor)
  }
}

inline fun <T> CompositeVisitor<T>.forEachVisitor(action: T.() -> Unit) {
  visitors.forEach { it.action() }
}

inline fun <C : CompositeVisitor<R>, T, R> CompositeVisitor<T>.addVisitorsTo(
    compositeVisitor: C, produce: T.() -> R?): C? {
  for (visitor in visitors) {
    visitor.produce()?.let { compositeVisitor.addVisitor(it) }
  }
  return if (compositeVisitor.isEmpty) null else compositeVisitor
}
