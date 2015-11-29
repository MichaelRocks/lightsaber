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

package io.michaelrocks.lightsaber.processor.analysis

import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.ProcessorFieldVisitor
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData
import io.michaelrocks.lightsaber.processor.annotations.AnnotationInstanceParser
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedFieldDescriptor
import io.michaelrocks.lightsaber.processor.signature.TypeSignatureParser
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

internal class InjectionFieldAnalyzer(
    processorContext: ProcessorContext,
    private val injectionTargetBuilder: InjectionTargetDescriptor.Builder,
    private val access: Int,
    private val fieldName: String,
    private val fieldDesc: String,
    private val signature: String?
) : ProcessorFieldVisitor(processorContext) {

  private var isInjectableField: Boolean = false
  private var qualifier: AnnotationData? = null

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
    val annotationType = Type.getType(desc)
    if (Types.INJECT_TYPE == annotationType) {
      isInjectableField = true
    } else if (processorContext.isQualifier(annotationType)) {
      return AnnotationInstanceParser(annotationType) { annotation ->
        val resolvedAnnotation = processorContext.annotationRegistry.resolveAnnotation(annotation)
        if (qualifier == null) {
          qualifier = resolvedAnnotation
        } else {
          reportError("Field has multiple qualifiers: ${injectionTargetBuilder.targetType}.$fieldName: $fieldDesc")
        }
      }
    }
    return null
  }

  override fun visitEnd() {
    if (isInjectableField) {
      val fieldType = Type.getType(fieldDesc)
      val typeSignature = TypeSignatureParser.parseTypeSignature(processorContext, signature, fieldType)
      val field = FieldDescriptor(fieldName, typeSignature)
      val qualifiedField = QualifiedFieldDescriptor(field, qualifier)
      if ((access and Opcodes.ACC_STATIC) == 0) {
        injectionTargetBuilder.addInjectableField(qualifiedField)
      } else {
        injectionTargetBuilder.addInjectableStaticField(qualifiedField)
      }
    }
  }
}
