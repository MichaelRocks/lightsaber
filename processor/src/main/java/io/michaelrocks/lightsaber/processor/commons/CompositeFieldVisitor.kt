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

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Attribute
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.TypePath
import java.util.ArrayList

class CompositeFieldVisitor : FieldVisitor(Opcodes.ASM5), CompositeVisitor<FieldVisitor> {
  override val visitors = ArrayList<FieldVisitor>()

  override fun addVisitor(visitor: FieldVisitor) {
    visitors.add(visitor)
  }

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? =
      addVisitorsTo(CompositeAnnotationVisitor()) { visitAnnotation(desc, visible) }

  override fun visitTypeAnnotation(typeRef: Int, typePath: TypePath?, desc: String,
      visible: Boolean): AnnotationVisitor? {
    return addVisitorsTo(CompositeAnnotationVisitor()) { visitTypeAnnotation(typeRef, typePath, desc, visible) }
  }

  override fun visitAttribute(attr: Attribute) =
      forEachVisitor { visitAttribute(attr) }

  override fun visitEnd() =
      forEachVisitor { visitEnd() }
}
