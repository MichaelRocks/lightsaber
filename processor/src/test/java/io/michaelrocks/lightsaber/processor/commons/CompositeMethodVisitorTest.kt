/*
 * Copyright 2019 Michael Rozumyanskiy
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
import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.TypeReference

class CompositeMethodVisitorTest {
  @Test
  fun testIsEmpty() {
    val compositeMethodVisitor = CompositeMethodVisitor()
    assertTrue(compositeMethodVisitor.isEmpty)
  }

  @Test
  fun testIsNotEmpty() {
    val compositeMethodVisitor = CompositeMethodVisitor()
    compositeMethodVisitor.addVisitor(mock<MethodVisitor>(MethodVisitor::class.java))
    assertFalse(compositeMethodVisitor.isEmpty)
  }

  @Test
  fun testEmptyVisit() {
    val compositeMethodVisitor = CompositeMethodVisitor()
    compositeMethodVisitor.visitEnd()
  }

  @Test
  fun testVisitParameter() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitParameter("Name", Opcodes.ACC_FINAL) }
  }

  @Test
  fun testVisitAnnotationDefault() {
    verifyCompositeMethodInvocations(CompositeMethodVisitor::class,
      { visitAnnotationDefault() },
      { visitEnd() }
    )
  }

  @Test
  fun testVisitAnnotation() {
    verifyCompositeMethodInvocations(CompositeMethodVisitor::class,
      { visitAnnotation("Desc", true) },
      { visitEnd() }
    )
  }

  @Test
  fun testVisitTypeAnnotation() {
    verifyCompositeMethodInvocations(CompositeMethodVisitor::class,
      { visitTypeAnnotation(TypeReference.METHOD_TYPE_PARAMETER, null, "Desc", true) },
      { visitEnd() }
    )
  }

  @Test
  fun testVisitParameterAnnotation() {
    verifyCompositeMethodInvocations(CompositeMethodVisitor::class,
      { visitParameterAnnotation(1, "Desc", true) },
      { visitEnd() }
    )
  }

  @Test
  fun testVisitAttribute() {
    val attribute = object : Attribute("AttributeType") {

    }
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitAttribute(attribute) }
  }

  @Test
  fun testVisitCode() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitCode() }
  }

  @Test
  fun testVisitFrame() {
    val local = arrayOf<Any>()
    val stack = arrayOf<Any>(Object())
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitFrame(Opcodes.F_NEW, 0, local, 1, stack) }
  }

  @Test
  fun testVisitInsn() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitInsn(Opcodes.AALOAD) }
  }

  @Test
  fun testVisitIntInsn() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitIntInsn(Opcodes.NEWARRAY, 1) }
  }

  @Test
  fun testVisitVarInsn() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitVarInsn(Opcodes.ILOAD, 1) }
  }

  @Test
  fun testVisitTypeInsn() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitTypeInsn(Opcodes.NEW, "Type") }
  }

  @Test
  fun testVisitFieldInsn() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitFieldInsn(Opcodes.GETFIELD, "Owner", "Name", "Desc") }
  }

  @Test
  fun testVisitMethodInsn() {
    verifyMethodInvocations(CompositeMethodVisitor::class) {
      @Suppress("DEPRECATION")
      visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Owner", "Name", "Desc")
    }
    verifyMethodInvocations(CompositeMethodVisitor::class) {
      visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Owner", "Name", "Desc", true)
    }
  }

  @Test
  fun testVisitInvokeDynamicInsn() {
    val bootstrapMethod = Handle(Opcodes.H_INVOKEINTERFACE, "Owner", "Name", "Desc", true)
    val arguments = arrayOf<Any>("Argument")
    verifyMethodInvocations(CompositeMethodVisitor::class) {
      visitInvokeDynamicInsn("Name", "Desc", bootstrapMethod, *arguments)
    }
  }

  @Test
  fun testVisitJumpInsn() {
    val label = Label()
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitJumpInsn(Opcodes.IFEQ, label) }
  }

  @Test
  fun testVisitLabel() {
    val label = Label()
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitLabel(label) }
  }

  @Test
  fun testVisitLdcInsn() {
    val `object` = Object()
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitLdcInsn(`object`) }
  }

  @Test
  fun testVisitIincInsn() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitIincInsn(1, 2) }
  }

  @Test
  fun testVisitTableSwitchInsn() {
    val label = Label()
    val labels = arrayOf(Label())
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitTableSwitchInsn(1, 2, label, *labels) }
  }

  @Test
  fun testVisitLookupSwitchInsn() {
    val label = Label()
    val keys = intArrayOf(1, 2)
    val labels = arrayOf(Label())
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitLookupSwitchInsn(label, keys, labels) }
  }

  @Test
  fun testVisitMultiANewArrayInsn() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitMultiANewArrayInsn("Desc", 2) }
  }

  @Test
  fun testVisitInsnAnnotation() {
    verifyCompositeMethodInvocations(CompositeMethodVisitor::class,
      { visitInsnAnnotation(TypeReference.METHOD_TYPE_PARAMETER, null, "Desc", true) },
      { visitEnd() }
    )
  }

  @Test
  fun testVisitTryCatchBlock() {
    val start = Label()
    val end = Label()
    val handler = Label()
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitTryCatchBlock(start, end, handler, "Type") }
  }

  @Test
  fun testVisitTryCatchAnnotation() {
    verifyCompositeMethodInvocations(CompositeMethodVisitor::class,
      { visitTryCatchAnnotation(TypeReference.METHOD_TYPE_PARAMETER, null, "Desc", true) },
      { visitEnd() }
    )
  }

  @Test
  fun testVisitLocalVariable() {
    val start = Label()
    val end = Label()
    verifyMethodInvocations(CompositeMethodVisitor::class) {
      visitLocalVariable("Name", "Desc", "Signature", start, end, 2)
    }
  }

  @Test
  fun testVisitLocalVariableAnnotation() {
    val start = arrayOf(Label())
    val end = arrayOf(Label())
    val index = intArrayOf(1, 2)
    verifyCompositeMethodInvocations(CompositeMethodVisitor::class,
      { visitLocalVariableAnnotation(TypeReference.METHOD_TYPE_PARAMETER, null, start, end, index, "Desc", true) },
      { visitEnd() }
    )
  }

  @Test
  fun testVisitLineNumber() {
    val label = Label()
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitLineNumber(2, label) }
  }

  @Test
  fun testVisitMaxs() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitMaxs(1, 2) }
  }

  @Test
  fun testVisitEnd() {
    verifyMethodInvocations(CompositeMethodVisitor::class) { visitEnd() }
  }
}
