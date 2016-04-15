/*
 * Copyright 2016 Michael Rozumyanskiy
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

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.Annotated
import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.model.*
import org.objectweb.asm.Type
import java.util.*

interface AnalyzerHelper {
  fun convertToInjectionPoint(method: MethodMirror, container: Type): InjectionPoint.Method
  fun convertToInjectionPoint(mirror: FieldMirror, container: Type): InjectionPoint.Field
  fun findQualifier(annotated: Annotated): AnnotationMirror?
  fun findScope(annotated: Annotated): Scope
}

class AnalyzerHelperImpl(
    private val classRegistry: ClassRegistry,
    private val scopeRegistry: ScopeRegistry,
    private val errorReporter: ErrorReporter
) : AnalyzerHelper {

  override fun convertToInjectionPoint(method: MethodMirror,
      container: Type): InjectionPoint.Method {
    return InjectionPoint.Method(container, method, method.getInjectees())
  }

  override fun convertToInjectionPoint(mirror: FieldMirror,
      container: Type): InjectionPoint.Field {
    return InjectionPoint.Field(container, mirror, mirror.getInjectee())
  }

  private fun MethodMirror.getInjectees(): List<Injectee> {
    return ArrayList<Injectee>(parameters.size).apply {
      parameters.forEachIndexed { index, parameter ->
        val type = signature.parameterTypes[index]
        val qualifier = findQualifier(parameter)
        add(type.toInjectee(qualifier))
      }
    }
  }

  private fun FieldMirror.getInjectee(): Injectee {
    return signature.type.toInjectee(findQualifier(this))
  }

  private fun GenericType.toInjectee(qualifier: AnnotationMirror?): Injectee {
    val dependency = toDependency(qualifier)
    val converter = getConverter()
    return Injectee(dependency, converter)
  }

  private fun GenericType.getConverter(): Converter {
    return when (rawType) {
      Types.PROVIDER_TYPE -> Converter.Identity
      Types.LAZY_TYPE -> Converter.Adapter(LightsaberTypes.LAZY_ADAPTER_TYPE)
      else -> Converter.Instance
    }
  }

  private fun GenericType.toDependency(qualifier: AnnotationMirror?): Dependency {
    when (rawType) {
      Types.PROVIDER_TYPE,
      Types.LAZY_TYPE ->
        if (this is GenericType.ParameterizedType) {
          return Dependency(typeArguments[0], qualifier)
        } else {
          errorReporter.reportError("Type $this must be parameterized")
          return Dependency(this, qualifier)
        }
    }

    return Dependency(this, qualifier)
  }

  override fun findQualifier(annotated: Annotated): AnnotationMirror? {
    fun isQualifier(annotationType: Type): Boolean {
      return classRegistry.getClassMirror(annotationType).annotations.contains(Types.QUALIFIER_TYPE)
    }

    val qualifierCount = annotated.annotations.count { isQualifier(it.type) }
    if (qualifierCount > 0) {
      if (qualifierCount > 1) {
        errorReporter.reportError("Element $this has multiple qualifiers")
      }
      return annotated.annotations.first { isQualifier(it.type) }
    } else {
      return null
    }
  }

  override fun findScope(annotated: Annotated): Scope {
    val scopeProviders = annotated.annotations.mapNotNull {
      scopeRegistry.findScopeProviderByAnnotationType(it.type)
    }

    return when (scopeProviders.size) {
      0 -> Scope.None
      1 -> Scope.Class(scopeProviders[0])
      else -> {
        errorReporter.reportError("Element $this has multiple scopes: $scopeProviders")
        Scope.None
      }
    }
  }
}
