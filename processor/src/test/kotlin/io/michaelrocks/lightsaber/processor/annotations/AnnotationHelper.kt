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

import org.objectweb.asm.Type
import java.util.*

internal object AnnotationHelper {
  fun createAnnotationDescriptor(annotationName: String): AnnotationDescriptor =
      AnnotationDescriptor(getAnnotationType(annotationName), emptyMap<String, Type>())

  fun createAnnotationDescriptor(annotationName: String, type: Type): AnnotationDescriptor =
      createAnnotationDescriptor(annotationName, "value", type)

  fun createAnnotationDescriptor(annotationName: String, name: String, type: Type): AnnotationDescriptor =
      createAnnotationDescriptor(annotationName, Collections.singletonMap(name, type))

  fun createAnnotationDescriptor(annotationName: String, vararg values: Pair<String, Type>): AnnotationDescriptor =
      AnnotationDescriptor(getAnnotationType(annotationName), hashMapOf(*values))

  fun createAnnotationDescriptor(annotationName: String, fields: Map<String, Type>): AnnotationDescriptor =
      AnnotationDescriptor(getAnnotationType(annotationName), fields)

  fun createAnnotationData(annotationName: String): AnnotationData =
      AnnotationData(getAnnotationType(annotationName), emptyMap<String, Any>())

  fun createAnnotationData(annotationName: String, defaultValue: Any): AnnotationData =
      createAnnotationData(annotationName, "value", defaultValue)

  fun createAnnotationData(annotationName: String, methodName: String, defaultValue: Any): AnnotationData =
      createAnnotationData(annotationName, Collections.singletonMap(methodName, defaultValue))

  fun createAnnotationData(annotationName: String, vararg values: Pair<String, Any>): AnnotationData =
      AnnotationData(getAnnotationType(annotationName), hashMapOf(*values))

  fun createAnnotationData(annotationName: String, values: Map<String, Any>): AnnotationData =
      AnnotationData(getAnnotationType(annotationName), values)

  fun getAnnotationType(annotationName: String): Type =
      Type.getObjectType(annotationName)
}
