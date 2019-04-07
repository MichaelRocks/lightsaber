/*
 * Copyright 2018 Michael Rozumyanskiy
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
import org.objectweb.asm.AnnotationVisitor

class CompositeAnnotationVisitorTest {
  @Test
  fun testIsEmpty() {
    val compositeAnnotationVisitor = CompositeAnnotationVisitor()
    assertTrue(compositeAnnotationVisitor.isEmpty)
  }

  @Test
  fun testIsNotEmpty() {
    val compositeAnnotationVisitor = CompositeAnnotationVisitor()
    compositeAnnotationVisitor.addVisitor(mock<AnnotationVisitor>(AnnotationVisitor::class.java))
    assertFalse(compositeAnnotationVisitor.isEmpty)
  }

  @Test
  fun testEmptyVisit() {
    val compositeAnnotationVisitor = CompositeAnnotationVisitor()
    compositeAnnotationVisitor.visitEnd()
  }

  @Test
  fun testVisit() {
    val value = Object()
    verifyMethodInvocations(CompositeAnnotationVisitor::class) { visit("Name", value) }
  }

  @Test
  fun testVisitEnum() {
    verifyMethodInvocations(CompositeAnnotationVisitor::class) { visitEnum("Name", "Desc", "Value") }
  }

  @Test
  fun testVisitAnnotation() {
    verifyCompositeMethodInvocations(CompositeAnnotationVisitor::class,
        { visitAnnotation("Name", "Desc") },
        { visitEnd() }
    )
  }

  @Test
  fun testVisitArray() {
    verifyCompositeMethodInvocations(CompositeAnnotationVisitor::class,
        { visitArray("Name") },
        { visitEnd() }
    )
  }

  @Test
  fun testVisitEnd() {
    verifyMethodInvocations(CompositeAnnotationVisitor::class) { visitEnd() }
  }
}
