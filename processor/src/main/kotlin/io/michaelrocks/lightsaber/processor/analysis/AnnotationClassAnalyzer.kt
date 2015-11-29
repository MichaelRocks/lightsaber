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

import io.michaelrocks.lightsaber.processor.ProcessorClassVisitor
import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.annotations.AnnotationClassVisitor
import io.michaelrocks.lightsaber.processor.commons.descriptor
import org.objectweb.asm.AnnotationVisitor
import javax.inject.Qualifier

class AnnotationClassAnalyzer private constructor(
    processorContext: ProcessorContext,
    private val annotationClassVisitor: AnnotationClassVisitor
) : ProcessorClassVisitor(processorContext, annotationClassVisitor) {

  companion object {
    private val QUALIFIER_DESCRIPTOR = Qualifier::class.descriptor
  }

  private var isQualifier: Boolean = false

  constructor(processorContext: ProcessorContext) : this(processorContext, AnnotationClassVisitor())

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?,
      interfaces: Array<String>?) {
    super.visit(version, access, name, signature, superName, interfaces)
    isQualifier = false
  }

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
    if (QUALIFIER_DESCRIPTOR == desc) {
      isQualifier = true
    }
    return super.visitAnnotation(desc, visible)
  }

  override fun visitEnd() {
    super.visitEnd()

    val annotation = annotationClassVisitor.toAnnotationDescriptor()
    val data = annotationClassVisitor.toAnnotationData()
    processorContext.annotationRegistry.addAnnotationDefaults(annotation, data)
    if (isQualifier) {
      processorContext.addQualifier(annotation.type)
    }
  }
}
