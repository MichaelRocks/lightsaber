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
import io.michaelrocks.lightsaber.processor.ProcessorMethodVisitor
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData
import io.michaelrocks.lightsaber.processor.annotations.AnnotationInstanceParser
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.ScopeDescriptor
import io.michaelrocks.lightsaber.processor.signature.MethodSignatureParser
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Type
import java.util.*

internal class ModuleMethodAnalyzer(
    processorContext: ProcessorContext,
    private val moduleBuilder: ModuleDescriptor.Builder,
    private val methodName: String,
    private val methodDesc: String,
    private val signature: String?
) : ProcessorMethodVisitor(processorContext) {

  private var isProviderMethod: Boolean = false
  private var resultQualifier: AnnotationData? = null
  private val parameterQualifiers = HashMap<Int, AnnotationData>()
  private var scope: ScopeDescriptor? = null

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor ? {
    val annotationType = Type.getType(desc)
    if (Types.PROVIDES_TYPE == annotationType) {
      isProviderMethod = true
    } else if (processorContext.isQualifier(annotationType)) {
      if (resultQualifier == null) {
        return AnnotationInstanceParser(annotationType) { annotation ->
          resultQualifier = processorContext.annotationRegistry.resolveAnnotation(annotation)
        }
      } else {
        reportError("Method has multiple qualifier annotations: ${moduleBuilder.moduleType}.$methodName$methodDesc")
      }
    } else if (scope == null) {
      scope = processorContext.findScopeByAnnotationType(annotationType)
    } else if (processorContext.findScopeByAnnotationType(annotationType) != null) {
      reportError("Method has multiple scope annotations: ${moduleBuilder.moduleType}.$methodName$methodDesc")
    }

    return null
  }

  override fun visitParameterAnnotation(parameter: Int, desc: String, visible: Boolean): AnnotationVisitor? {
    if (isProviderMethod) {
      val annotationType = Type.getType(desc)
      if (processorContext.isQualifier(annotationType)) {
        return AnnotationInstanceParser(annotationType) { annotation ->
          val resolvedAnnotation = processorContext.annotationRegistry.resolveAnnotation(annotation)
          if (parameterQualifiers.put(parameter, resolvedAnnotation) != null) {
            reportError(
                "Method parameter $parameter has multiple qualifiers: " +
                    "${moduleBuilder.moduleType}.$methodName$methodDesc"
            )
          }
        }
      }
    }
    return null
  }

  override fun visitEnd() {
    if (isProviderMethod) {
      val methodType = Type.getMethodType(methodDesc)
      val methodSignature = MethodSignatureParser.parseMethodSignature(processorContext, signature, methodType)
      val providerMethod = MethodDescriptor(methodName, methodSignature)
      val qualifiedProviderMethod = QualifiedMethodDescriptor(providerMethod, parameterQualifiers, resultQualifier)
      moduleBuilder.addProviderMethod(qualifiedProviderMethod, scope)
    }
  }
}
