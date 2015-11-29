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

import io.michaelrocks.lightsaber.processor.logging.getLogger
import org.objectweb.asm.Type
import java.util.*

class AnnotationRegistry {
  private val logger = getLogger()

  private val annotationsByType = HashMap<Type, AnnotationDescriptor>()
  private val unresolvedDefaultsByType = HashMap<Type, AnnotationData>()
  private val resolvedDefaultsByType = HashMap<Type, AnnotationData>()

  fun addAnnotationDefaults(annotation: AnnotationDescriptor, defaults: AnnotationData) {
    check(annotation.type == defaults.type)

    if (annotationsByType.containsKey(annotation.type)) {
      logger.warn("Annotation already registered: {}", annotation.type)
      return
    }

    annotationsByType.put(annotation.type, annotation)

    if (defaults.resolved) {
      check(!unresolvedDefaultsByType.containsKey(annotation.type))
      resolvedDefaultsByType.put(annotation.type, defaults)
    } else {
      check(!resolvedDefaultsByType.containsKey(annotation.type))
      unresolvedDefaultsByType.put(annotation.type, defaults)
    }
  }

  fun findAnnotationByType(annotationType: Type): AnnotationDescriptor? {
    return annotationsByType[annotationType]
  }

  fun resolveAnnotation(data: AnnotationData): AnnotationData {
    if (data.resolved) {
      return data
    }

    return AnnotationResolver().resolve(data)
  }

  internal fun hasUnresolvedDefaults(annotationType: Type): Boolean {
    return unresolvedDefaultsByType.containsKey(annotationType)
  }

  internal fun hasResolvedDefaults(annotationType: Type): Boolean {
    return resolvedDefaultsByType.containsKey(annotationType)
  }

  private inner class AnnotationResolver {
    private val values = HashMap<String, Any>()
    private var resolved = true

    internal fun resolve(data: AnnotationData): AnnotationData {
      return resolve(data, true)
    }

    private fun resolve(data: AnnotationData, applyDefaults: Boolean): AnnotationData {
      if (applyDefaults) {
        applyDefaults(data)
      }

      data.values.mapValuesTo(values) { entry -> resolveObject(entry.value) }
      return AnnotationData(data.type, Collections.unmodifiableMap(values), resolved)
    }

    private fun applyDefaults(data: AnnotationData) {
      val defaults = resolveDefaults(data.type)
      if (defaults == null) {
        resolved = false
        return
      }

      resolved = resolved and defaults.resolved
      values.putAll(defaults.values)
    }

    private fun resolveDefaults(annotationType: Type): AnnotationData? {
      return resolvedDefaultsByType[annotationType] ?:
          unresolvedDefaultsByType.remove(annotationType)?.let { unresolvedDefaults ->
            val resolvedDefaults =
                if (unresolvedDefaults.resolved) unresolvedDefaults
                else AnnotationResolver().resolve(unresolvedDefaults, false)
            resolvedDefaultsByType.put(annotationType, resolvedDefaults)
            resolvedDefaults
          }
    }

    @Suppress("UNCHECKED_CAST")
    private fun resolveObject(value: Any): Any =
        when (value) {
          is AnnotationData -> resolveAnnotation(value).let { data ->
            resolved = resolved and data.resolved
            data
          }
          is List<*> -> resolveArray(value as List<Any>)
          else -> value
        }

    private fun resolveArray(array: List<Any>): List<Any> = array.map { resolveObject(it) }
  }
}
