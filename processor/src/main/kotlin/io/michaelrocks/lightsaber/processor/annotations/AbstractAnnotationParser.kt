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

import io.michaelrocks.lightsaber.processor.descriptors.EnumValueDescriptor
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

abstract class AbstractAnnotationParser protected constructor(
    protected val annotationRegistry: AnnotationRegistry
) : AnnotationVisitor(Opcodes.ASM5) {

  override fun visit(name: String?, value: Any) {
    addValue(name, value)
  }

  override fun visitEnum(name: String?, desc: String, value: String) {
    addValue(name, EnumValueDescriptor(desc, value))
  }

  override fun visitAnnotation(name: String?, desc: String): AnnotationVisitor {
    val parent = this
    return object : AnnotationInstanceParser(annotationRegistry, Type.getType(desc)) {
      override fun visitEnd() {
        parent.addValue(name, toAnnotation())
      }
    }
  }

  override fun visitArray(name: String?): AnnotationVisitor {
    val parent = this
    return object : AnnotationArrayParser(annotationRegistry) {
      override fun visitEnd() {
        parent.addValue(name, values)
      }
    }
  }

  protected abstract fun addValue(name: String?, value: Any)
}
