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

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Type

fun AnnotationInstanceParser(
    annotationRegistry: AnnotationRegistry,
    annotationType: Type,
    callback: (annotation: AnnotationData) -> Unit
): AnnotationVisitor {
  return object : AnnotationInstanceParser(annotationRegistry, annotationType) {
    override fun visitEnd() {
      callback(toAnnotation())
    }
  }
}

open class AnnotationInstanceParser(
    annotationRegistry: AnnotationRegistry,
    annotationType: Type
) : AbstractAnnotationParser(annotationRegistry) {

  private val annotationBuilder =
      AnnotationDataBuilder(annotationType, annotationRegistry.findAnnotationDefaults(annotationType))

  fun toAnnotation(): AnnotationData {
    return annotationBuilder.build()
  }

  override fun addValue(name: String?, value: Any) {
    annotationBuilder.addDefaultValue(name!!, value)
  }
}
