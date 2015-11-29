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

import org.objectweb.asm.*
import java.util.*

class CompositeMethodVisitor : MethodVisitor(Opcodes.ASM5), CompositeVisitor<MethodVisitor> {
  override val visitors = ArrayList<MethodVisitor>()

  override fun addVisitor(visitor: MethodVisitor) {
    visitors.add(visitor)
  }

  override fun visitParameter(name: String, access: Int) =
      forEachVisitor { visitParameter(name, access) }

  override fun visitAnnotationDefault(): AnnotationVisitor? =
      addVisitorsTo(CompositeAnnotationVisitor()) { visitAnnotationDefault() }

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? =
      addVisitorsTo(CompositeAnnotationVisitor()) { visitAnnotation(desc, visible) }

  override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, desc: String,
      visible: Boolean): AnnotationVisitor? {
    return addVisitorsTo(CompositeAnnotationVisitor()) { visitTypeAnnotation(typeRef, typePath, desc, visible) }
  }

  override fun visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean): AnnotationVisitor? =
      addVisitorsTo(CompositeAnnotationVisitor()) { visitParameterAnnotation(parameter, desc, visible) }

  override fun visitAttribute(attr: Attribute) =
      forEachVisitor { visitAttribute(attr) }

  override fun visitCode() =
      forEachVisitor { visitCode() }

  override fun visitFrame(type: Int, nLocal: Int, local: Array<Any>?, nStack: Int, stack: Array<Any>?) =
      forEachVisitor { visitFrame(type, nLocal, local, nStack, stack) }

  override fun visitInsn(opcode: Int) =
      forEachVisitor { visitInsn(opcode) }

  override fun visitIntInsn(opcode: Int, operand: Int) =
      forEachVisitor { visitIntInsn(opcode, operand) }

  override fun visitVarInsn(opcode: Int, operand: Int) =
      forEachVisitor { visitVarInsn(opcode, operand) }

  override fun visitTypeInsn(opcode: Int, type: String) =
      forEachVisitor { visitTypeInsn(opcode, type) }

  override fun visitFieldInsn(opcode: Int, owner: String, name: String, desc: String) =
      forEachVisitor { visitFieldInsn(opcode, owner, name, desc) }

  @Suppress("DEPRECATION")
  override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String) =
      forEachVisitor {
        visitMethodInsn(opcode, owner, name, desc)
      }

  override fun visitMethodInsn(opcode: Int, owner: String, name: String, desc: String, itf: Boolean) =
      forEachVisitor { visitMethodInsn(opcode, owner, name, desc, itf) }

  override fun visitInvokeDynamicInsn(name: String, desc: String, bsm: Handle, vararg bsmArgs: Any) =
      forEachVisitor { visitInvokeDynamicInsn(name, desc, bsm, *bsmArgs) }

  override fun visitJumpInsn(opcode: Int, label: Label) =
      forEachVisitor { visitJumpInsn(opcode, label) }

  override fun visitLabel(label: Label) =
      forEachVisitor { visitLabel(label) }

  override fun visitLdcInsn(cst: Any) =
      forEachVisitor { visitLdcInsn(cst) }

  override fun visitIincInsn(operand: Int, increment: Int) =
      forEachVisitor { visitIincInsn(operand, increment) }

  override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label, vararg labels: Label) =
      forEachVisitor { visitTableSwitchInsn(min, max, dflt, *labels) }

  override fun visitLookupSwitchInsn(dflt: Label, keys: IntArray, labels: Array<Label>) =
      forEachVisitor { visitLookupSwitchInsn(dflt, keys, labels) }

  override fun visitMultiANewArrayInsn(desc: String, dims: Int) =
      forEachVisitor { visitMultiANewArrayInsn(desc, dims) }

  override fun visitInsnAnnotation(typeRef: Int, typePath: TypePath?, desc: String,
      visible: Boolean): AnnotationVisitor? {
    return addVisitorsTo(CompositeAnnotationVisitor()) { visitInsnAnnotation(typeRef, typePath, desc, visible) }
  }

  override fun visitTryCatchBlock(start: Label, end: Label, handler: Label, type: String?) =
      forEachVisitor { visitTryCatchBlock(start, end, handler, type) }

  override fun visitTryCatchAnnotation(typeRef: Int, typePath: TypePath?, desc: String,
      visible: Boolean): AnnotationVisitor? {
    return addVisitorsTo(CompositeAnnotationVisitor()) { visitTryCatchAnnotation(typeRef, typePath, desc, visible) }
  }

  override fun visitLocalVariable(name: String, desc: String, signature: String?, start: Label, end: Label,
      index: Int) {
    forEachVisitor { visitLocalVariable(name, desc, signature, start, end, index) }
  }

  override fun visitLocalVariableAnnotation(typeRef: Int, typePath: TypePath?, start: Array<Label>, end: Array<Label>,
      index: IntArray, desc: String, visible: Boolean): AnnotationVisitor? {
    return addVisitorsTo(CompositeAnnotationVisitor()) {
      visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible)
    }
  }

  override fun visitLineNumber(line: Int, start: Label) =
      forEachVisitor { visitLineNumber(line, start) }

  override fun visitMaxs(maxStack: Int, maxLocals: Int) =
      forEachVisitor { visitMaxs(maxStack, maxLocals) }

  override fun visitEnd() =
      forEachVisitor { visitEnd() }
}
