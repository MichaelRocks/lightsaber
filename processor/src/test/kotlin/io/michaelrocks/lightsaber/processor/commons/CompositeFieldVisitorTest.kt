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

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.objectweb.asm.Attribute
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.TypeReference

class CompositeFieldVisitorTest {
  @Test
  fun testIsEmpty() {
    val compositeFieldVisitor = CompositeFieldVisitor()
    assertTrue(compositeFieldVisitor.isEmpty)
  }

  @Test
  fun testIsNotEmpty() {
    val compositeFieldVisitor = CompositeFieldVisitor()
    compositeFieldVisitor.addVisitor(mock<FieldVisitor>(FieldVisitor::class.java))
    assertFalse(compositeFieldVisitor.isEmpty)
  }

  @Test
  fun testEmptyVisit() {
    val compositeFieldVisitor = CompositeFieldVisitor()
    compositeFieldVisitor.visitEnd()
  }

  @Test
  fun testVisitAnnotation() {
    verifyCompositeMethodInvocations(CompositeClassVisitor::class,
        { visitAnnotation("Desc", true) },
        { visitEnd() }
    )
  }

  @Test
  fun testVisitTypeAnnotation() {
    verifyCompositeMethodInvocations(CompositeClassVisitor::class,
        { visitTypeAnnotation(TypeReference.FIELD, null, "Desc", true) },
        { visitEnd() }
    )
  }

  @Test
  fun testVisitAttribute() {
    val attribute = object : Attribute("AttributeType") {}
    verifyMethodInvocations(CompositeFieldVisitor::class) { visitAttribute(attribute) }
  }

  @Test
  fun testVisitEnd() {
    verifyMethodInvocations(CompositeFieldVisitor::class) { visitEnd() }
  }
}
