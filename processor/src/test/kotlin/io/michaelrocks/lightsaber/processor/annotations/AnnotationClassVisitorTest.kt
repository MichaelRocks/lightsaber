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
import io.michaelrocks.lightsaber.processor.descriptors.EnumValueDescriptor
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.objectweb.asm.Type

class AnnotationClassVisitorTest {
  @Test
  fun testEmptyAnnotation() {
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("EmptyAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).generate()
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertTrue(actualAnnotation.values.isEmpty())
  }

  @Test
  fun testExplicitValueAnnotation() {
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("ExplicitValueAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).apply {
      addMethod("explicitValue", getType<String>())
      generate()
    }
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertTrue(actualAnnotation.values.isEmpty())
  }

  @Test
  fun testImplicitValueAnnotation() {
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("ImplicitValueAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).apply {
      addMethod("implicitValue", getType<String>(), "defaultImplicitValue")
      generate()
    }
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals("defaultImplicitValue", actualAnnotation.values["implicitValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testExplicitAndImplicitValuesAnnotation() {
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("ExplicitAndImplicitValuesAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).apply {
      addMethod("explicitValue", getType<String>())
      addMethod("implicitValue", getType<String>(), "defaultImplicitValue")
      generate()
    }
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals("defaultImplicitValue", actualAnnotation.values["implicitValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testSimpleValuesAnnotation() {
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("PrimitiveValuesAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).apply {
      addMethod("booleanValue", Type.BOOLEAN_TYPE, true)
      addMethod("byteValue", Type.BYTE_TYPE, 42.toByte())
      addMethod("charValue", Type.CHAR_TYPE, 'x')
      addMethod("floatValue", Type.FLOAT_TYPE, Math.E.toFloat())
      addMethod("doubleValue", Type.DOUBLE_TYPE, Math.PI)
      addMethod("intValue", Type.INT_TYPE, 42)
      addMethod("longValue", Type.LONG_TYPE, 42L)
      addMethod("shortValue", Type.SHORT_TYPE, 42.toShort())
      addMethod("stringValue", getType<String>(), "x")
      generate()
    }
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(true, actualAnnotation.values["booleanValue"])
    assertEquals(42.toByte(), actualAnnotation.values["byteValue"])
    assertEquals('x', actualAnnotation.values["charValue"])
    assertEquals(Math.E.toFloat(), actualAnnotation.values["floatValue"] as Float, 0f)
    assertEquals(Math.PI, actualAnnotation.values["doubleValue"] as Double, 0.0)
    assertEquals(42, actualAnnotation.values["intValue"])
    assertEquals(42L, actualAnnotation.values["longValue"])
    assertEquals(42.toShort(), actualAnnotation.values["shortValue"])
    assertEquals("x", actualAnnotation.values["stringValue"])
    assertEquals(9, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testArrayValuesAnnotation() {
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("ArrayValuesAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).apply {
      addMethod("booleanArrayValue", getType<BooleanArray>(), booleanArrayOf(true, false, true))
      addMethod("byteArrayValue", getType<ByteArray>(), byteArrayOf(42, 43, 44))
      addMethod("charArrayValue", getType<CharArray>(), charArrayOf('x', 'y', 'z'))
      addMethod("floatArrayValue", getType<FloatArray>(), floatArrayOf(42f, 43f, 44f))
      addMethod("doubleArrayValue", getType<DoubleArray>(), doubleArrayOf(42.0, 43.0, 44.0))
      addMethod("intArrayValue", getType<IntArray>(), intArrayOf(42, 43, 44))
      addMethod("longArrayValue", getType<LongArray>(), longArrayOf(42, 43, 44))
      addMethod("shortArrayValue", getType<ShortArray>(), shortArrayOf(42, 43, 44))
      addMethod("stringArrayValue", getType<Array<String>>(), arrayOf("x", "y", "z"))
      generate()
    }
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertArrayEquals(booleanArrayOf(true, false, true), actualAnnotation.values["booleanArrayValue"] as BooleanArray)
    assertArrayEquals(byteArrayOf(42, 43, 44), actualAnnotation.values["byteArrayValue"] as ByteArray)
    assertArrayEquals(charArrayOf('x', 'y', 'z'), actualAnnotation.values["charArrayValue"] as CharArray)
    assertArrayEquals(floatArrayOf(42f, 43f, 44f), actualAnnotation.values["floatArrayValue"] as FloatArray, 0f)
    assertArrayEquals(doubleArrayOf(42.0, 43.0, 44.0), actualAnnotation.values["doubleArrayValue"] as DoubleArray, 0.0)
    assertArrayEquals(intArrayOf(42, 43, 44), actualAnnotation.values["intArrayValue"] as IntArray)
    assertArrayEquals(longArrayOf(42, 43, 44), actualAnnotation.values["longArrayValue"] as LongArray)
    assertArrayEquals(shortArrayOf(42, 43, 44), actualAnnotation.values["shortArrayValue"] as ShortArray)
    @Suppress("UNCHECKED_CAST")
    assertArrayEquals(arrayOf("x", "y", "z"), actualAnnotation.values["stringArrayValue"] as Array<String>)
    assertEquals(9, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testEnumAnnotation() {
    val enumType = Type.getObjectType("TestEnum")
    val enumValue = EnumValueDescriptor(enumType, "TEST")
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("EnumAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).run {
      addMethod("enumValue", enumType, enumValue)
      generate()
    }
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(enumValue, actualAnnotation.values["enumValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testEnumArrayAnnotation() {
    val enumType = Type.getObjectType("TestEnum")
    val enumArrayType = Type.getType("[${enumType.descriptor}")
    val enumValues = arrayOf(
        EnumValueDescriptor(enumType, "TEST1"),
        EnumValueDescriptor(enumType, "TEST2"),
        EnumValueDescriptor(enumType, "TEST3")
    )
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("testEnumArrayAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).run {
      addMethod("enumArrayValue", enumArrayType, enumValues)
      generate()
    }
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(listOf(*enumValues), actualAnnotation.values["enumArrayValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testNestedAnnotationAnnotation() {
    val nestedAnnotation = AnnotationHelper.createAnnotationData("NestedAnnotation", "Nested")
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    `when`(annotationRegistry.findAnnotationDefaults(nestedAnnotation.type))
        .thenReturn(AnnotationData(nestedAnnotation.type, emptyMap()))
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("NestedAnnotationAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).run {
      addMethod("annotationValue", nestedAnnotation.type, nestedAnnotation)
      generate()
    }
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(nestedAnnotation, actualAnnotation.values["annotationValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }

  @Test
  fun testNestedAnnotationArrayAnnotation() {
    val nestedAnnotations = arrayOf(
        AnnotationHelper.createAnnotationData("NestedAnnotation", "Nested1"),
        AnnotationHelper.createAnnotationData("NestedAnnotation", "Nested2"),
        AnnotationHelper.createAnnotationData("NestedAnnotation", "Nested3")
    )
    val annotationArrayType = Type.getType("[${nestedAnnotations[0].type.descriptor}")
    val annotationRegistry = mock(AnnotationRegistry::class.java)
    for (nestedAnnotation in nestedAnnotations) {
      `when`(annotationRegistry.findAnnotationDefaults(nestedAnnotation.type))
          .thenReturn(AnnotationData(nestedAnnotation.type, emptyMap()))
    }
    val visitor = AnnotationClassVisitor(annotationRegistry)
    val annotationType = Type.getObjectType("NestedAnnotationArrayAnnotation")
    AnnotationClassGenerator.create(visitor, annotationType).run {
      addMethod("annotationArrayValue", annotationArrayType, nestedAnnotations)
      generate()
    }
    val actualAnnotation = visitor.toAnnotationData()
    assertEquals(annotationType, actualAnnotation.type)
    assertEquals(listOf(*nestedAnnotations), actualAnnotation.values["annotationArrayValue"])
    assertEquals(1, actualAnnotation.values.size.toLong())
  }
}
