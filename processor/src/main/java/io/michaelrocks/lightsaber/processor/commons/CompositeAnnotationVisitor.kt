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
import org.objectweb.asm.Opcodes
import java.util.ArrayList

class CompositeAnnotationVisitor : AnnotationVisitor(Opcodes.ASM5), CompositeVisitor<AnnotationVisitor> {
  override val visitors = ArrayList<AnnotationVisitor>()

  override fun addVisitor(visitor: AnnotationVisitor) {
    visitors.add(visitor)
  }

  override fun visit(name: String?, value: Any) =
      forEachVisitor { visit(name, value) }

  override fun visitEnum(name: String?, desc: String, value: String) =
      forEachVisitor { visitEnum(name, desc, value) }

  override fun visitAnnotation(name: String?, desc: String): AnnotationVisitor? =
      addVisitorsTo(CompositeAnnotationVisitor()) { visitAnnotation(name, desc) }

  override fun visitArray(name: String?): AnnotationVisitor? =
      addVisitorsTo(CompositeAnnotationVisitor()) { visitArray(name) }

  override fun visitEnd() =
      forEachVisitor { visitEnd() }
}
