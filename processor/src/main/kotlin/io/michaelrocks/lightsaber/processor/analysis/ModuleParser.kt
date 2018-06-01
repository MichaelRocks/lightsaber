/*
 * Copyright 2018 Michael Rozumyanskiy
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
import io.michaelrocks.grip.fields
import io.michaelrocks.grip.from
import io.michaelrocks.grip.isStatic
import io.michaelrocks.grip.methodType
import io.michaelrocks.grip.methods
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.not
import io.michaelrocks.grip.returns
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.InjectionPoint
import io.michaelrocks.lightsaber.processor.model.InjectionTarget
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint

interface ModuleParser {
  fun parseModule(type: Type.Object, providableTargets: Collection<InjectionTarget>): Module
}

class ModuleParserImpl(
    private val grip: Grip,
    private val analyzerHelper: AnalyzerHelper,
    private val errorReporter: ErrorReporter
) : ModuleParser {
  private val logger = getLogger()

  override fun parseModule(type: Type.Object, providableTargets: Collection<InjectionTarget>): Module {
    return convertToModule(grip.classRegistry.getClassMirror(type), providableTargets)
  }

  private fun convertToModule(mirror: ClassMirror, providableTargets: Collection<InjectionTarget>): Module {
    if (mirror.signature.typeParameters.isNotEmpty()) {
      errorReporter.reportError("Module cannot have a type parameters: ${mirror.type.className}")
      return Module(mirror.type, emptyList())
    }

    if (Types.MODULE_TYPE !in mirror.annotations) {
      errorReporter.reportError("Class ${mirror.type.className} is not a module")
      return Module(mirror.type, emptyList())
    }

    val methodsQuery = grip select methods from mirror where
        (annotatedWith(Types.PROVIDES_TYPE) and methodType(not(returns(Type.Primitive.Void))) and not(isStatic()))
    val fieldsQuery = grip select fields from mirror where
        (annotatedWith(Types.PROVIDES_TYPE) and not(isStatic()))

    logger.debug("Module: {}", mirror.type.className)
    val constructors = providableTargets.map { target ->
      logger.debug("  Constructor: {}", target.injectionPoints.first())
      target.toProvider(target.type)
    }

    val methods = methodsQuery.execute()[mirror.type].orEmpty().mapIndexed { index, method ->
      logger.debug("  Method: {}", method)
      method.toProvider(mirror.type, index)
    }

    val fields = fieldsQuery.execute()[mirror.type].orEmpty().mapIndexed { index, field ->
      logger.debug("  Field: {}", field)
      field.toProvider(mirror.type, index)
    }

    return Module(mirror.type, constructors + methods + fields)
  }

  private fun InjectionTarget.toProvider(container: Type.Object): Provider {
    val mirror = grip.classRegistry.getClassMirror(type)
    val providerType = getObjectTypeByInternalName("${type.internalName}\$ConstructorProvider")
    val dependency = Dependency(GenericType.Raw(type), analyzerHelper.findQualifier(mirror))
    val provisionPoint = ProvisionPoint.Constructor(dependency, injectionPoints.first() as InjectionPoint.Method)
    val scope = analyzerHelper.findScope(mirror)
    return Provider(providerType, provisionPoint, container, scope)
  }

  private fun MethodMirror.toProvider(container: Type.Object, index: Int): Provider {
    val providerType = getObjectTypeByInternalName("${container.internalName}\$MethodProvider\$$index")
    val injectionPoint = analyzerHelper.convertToInjectionPoint(this, container)
    val dependency = Dependency(signature.returnType, analyzerHelper.findQualifier(this))
    val provisionPoint = ProvisionPoint.Method(dependency, injectionPoint)
    val scope = analyzerHelper.findScope(this)
    return Provider(providerType, provisionPoint, container, scope)
  }

  private fun FieldMirror.toProvider(container: Type.Object, index: Int): Provider {
    val providerType = getObjectTypeByInternalName("${container.internalName}\$FieldProvider\$$index")
    val dependency = Dependency(signature.type, analyzerHelper.findQualifier(this))
    val provisionPoint = ProvisionPoint.Field(container, dependency, this)
    val scope = analyzerHelper.findScope(this)
    return Provider(providerType, provisionPoint, container, scope)
  }
}
