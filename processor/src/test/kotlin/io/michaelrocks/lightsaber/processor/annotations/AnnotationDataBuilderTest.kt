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
import org.objectweb.asm.Type

class AnnotationDataBuilderTest {
  @Test
  @Throws(Exception::class)
  fun testEmptyAnnotation() {
    val annotation = newAnnotationBuilder("EmptyAnnotation").build()
    assertEquals("EmptyAnnotation", annotation.type.internalName)
    assertTrue(annotation.values.isEmpty())
  }

  @Test
  @Throws(Exception::class)
  fun testDefaultValueAnnotation() {
    val annotation = newAnnotationBuilder("DefaultValueAnnotation").addDefaultValue("DefaultValue").build()
    assertEquals("DefaultValueAnnotation", annotation.type.internalName)
    assertEquals(1, annotation.values.size.toLong())
    assertEquals("DefaultValue", annotation.values["value"])
  }

  @Test
  @Throws(Exception::class)
  fun testNamedValueAnnotation() {
    val annotation = newAnnotationBuilder("NamedValueAnnotation").addDefaultValue("namedValue", "NamedValue").build()
    assertEquals("NamedValueAnnotation", annotation.type.internalName)
    assertEquals(1, annotation.values.size.toLong())
    assertEquals("NamedValue", annotation.values["namedValue"])
  }

  @Test
  @Throws(Exception::class)
  fun testVariousValuesAnnotation() {
    val innerAnnotation = newAnnotationBuilder("InnerAnnotation").build()
    val annotation = newAnnotationBuilder("VariousValuesAnnotation").run {
      addDefaultValue("booleanValue", true)
      addDefaultValue("byteValue", 42.toByte())
      addDefaultValue("charValue", 'x')
      addDefaultValue("floatValue", Math.E.toFloat())
      addDefaultValue("doubleValue", Math.PI)
      addDefaultValue("intValue", 42)
      addDefaultValue("longValue", 42L)
      addDefaultValue("shortValue", 42.toShort())
      addDefaultValue("stringValue", "x")
      addDefaultValue("annotationValue", innerAnnotation)
      addDefaultValue("booleanArrayValue", booleanArrayOf(true))
      addDefaultValue("byteArrayValue", byteArrayOf(42.toByte()))
      addDefaultValue("charArrayValue", charArrayOf('x'))
      addDefaultValue("floatArrayValue", floatArrayOf(Math.E.toFloat()))
      addDefaultValue("doubleArrayValue", doubleArrayOf(Math.PI))
      addDefaultValue("intArrayValue", intArrayOf(42))
      addDefaultValue("longArrayValue", longArrayOf(42L))
      addDefaultValue("shortArrayValue", shortArrayOf(42.toShort()))
      addDefaultValue("stringArrayValue", arrayOf("x"))
      addDefaultValue("annotationArrayValue", arrayOf(innerAnnotation)).build()
    }
    assertEquals("VariousValuesAnnotation", annotation.type.internalName)
    assertEquals(20, annotation.values.size.toLong())
    assertEquals(true, annotation.values["booleanValue"])
    assertEquals(42.toByte(), annotation.values["byteValue"])
    assertEquals('x', annotation.values["charValue"])
    assertEquals(Math.E.toFloat(), annotation.values["floatValue"])
    assertEquals(Math.PI, annotation.values["doubleValue"])
    assertEquals(42, annotation.values["intValue"])
    assertEquals(42L, annotation.values["longValue"])
    assertEquals(42.toShort(), annotation.values["shortValue"])
    assertEquals("x", annotation.values["stringValue"])
    assertEquals(innerAnnotation, annotation.values["annotationValue"])
    assertArrayEquals(booleanArrayOf(true), annotation.values["booleanArrayValue"] as BooleanArray)
    assertArrayEquals(byteArrayOf(42.toByte()), annotation.values["byteArrayValue"] as ByteArray)
    assertArrayEquals(charArrayOf('x'), annotation.values["charArrayValue"] as CharArray)
    assertArrayEquals(floatArrayOf(Math.E.toFloat()), annotation.values["floatArrayValue"] as FloatArray, 0f)
    assertArrayEquals(doubleArrayOf(Math.PI), annotation.values["doubleArrayValue"] as DoubleArray, 0.0)
    assertArrayEquals(intArrayOf(42), annotation.values["intArrayValue"] as IntArray)
    assertArrayEquals(longArrayOf(42L), annotation.values["longArrayValue"] as LongArray)
    assertArrayEquals(shortArrayOf(42.toShort()), annotation.values["shortArrayValue"] as ShortArray)
    @Suppress("UNCHECKED_CAST")
    assertArrayEquals(arrayOf("x"), annotation.values["stringArrayValue"] as Array<String>)
    @Suppress("UNCHECKED_CAST")
    assertArrayEquals(arrayOf(innerAnnotation), annotation.values["annotationArrayValue"] as Array<Any>)
  }

  @Test
  @Throws(Exception::class)
  fun testResolvedAnnotation() {
    val annotation = newAnnotationBuilder("ResolvedAnnotation").build()
    assertEquals("ResolvedAnnotation", annotation.type.internalName)
    assertTrue(annotation.values.isEmpty())
  }

  @Test
  @Throws(Exception::class)
  fun testPreservesOrder() {
    val builder = newAnnotationBuilder("PreservesOrder")
    for (i in 0..99) {
      builder.addDefaultValue("value$i", "Value$i")
    }

    val annotation = builder.build()
    annotation.values.keys.forEachIndexed { i, name ->
      assertEquals("value$i", name)
    }
    annotation.values.values.forEachIndexed { i, value ->
      assertEquals("Value$i", value)
    }
    annotation.values.entries.forEachIndexed { i, entry ->
      assertEquals("value$i", entry.key)
      assertEquals("Value$i", entry.value)
    }
  }

  private fun newAnnotationBuilder(annotationName: String): AnnotationDataBuilder {
    val type = Type.getObjectType(annotationName)
    return AnnotationDataBuilder(type)
  }
}
