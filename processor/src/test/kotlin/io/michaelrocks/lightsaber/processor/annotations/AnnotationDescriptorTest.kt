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

import io.michaelrocks.lightsaber.processor.commons.getType
import org.junit.Assert.*
import org.junit.Test
import org.objectweb.asm.Type
import java.util.*

class AnnotationDescriptorTest {
  @Test
  @Throws(Exception::class)
  fun testEqualsWithIntArrays() {
    val annotation1 = AnnotationHelper.createAnnotationDescriptor("EqualsEmpty")
    val annotation2 = AnnotationHelper.createAnnotationDescriptor("EqualsEmpty")
    assertEquals(0, annotation1.fields.size.toLong())
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode().toLong(), annotation2.hashCode().toLong())
  }

  @Test
  @Throws(Exception::class)
  fun testEqualsWithDefaultField() {
    val annotation1 = AnnotationHelper.createAnnotationDescriptor("EqualsWithDefaultField", getType<String>())
    val annotation2 = AnnotationHelper.createAnnotationDescriptor("EqualsWithDefaultField", getType<String>())
    assertEquals(1, annotation1.fields.size.toLong())
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode().toLong(), annotation2.hashCode().toLong())
  }

  @Test
  @Throws(Exception::class)
  fun testEqualsWithSimilarOrder() {
    val annotation1 = AnnotationHelper.createAnnotationDescriptor("EqualsWithSimilarOrder",
        Pair("value1", getType<String>()),
        Pair("value2", getType<String>()))
    val annotation2 = AnnotationHelper.createAnnotationDescriptor("EqualsWithSimilarOrder",
        Pair("value1", getType<String>()),
        Pair("value2", getType<String>()))
    assertEquals(2, annotation1.fields.size.toLong())
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode().toLong(), annotation2.hashCode().toLong())
  }

  @Test
  @Throws(Exception::class)
  fun testEqualsWithDifferentOrder() {
    val annotation1 = AnnotationHelper.createAnnotationDescriptor("EqualsWithSimilarOrder",
        Pair("value1", getType<String>()),
        Pair("value2", getType<String>()))
    val annotation2 = AnnotationHelper.createAnnotationDescriptor("EqualsWithSimilarOrder",
        Pair("value2", getType<String>()),
        Pair("value1", getType<String>()))
    assertEquals(2, annotation1.fields.size.toLong())
    assertEquals(annotation1, annotation2)
    assertEquals(annotation1.hashCode().toLong(), annotation2.hashCode().toLong())
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsToNull() {
    val annotation = AnnotationHelper.createAnnotationDescriptor("NotEqualsToNull")
    // noinspection ObjectEqualsNull
    assertFalse(annotation == null)
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsToString() {
    val annotation = AnnotationHelper.createAnnotationDescriptor("NotEqualsToString")
    assertNotEquals("NotEqualsToString", annotation)
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsByType() {
    val annotation1 = AnnotationHelper.createAnnotationDescriptor("NotEqualsByType1")
    val annotation2 = AnnotationHelper.createAnnotationDescriptor("NotEqualsByType2")
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsByFieldType() {
    val annotation1 = AnnotationHelper.createAnnotationDescriptor("NotEqualsByFieldName", getType<String>())
    val annotation2 = AnnotationHelper.createAnnotationDescriptor("NotEqualsByFieldName", Type.INT_TYPE)
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  @Throws(Exception::class)
  fun testNotEqualsByFieldName() {
    val annotation1 = AnnotationHelper.createAnnotationDescriptor("NotEqualsByFieldName", "value1", Type.INT_TYPE)
    val annotation2 = AnnotationHelper.createAnnotationDescriptor("NotEqualsByFieldName", "value2", Type.INT_TYPE)
    assertNotEquals(annotation1, annotation2)
  }

  @Test
  @Throws(Exception::class)
  fun testToString() {
    val nameUuid = UUID.randomUUID()
    val name = "ToString%016x%016x".format(nameUuid.mostSignificantBits, nameUuid.leastSignificantBits)
    val fieldName = UUID.randomUUID().toString()
    val fieldType = Type.getObjectType(UUID.randomUUID().toString())
    val annotation = AnnotationHelper.createAnnotationDescriptor(name, fieldName, fieldType)
    val annotationDescription = annotation.toString()
    assertNotNull(annotationDescription)
    assertTrue(annotationDescription.contains(name))
    assertTrue(annotationDescription.contains(fieldName))
    assertTrue(annotationDescription.contains(fieldType.toString()))
  }
}
