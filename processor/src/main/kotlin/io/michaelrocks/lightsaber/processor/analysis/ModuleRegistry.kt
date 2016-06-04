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
import io.michaelrocks.grip.not
import io.michaelrocks.grip.returns
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint
import java.util.HashMap

interface ModuleRegistry {
  fun getOrCreateModule(type: Type.Object): Module
}

class ModuleRegistryImpl(
    private val grip: Grip,
    private val analyzerHelper: AnalyzerHelper,
    private val errorReporter: ErrorReporter
) : ModuleRegistry {
  private val logger = getLogger()

  private val modulesByType = HashMap<Type.Object, Module>()

  override fun getOrCreateModule(type: Type.Object): Module {
    return modulesByType.getOrPut(type) { convertToModule(grip.classRegistry.getClassMirror(type)) }
  }

  private fun convertToModule(mirror: ClassMirror): Module {
    if (mirror.signature.typeParameters.isNotEmpty()) {
      errorReporter.reportError("Module cannot have a type parameters: $mirror")
      return Module(mirror.type, emptyList())
    }

    if (Types.MODULE_TYPE !in mirror.annotations) {
      errorReporter.reportError("Class $mirror is not a module")
      return Module(mirror.type, emptyList())
    }

    val methodsQuery = grip select methods from mirror where
        (annotatedWith(Types.PROVIDES_TYPE) and methodType(not(returns(Type.Primitive.Void))) and not(isStatic()))
    val fieldsQuery = grip select fields from mirror where
        (annotatedWith(Types.PROVIDES_TYPE) and not(isStatic()))

    logger.debug("Module: {}", mirror)
    val methods = methodsQuery.execute()[mirror.type].orEmpty().mapIndexed { index, method ->
      logger.debug("  Method: {}", method)
      method.toProvider(mirror.type, index)
    }

    val fields = fieldsQuery.execute()[mirror.type].orEmpty().mapIndexed { index, field ->
      logger.debug("  Field: {}", field)
      field.toProvider(mirror.type, index)
    }

    return Module(mirror.type, methods + fields)
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
