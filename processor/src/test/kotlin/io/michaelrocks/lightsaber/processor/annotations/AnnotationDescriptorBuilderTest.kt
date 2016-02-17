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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.objectweb.asm.Type

class AnnotationDescriptorBuilderTest {
  @Test
  @Throws(Exception::class)
  fun testEmptyAnnotation() {
    val annotation = newBuilder("EmptyAnnotation").build()
    assertEquals("EmptyAnnotation", annotation.type.internalName)
    assertTrue(annotation.fields.isEmpty())
  }

  @Test
  @Throws(Exception::class)
  fun testDefaultValueAnnotation() {
    val annotation = newBuilder("DefaultValueAnnotation").addDefaultField(getType<String>()).build()
    assertEquals("DefaultValueAnnotation", annotation.type.internalName)
    assertEquals(1, annotation.fields.size.toLong())
    assertEquals(getType<String>(), annotation.fields["value"])
  }

  @Test
  @Throws(Exception::class)
  fun testNamedValueAnnotation() {
    val annotation = newBuilder("NamedValueAnnotation").addField("namedValue", getType<String>()).build()
    assertEquals("NamedValueAnnotation", annotation.type.internalName)
    assertEquals(1, annotation.fields.size.toLong())
    assertEquals(getType<String>(), annotation.fields["namedValue"])
  }

  @Test
  @Throws(Exception::class)
  fun testMultipleValuesAnnotation() {
    val annotation = newBuilder("NamedValueAnnotation").run {
      addField("namedValue1", getType<String>())
      addField("namedValue2", Type.INT_TYPE)
      addField("namedValue3", Type.getObjectType("[Z"))
      build()
    }
    assertEquals("NamedValueAnnotation", annotation.type.internalName)
    assertEquals(3, annotation.fields.size.toLong())
    assertEquals(getType<String>(), annotation.fields["namedValue1"])
    assertEquals(Type.INT_TYPE, annotation.fields["namedValue2"])
    assertEquals(Type.getObjectType("[Z"), annotation.fields["namedValue3"])
  }

  @Test
  @Throws(Exception::class)
  fun testPreservesOrder() {
    val builder = newBuilder("PreservesOrder")
    for (i in 0..99) {
      builder.addField("value$i", Type.getObjectType("Type$i"))
    }

    val annotation = builder.build()
    annotation.fields.keys.forEachIndexed { i, fieldName ->
      assertEquals("value$i", fieldName)
    }
    annotation.fields.values.forEachIndexed { i, fieldType ->
      assertEquals(Type.getObjectType("Type$i"), fieldType)
    }
    annotation.fields.entries.forEachIndexed { i, field ->
      assertEquals("value$i", field.key)
      assertEquals(Type.getObjectType("Type$i"), field.value)
    }
  }

  private fun newBuilder(annotationName: String): AnnotationDescriptorBuilder {
    val type = Type.getObjectType(annotationName)
    return AnnotationDescriptorBuilder(type)
  }
}
