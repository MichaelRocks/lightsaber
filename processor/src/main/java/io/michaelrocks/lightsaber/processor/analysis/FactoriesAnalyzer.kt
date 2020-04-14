/*
 * Copyright 2020 Michael Rozumyanskiy
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

import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.and
import io.michaelrocks.grip.annotatedWith
import io.michaelrocks.grip.classes
import io.michaelrocks.grip.from
import io.michaelrocks.grip.isConstructor
import io.michaelrocks.grip.methods
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.associateByIndexedTo
import io.michaelrocks.lightsaber.processor.commons.boxed
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.Factory
import io.michaelrocks.lightsaber.processor.model.FactoryInjectee
import io.michaelrocks.lightsaber.processor.model.FactoryInjectionPoint
import io.michaelrocks.lightsaber.processor.model.FactoryProvisionPoint
import io.michaelrocks.lightsaber.processor.model.Injectee
import io.michaelrocks.lightsaber.processor.model.InjectionPoint
import java.io.File

interface FactoriesAnalyzer {
  fun analyze(files: Collection<File>): Collection<Factory>
}

class FactoriesAnalyzerImpl(
  private val grip: Grip,
  private val analyzerHelper: AnalyzerHelper,
  private val errorReporter: ErrorReporter,
  private val projectName: String
) : FactoriesAnalyzer {

  override fun analyze(files: Collection<File>): Collection<Factory> {
    val factoriesQuery = grip select classes from files where annotatedWith(Types.FACTORY_TYPE)
    return factoriesQuery.execute().classes.mapNotNull {
      maybeCreateFactory(it)
    }
  }

  private fun maybeCreateFactory(mirror: ClassMirror): Factory? {
    val provisionPoints = mirror.methods.mapNotNull { maybeCreateFactoryProvisionPoint(mirror, it) }
    if (provisionPoints.isEmpty()) {
      return null
    }

    val implementationType =
      getObjectTypeByInternalName(mirror.type.internalName + "\$Lightsaber\$Factory\$$projectName")
    val qualifier = analyzerHelper.findQualifier(mirror)
    val dependency = Dependency(GenericType.Raw(mirror.type), qualifier)
    return Factory(mirror.type, implementationType, dependency, provisionPoints)
  }

  private fun maybeCreateFactoryProvisionPoint(mirror: ClassMirror, method: MethodMirror): FactoryProvisionPoint? {
    val returnType = tryExtractReturnTypeFromFactoryMethod(mirror, method) ?: return null

    val dependencyMirror = grip.classRegistry.getClassMirror(returnType)
    val dependencyConstructorsQuery =
      grip select methods from dependencyMirror where (isConstructor() and annotatedWith(Types.FACTORY_INJECT_TYPE))
    val dependencyConstructors = dependencyConstructorsQuery.execute().values.singleOrNull().orEmpty()
    if (dependencyConstructors.isEmpty()) {
      error("Class ${dependencyMirror.type.className} must have a constructor annotated with @Factory.Inject")
      return null
    }

    if (dependencyConstructors.size != 1) {
      error("Class ${dependencyMirror.type.className} must have a single constructor annotated with @Factory.Inject")
      return null
    }

    val methodInjectionPoint = analyzerHelper.convertToInjectionPoint(method, mirror.type)
    validateNoDuplicateInjectees(methodInjectionPoint)
    val argumentIndexToInjecteeMap = methodInjectionPoint.injectees.associateByIndexedTo(
      HashMap(),
      keySelector = { _, injectee -> injectee },
      valueSelector = { index, _ -> index }
    )

    val constructor = dependencyConstructors.single()
    val constructorInjectionPoint = analyzerHelper.convertToInjectionPoint(constructor, mirror.type)

    val factoryInjectees = constructorInjectionPoint.injectees.mapNotNull { injectee ->
      if (Types.FACTORY_PARAMETER_TYPE in injectee.annotations) {
        val argumentIndex = argumentIndexToInjecteeMap[injectee]
        if (argumentIndex == null) {
          val dependencyClassName = dependencyMirror.type.className
          val factoryClassName = mirror.type.className
          error("Class $dependencyClassName contains a @Factory.Parameter not provided by factory $factoryClassName: ${injectee.dependency}")
          null
        } else {
          FactoryInjectee.FromMethod(injectee, argumentIndex)
        }
      } else {
        FactoryInjectee.FromInjector(injectee)
      }
    }

    val factoryInjectionPoint = FactoryInjectionPoint(returnType, constructor, factoryInjectees)
    return FactoryProvisionPoint(mirror.type, method, factoryInjectionPoint)
  }

  private fun tryExtractReturnTypeFromFactoryMethod(mirror: ClassMirror, method: MethodMirror): Type.Object? {
    val returnAnnotation = method.annotations[Types.FACTORY_RETURN_TYPE]
    if (returnAnnotation != null) {
      val returnType = returnAnnotation.values["value"]
      if (returnType !is Type) {
        error("Method ${mirror.type.className}.${method.name} is annotated with @Factory.Return that has a wrong parameter $returnType")
        return null
      }

      if (returnType !is Type.Object) {
        error("Method ${mirror.type.className}.${method.name} is annotated with @Factory.Return with ${returnType.className} value, but its value must be a class")
        return null
      }

      return returnType
    }

    val returnType = method.type.returnType
    if (returnType !is Type.Object) {
      error("Method ${mirror.type.className}.${method.name} returns ${returnType.className}, but must return a class")
      return null
    }

    return returnType
  }

  private fun validateNoDuplicateInjectees(injectionPoint: InjectionPoint.Method) {
    val visitedInjectees = hashSetOf<Injectee>()
    injectionPoint.injectees.forEach { injectee ->
      if (!visitedInjectees.add(injectee.boxed())) {
        val className = injectionPoint.containerType.className
        val methodName = injectionPoint.method.name
        error("Method $className.$methodName accepts $injectee multiple times")
      }
    }
  }

  private fun error(message: String) {
    errorReporter.reportError(message)
  }
}
