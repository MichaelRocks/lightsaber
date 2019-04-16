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

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Attribute
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.TypePath
import java.util.ArrayList

class CompositeClassVisitor : ClassVisitor(Opcodes.ASM5), CompositeVisitor<ClassVisitor> {
  override val visitors = ArrayList<ClassVisitor>()

  override fun addVisitor(visitor: ClassVisitor) {
    visitors.add(visitor)
  }

  override fun visit(
    version: Int,
    access: Int,
    name: String,
    signature: String?,
    superName: String?,
    interfaces: Array<String>?
  ) {
    forEachVisitor { visit(version, access, name, signature, superName, interfaces) }
  }

  override fun visitSource(source: String?, debug: String?) =
    forEachVisitor { visitSource(source, debug) }

  override fun visitOuterClass(owner: String, name: String?, desc: String?) =
    forEachVisitor { visitOuterClass(owner, name, desc) }

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? =
    addVisitorsTo(CompositeAnnotationVisitor()) { visitAnnotation(desc, visible) }

  override fun visitTypeAnnotation(
    typeRef: Int,
    typePath: TypePath?,
    desc: String,
    visible: Boolean
  ): AnnotationVisitor? {
    return addVisitorsTo(CompositeAnnotationVisitor()) { visitTypeAnnotation(typeRef, typePath, desc, visible) }
  }

  override fun visitAttribute(attr: Attribute) =
    forEachVisitor { visitAttribute(attr) }

  override fun visitInnerClass(name: String, outerName: String?, innerName: String?, access: Int) =
    forEachVisitor { visitInnerClass(name, outerName, innerName, access) }

  override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? =
    addVisitorsTo(CompositeFieldVisitor()) { visitField(access, name, desc, signature, value) }

  override fun visitMethod(
    access: Int,
    name: String,
    desc: String,
    signature: String?,
    exceptions: Array<String>?
  ): MethodVisitor? {
    return addVisitorsTo(CompositeMethodVisitor()) { visitMethod(access, name, desc, signature, exceptions) }
  }

  override fun visitEnd() =
    forEachVisitor { visitEnd() }
}
