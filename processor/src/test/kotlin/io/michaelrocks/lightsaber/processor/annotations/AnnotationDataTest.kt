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

package io.michaelrocks.lightsaber.processor.annotations

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class AnnotationDataTest {
  @Test
  @Throws(Exception::class)
  fun testEqualsWithIntArrays() {
    val annotation1 = AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(42, 43, 44))
    val annotation2 = AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(42, 43, 44))
    assertEquals(1, annotation1.values.size)
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode(), annotation2.hashCode())
  }

  @Test
  @Throws(Exception::class)
  fun testEqualsWithAnnotationArrays() {
    val annotation1 = AnnotationHelper.createAnnotationData("EqualsWithAnnotationArrays",
        arrayOf(
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(42, 43, 44)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(45, 46, 47)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(48, 49, 50))
        )
    )
    val annotation2 = AnnotationHelper.createAnnotationData("EqualsWithAnnotationArrays",
        arrayOf(
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(42, 43, 44)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(45, 46, 47)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(48, 49, 50))
        )
    )
    assertEquals(1, annotation1.values.size)
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode(), annotation2.hashCode())
  }

  @Test
  @Throws(Exception::class)
  fun testEqualsWithDifferentOrder() {
    val values1 = hashMapOf(
        "intValue" to 42,
        "stringValue" to "42"
    )
    val annotation1 = AnnotationHelper.createAnnotationData("EqualsWithAnnotationArrays", values1)
    val values2 = hashMapOf(
        "intValue" to 42,
        "stringValue" to "42"
    )
    val annotation2 = AnnotationHelper.createAnnotationData("EqualsWithAnnotationArrays", values2)
    assertEquals(2, annotation1.values.size)
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode(), annotation2.hashCode())
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsToNull() {
    val annotation = AnnotationHelper.createAnnotationData("NotEqualsToNull")
    @Suppress("SENSELESS_COMPARISON")
    assertFalse(annotation == null)
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsToString() {
    val annotation = AnnotationHelper.createAnnotationData("NotEqualsToString")
    assertNotEquals("NotEqualsToString", annotation)
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsByType() {
    val annotation1 = AnnotationHelper.createAnnotationData("NotEqualsByType1")
    val annotation2 = AnnotationHelper.createAnnotationData("NotEqualsByType2")
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsWithStrings() {
    val annotation1 = AnnotationHelper.createAnnotationData("NotEqualsWithStrings", "Value1")
    val annotation2 = AnnotationHelper.createAnnotationData("NotEqualsWithStrings", "Value2")
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsWithIntArrays() {
    val annotation1 = AnnotationHelper.createAnnotationData("NotEqualsWithIntArrays", intArrayOf(42))
    val annotation2 = AnnotationHelper.createAnnotationData("NotEqualsWithIntArrays", intArrayOf(-42))
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsWithAnnotationArrays() {
    val annotation1 = AnnotationHelper.createAnnotationData("NotEqualsWithAnnotationArrays",
        arrayOf(
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(42, 43, 44)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(45, 46, 47)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(48, 49, 50)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(1))
        )
    )
    val annotation2 = AnnotationHelper.createAnnotationData("NotEqualsWithAnnotationArrays",
        arrayOf(
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(42, 43, 44)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(45, 46, 47)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(48, 49, 50)),
            AnnotationHelper.createAnnotationData("EqualsWithIntArrays", intArrayOf(-1))
        )
    )
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  @Throws(Exception::class)
  fun testToString() {
    val nameUuid = UUID.randomUUID()
    val name = "ToString%016x%016x".format(nameUuid.mostSignificantBits, nameUuid.leastSignificantBits)
    val value = UUID.randomUUID().toString()
    val annotation = AnnotationHelper.createAnnotationData(name, value)
    val annotationDescription = annotation.toString()
    assertNotNull(annotationDescription)
    assertTrue(annotationDescription.contains(name))
    assertTrue(annotationDescription.contains(value))
  }
}
