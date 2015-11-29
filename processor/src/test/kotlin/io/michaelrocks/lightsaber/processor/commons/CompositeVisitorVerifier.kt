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

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.mockito.Mockito.*
import java.util.*
import kotlin.reflect.KClass

private val MAX_VISITOR_COUNT = 3
private val EMPTIES_PERMUTATIONS = arrayOf(
    booleanArrayOf(),
    booleanArrayOf(true),
    booleanArrayOf(false),
    booleanArrayOf(true, true),
    booleanArrayOf(false, true),
    booleanArrayOf(true, false),
    booleanArrayOf(false, false),
    booleanArrayOf(true, true, true),
    booleanArrayOf(true, true, false),
    booleanArrayOf(true, false, true),
    booleanArrayOf(false, true, true),
    booleanArrayOf(true, false, false),
    booleanArrayOf(false, false, false)
)

fun <T, V> verifyMethodInvocations(compositeVisitorClass: KClass<T>, action: V.() -> Any?)
    where T : V, T : CompositeVisitor<V>, V : Any {
  for (i in 0..MAX_VISITOR_COUNT) {
    verifyMethodInvocation(compositeVisitorClass.java.newInstance(), action, i)
  }
}

fun <T, V> verifyMethodInvocations(compositeVisitorClass: Class<T>, action: V.() -> Any?)
    where T : V, T : CompositeVisitor<V>, V : Any {
  for (i in 0..MAX_VISITOR_COUNT) {
    verifyMethodInvocation(compositeVisitorClass.newInstance(), action, i)
  }
}

private fun <T, V> verifyMethodInvocation(compositeVisitor: T, action: V.() -> Any?, visitorCount: Int)
    where T : V, T : CompositeVisitor<V>, V : Any {
  val visitors = ArrayList<V>(visitorCount)
  for (i in 0 until visitorCount) {
    @Suppress("UNCHECKED_CAST")
    val visitor = mock(compositeVisitor.javaClass.superclass as Class<V>)
    visitors.add(visitor)
    compositeVisitor.addVisitor(visitor)
  }

  compositeVisitor.action()

  for (visitor in visitors) {
    verify(visitor, only()).action()
  }
}

inline fun <reified T, R, V> verifyCompositeMethodInvocations(
    noinline action: V.() -> R, noinline innerAction: R.() -> Any?)
    where T : Any, T : V, T : CompositeVisitor<V>, V : Any {
  verifyCompositeMethodInvocations(T::class.java, action, innerAction)
}

fun <T, R, V> verifyCompositeMethodInvocations(compositeVisitorClass: KClass<T>, action: V.() -> R,
    innerAction: R.() -> Any?)
    where T : Any, T : V, T : CompositeVisitor<V>, V : Any {
  verifyCompositeMethodInvocations(compositeVisitorClass.java, action, innerAction)
}

fun <T, R, V> verifyCompositeMethodInvocations(compositeVisitorClass: Class<T>, action: V.() -> R,
    innerAction: R.() -> Any?)
    where T : Any, T : V, T : CompositeVisitor<V>, V : Any {
  for (empties in EMPTIES_PERMUTATIONS) {
    verifyCompositeMethodInvocation(compositeVisitorClass.newInstance(), action, innerAction, empties)
  }
}

@Suppress("CAST_NEVER_SUCCEEDS")
private fun <T, R, V> verifyCompositeMethodInvocation(compositeVisitor: T,
    action: V.() -> R, innerAction: R.() -> Any?, empties: BooleanArray)
    where T : V, T : CompositeVisitor<V>, V : Any {
  val visitors = ArrayList<V>(empties.size)
  for (empty in empties) {
    val answer = if (empty) RETURNS_DEFAULTS else RETURNS_DEEP_STUBS
    @Suppress("UNCHECKED_CAST")
    val visitor = mock(compositeVisitor.javaClass.superclass as Class<V>, answer)
    visitors.add(visitor)
    compositeVisitor.addVisitor(visitor)
  }

  val result = compositeVisitor.action()

  for (visitor in visitors) {
    visitor.action()
  }

  result?.innerAction()

  var hasNonEmpty = false
  for (i in empties.indices) {
    val empty = empties[i]
    if (!empty) {
      hasNonEmpty = true
      val visitor = visitors.get(i)
      assertNotNull(result)
      innerAction.invoke(verify(action.invoke(visitor), only()))
    }
  }

  if (!hasNonEmpty) {
    assertNull(result)
  }
}
