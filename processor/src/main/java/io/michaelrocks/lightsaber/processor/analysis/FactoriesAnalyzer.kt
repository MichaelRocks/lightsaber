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
import io.michaelrocks.grip.mirrors.isInterface
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
    if (!mirror.isInterface) {
      error("Factory ${mirror.type.className} must be an interface")
      return null
    }

    if (mirror.signature.typeVariables.isNotEmpty()) {
      error("Factory ${mirror.type.className} mustn't contain generic parameters")
      return null
    }

    if (mirror.interfaces.isNotEmpty()) {
      error("Factory ${mirror.type.className} mustn't extend any interfaces")
      return null
    }

    if (mirror.methods.isEmpty()) {
      error("Factory ${mirror.type.className} must contain at least one method")
      return null
    }

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
    val returnType = method.type.returnType
    if (returnType !is Type.Object) {
      error("Method ${mirror.type.className}.${method.name} returns ${returnType.className}, but must return a class")
      return null
    }

    if (mirror.signature.typeVariables.isNotEmpty()) {
      error("Method ${mirror.type.className}.${method.name} mustn't contain generic parameters")
      return null
    }

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

    val factoryInjectees = constructorInjectionPoint.injectees.map { injectee ->
      val argumentIndex = argumentIndexToInjecteeMap[injectee]
      if (argumentIndex == null) {
        FactoryInjectee.FromInjector(injectee)
      } else {
        FactoryInjectee.FromMethod(injectee, argumentIndex)
      }
    }

    val factoryInjectionPoint = FactoryInjectionPoint(returnType, constructor, factoryInjectees)
    return FactoryProvisionPoint(mirror.type, method, method.signature.returnType, factoryInjectionPoint)
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
