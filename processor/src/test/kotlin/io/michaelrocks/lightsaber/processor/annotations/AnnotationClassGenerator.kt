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
import io.michaelrocks.lightsaber.processor.descriptors.EnumValueDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.descriptor
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

class AnnotationClassGenerator private constructor(private val classVisitor: ClassVisitor) {
  companion object {
    fun create(classVisitor: ClassVisitor, annotationType: Type): AnnotationClassGenerator {
      val builder = AnnotationClassGenerator(classVisitor)
      classVisitor.visit(
          V1_6,
          ACC_PUBLIC or ACC_ANNOTATION or ACC_ABSTRACT or ACC_INTERFACE,
          annotationType.internalName,
          null,
          getType<Any>().internalName,
          arrayOf(getType<Annotation>().internalName))
      return builder
    }
  }

  fun addMethod(name: String, type: Type): AnnotationClassGenerator {
    addMethod(name, type, null)
    return this
  }

  fun addMethod(name: String, type: Type, defaultValue: Any?): AnnotationClassGenerator {
    addMethod(MethodDescriptor.forMethod(name, type), defaultValue)
    return this
  }

  private fun addMethod(method: MethodDescriptor, defaultValue: Any?) {
    val methodVisitor = classVisitor.visitMethod(
        ACC_PUBLIC or ACC_ABSTRACT,
        method.name,
        method.descriptor,
        null,
        null)
    if (defaultValue != null) {
      val annotationVisitor = methodVisitor.visitAnnotationDefault()
      addValue(annotationVisitor, null, defaultValue)
      annotationVisitor.visitEnd()
    }
    methodVisitor.visitEnd()
  }

  private fun addValue(annotationVisitor: AnnotationVisitor, name: String?, defaultValue: Any) {
    if (defaultValue is EnumValueDescriptor) {
      annotationVisitor.visitEnum(name, defaultValue.type.descriptor, defaultValue.value)
    } else if (defaultValue is AnnotationData) {
      val innerAnnotationVisitor = annotationVisitor.visitAnnotation(name, defaultValue.type.descriptor)
      for (entry in defaultValue.values.entries) {
        addValue(innerAnnotationVisitor, entry.key, entry.value)
      }
      innerAnnotationVisitor.visitEnd()
    } else if (defaultValue is Array<*> &&
        (defaultValue.isArrayOf<EnumValueDescriptor>() || defaultValue.isArrayOf<AnnotationData>())) {
      val innerAnnotationVisitor = annotationVisitor.visitArray(name)
      defaultValue.forEach { value -> addValue(innerAnnotationVisitor, null, value!!) }
      innerAnnotationVisitor.visitEnd()
    } else {
      annotationVisitor.visit(name, defaultValue)
    }
  }

  fun generate() {
    classVisitor.visitEnd()
  }
}
