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
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedFieldDescriptor
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Type

internal class ModuleFieldAnalyzer(
    processorContext: ProcessorContext,
    private val moduleBuilder: ModuleDescriptor.Builder,
    private val fieldName: String,
    private val fieldDesc: String
) : ProcessorFieldVisitor(processorContext) {

  private var isProviderField: Boolean = false
  private var qualifier: AnnotationData? = null

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
    val annotationType = Type.getType(desc)
    if (Types.PROVIDES_TYPE == annotationType) {
      isProviderField = true
    } else if (processorContext.isQualifier(annotationType)) {
      if (qualifier == null) {
        return AnnotationInstanceParser(processorContext.annotationRegistry, annotationType) { annotation ->
          qualifier = annotation
        }
      } else {
        reportError("Field has multiple qualifier annotations: ${moduleBuilder.moduleType}.$fieldName: $fieldDesc")
      }
    }

    return null
  }

  override fun visitEnd() {
    if (isProviderField) {
      val providerField = FieldDescriptor(fieldName, fieldDesc)
      val qualifiedProviderField = QualifiedFieldDescriptor(providerField, qualifier)
      moduleBuilder.addProviderField(qualifiedProviderField)
    }
  }
}
