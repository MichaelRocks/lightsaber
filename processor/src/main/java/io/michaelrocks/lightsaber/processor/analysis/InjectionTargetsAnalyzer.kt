/*
 * Copyright 2019 Michael Rozumyanskiy
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

import io.michaelrocks.grip.FieldsResult
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.MethodsResult
import io.michaelrocks.grip.annotatedWith
import io.michaelrocks.grip.fields
import io.michaelrocks.grip.methods
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.isConstructor
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.cast
import io.michaelrocks.lightsaber.processor.commons.given
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.InjectionPoint
import io.michaelrocks.lightsaber.processor.model.InjectionTarget
import java.io.File
import java.util.ArrayList
import java.util.HashSet

interface InjectionTargetsAnalyzer {
  fun analyze(files: Collection<File>): Result

  data class Result(
    val injectableTargets: Collection<InjectionTarget>,
    val providableTargets: Collection<InjectionTarget>
  )
}

class InjectionTargetsAnalyzerImpl(
  private val grip: Grip,
  private val analyzerHelper: AnalyzerHelper,
  private val errorReporter: ErrorReporter
) : InjectionTargetsAnalyzer {

  private val logger = getLogger()

  override fun analyze(files: Collection<File>): InjectionTargetsAnalyzer.Result {
    val context = createInjectionTargetsContext(files)
    val injectableTargets = analyzeInjectableTargets(context)
    val providableTargets = analyzeProvidableTargets(context)
    return InjectionTargetsAnalyzer.Result(injectableTargets, providableTargets)
  }

  private fun createInjectionTargetsContext(files: Collection<File>): InjectionTargetsContext {
    val methodsQuery = grip select methods from files where annotatedWith(Types.INJECT_TYPE)
    val fieldsQuery = grip select fields from files where annotatedWith(Types.INJECT_TYPE)

    val methodsResult = methodsQuery.execute()
    val fieldsResult = fieldsQuery.execute()

    val types = HashSet<Type.Object>(methodsResult.size + fieldsResult.size).apply {
      addAll(methodsResult.types)
      addAll(fieldsResult.types)
    }

    return InjectionTargetsContext(types, methodsResult, fieldsResult)
  }

  private fun analyzeInjectableTargets(context: InjectionTargetsContext): Collection<InjectionTarget> {
    return context.types.mapNotNull { type ->
      logger.debug("Target: {}", type)
      val injectionPoints = ArrayList<InjectionPoint>()

      context.methods[type]?.mapNotNullTo(injectionPoints) { method ->
        logger.debug("  Method: {}", method)
        given(!method.isConstructor) { analyzerHelper.convertToInjectionPoint(method, type) }
      }

      context.fields[type]?.mapTo(injectionPoints) { field ->
        logger.debug("  Field: {}", field)
        analyzerHelper.convertToInjectionPoint(field, type)
      }

      given(injectionPoints.isNotEmpty()) { InjectionTarget(type, injectionPoints) }
    }
  }

  private fun analyzeProvidableTargets(context: InjectionTargetsContext): Collection<InjectionTarget> {
    return context.types.mapNotNull { type ->
      logger.debug("Target: {}", type)
      val constructors = context.methods[type].orEmpty().mapNotNull { method ->
        logger.debug("  Method: {}", method)
        given(method.isConstructor) { analyzerHelper.convertToInjectionPoint(method, type) }
      }

      given(constructors.isNotEmpty()) {
        if (constructors.size > 1) {
          val separator = "\n  "
          val constructorsString = constructors.map { it.cast<InjectionPoint.Method>().method }.joinToString(separator)
          errorReporter.reportError("Class has multiple injectable constructors:$separator$constructorsString")
        }

        InjectionTarget(type, constructors)
      }
    }
  }

  private class InjectionTargetsContext(
    val types: Collection<Type.Object>,
    val methods: MethodsResult,
    val fields: FieldsResult
  )
}
