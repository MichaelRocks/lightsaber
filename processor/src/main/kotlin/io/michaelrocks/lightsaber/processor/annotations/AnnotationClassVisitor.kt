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

import org.objectweb.asm.*

class AnnotationClassVisitor : ClassVisitor(Opcodes.ASM5) {
  private var annotationDescriptorBuilder: AnnotationDescriptorBuilder? = null
  private var annotationDataBuilder: AnnotationDataBuilder? = null

  fun toAnnotationDescriptor(): AnnotationDescriptor {
    return annotationDescriptorBuilder!!.build()
  }

  fun toAnnotationData(): AnnotationData {
    return annotationDataBuilder!!.build()
  }

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?,
      interfaces: Array<String>?) {
    val annotationType = Type.getObjectType(name)
    annotationDescriptorBuilder = AnnotationDescriptorBuilder(annotationType)
    annotationDataBuilder = AnnotationDataBuilder(annotationType)
  }

  override fun visitMethod(access: Int, name: String, desc: String, signature: String?,
      exceptions: Array<String>?): MethodVisitor {
    annotationDescriptorBuilder!!.addField(name, Type.getReturnType(desc))
    return object : MethodVisitor(Opcodes.ASM5) {
      override fun visitAnnotationDefault(): AnnotationVisitor {
        return object : AnnotationValueParser() {
          override fun visitEnd() {
            annotationDataBuilder!!.addDefaultValue(name, value!!)
          }
        }
      }
    }
  }
}
