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

import io.michaelrocks.grip.*
import io.michaelrocks.grip.mirrors.*
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.cast
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.*
import org.objectweb.asm.Type
import java.io.File
import java.util.*

class Analyzer(private val processorContext: ProcessorContext) {
  private val logger = getLogger("Analyzer")

  fun analyze() {
    analyzeModules(processorContext.grip, processorContext.inputFile)
    analyzeInjectionTargets(processorContext.grip, processorContext.inputFile)
    composePackageModules(processorContext.grip)
  }

  fun analyzeModules(grip: Grip, file: File) {
    val modulesQuery = grip select classes from file where annotatedWith(Types.MODULE_TYPE)
    val methodsQuery = grip select methods from modulesQuery where
        (annotatedWith(Types.PROVIDES_TYPE) and type(not(returns(Type.VOID_TYPE))) and not(isStatic()))
    val fieldsQuery = grip select fields from modulesQuery where
        (annotatedWith(Types.PROVIDES_TYPE) and not(isStatic()))

    val modulesResult = modulesQuery.execute()
    val methodsResult = methodsQuery.execute()
    val fieldsResult = fieldsQuery.execute()

    for (moduleResult in modulesResult) {
      val moduleType = moduleResult.type
      logger.debug("Module: {}", moduleResult)
      val methods = methodsResult[moduleType].orEmpty().mapIndexed { index, method ->
        logger.debug("  Method: {}", method)
        method.toProvider(moduleResult.type, index)
      }

      val fields = fieldsResult[moduleType].orEmpty().mapIndexed { index, field ->
        logger.debug("  Field: {}", field)
        field.toProvider(moduleResult.type, index)
      }

      val configuratorType = composeConfiguratorType(moduleType)
      val module = Module(moduleType, configuratorType, methods + fields)
      processorContext.addModule(module)
    }
  }

  private fun analyzeInjectionTargets(grip: Grip, file: File) {
    val methodsQuery = grip select methods from file where annotatedWith(Types.INJECT_TYPE)
    val fieldsQuery = grip select fields from file where annotatedWith(Types.INJECT_TYPE)

    val methodsResult = methodsQuery.execute()
    val fieldsResult = fieldsQuery.execute()

    val types = HashSet<Type>(methodsResult.size + fieldsResult.size).apply {
      addAll(methodsResult.types)
      addAll(fieldsResult.types)
    }

    addInjectableTargets(types, methodsResult, fieldsResult)
    addProvidableTargets(types, methodsResult)
  }

  private fun addInjectableTargets(types: Collection<Type>, methodsResult: MethodsResult, fieldsResult: FieldsResult) {
    for (type in types) {
      logger.debug("Target: {}", type)
      val injectionPoints = ArrayList<InjectionPoint>()

      methodsResult[type]?.mapNotNullTo(injectionPoints) { method ->
        logger.debug("  Method: {}", method)
        if (method.isConstructor()) null else method.toInjectionPoint(type)
      }

      fieldsResult[type]?.mapTo(injectionPoints) { field ->
        logger.debug("  Field: {}", field)
        field.toInjectionPoint(type)
      }

      if (injectionPoints.isNotEmpty()) {
        val injectionTarget = InjectionTarget(type, injectionPoints)
        processorContext.addInjectableTarget(injectionTarget)
      }
    }
  }

  private fun addProvidableTargets(types: Collection<Type>, methodsResult: MethodsResult) {
    for (type in types) {
      logger.debug("Target: {}", type)
      val constructors = methodsResult[type].orEmpty().mapNotNull { method ->
        logger.debug("  Method: {}", method)
        if (method.isConstructor()) method.toInjectionPoint(type) else null
      }

      if (constructors.isNotEmpty()) {
        if (constructors.size > 1) {
          val separator = "\n  "
          val constructorsString = constructors.map { it.cast<InjectionPoint.Method>().method }.joinToString(separator)
          processorContext.reportError("Class has multiple injectable constructors:$separator$constructorsString")
        }

        val injectionTarget = InjectionTarget(type, constructors)
        processorContext.addProvidableTarget(injectionTarget)
      }
    }
  }

  private fun composePackageModules(grip: Grip) {
    val injectionTargetsByPackageName = processorContext.getProvidableTargets().associateManyBy {
      it.type.internalName.substringBeforeLast('/', "")
    }
    injectionTargetsByPackageName.entries.forEach {
      val (packageName, injectionTargets) = it
      val providers = injectionTargets.map {
        it.injectionPoints.first().toProvider(grip.classRegistry.getClassMirror(it.type))
      }
      val moduleType = processorContext.getPackageModuleType(packageName)
      val configuratorType = composeConfiguratorType(moduleType)
      val module = Module(moduleType, configuratorType, providers)
      processorContext.addPackageModule(module)
    }
  }

  private inline fun <T, K> Iterable<T>.associateManyBy(keySelector: (T) -> K): Map<K, Collection<T>> {
    val destination = HashMap<K, MutableCollection<T>>()
    for (element in this) {
      val items = destination.getOrPut(keySelector(element)) { ArrayList() }
      items += element
    }
    return destination
  }

  private fun MethodMirror.toProvider(container: Type, index: Int): Provider {
    val providerType = Type.getObjectType("${container.internalName}\$MethodProvider\$$index")
    val injectionPoint = toInjectionPoint(container)
    val dependency = Dependency(signature.returnType, findQualifier())
    val provisionPoint = ProvisionPoint.Method(dependency, injectionPoint)
    val scope = findScope()
    return Provider(providerType, provisionPoint, container, scope)
  }

  private fun FieldMirror.toProvider(container: Type, index: Int): Provider {
    val providerType = Type.getObjectType("${container.internalName}\$FieldProvider\$$index")
    val dependency = Dependency(signature.type, findQualifier())
    val provisionPoint = ProvisionPoint.Field(container, dependency, this)
    val scope = findScope()
    return Provider(providerType, provisionPoint, container, scope)
  }

  private fun InjectionPoint.toProvider(container: ClassMirror): Provider {
    val providerType = Type.getObjectType("${containerType.internalName}\$ConstructorProvider")
    val dependency = Dependency(GenericType.RawType(containerType), null)
    val provisionPoint = ProvisionPoint.Constructor(dependency, cast<InjectionPoint.Method>())
    return Provider(providerType, provisionPoint, container.type, container.findScope())
  }

  private fun MethodMirror.toInjectionPoint(container: Type): InjectionPoint.Method {
    return InjectionPoint.Method(container, this, getInjectees())
  }

  private fun FieldMirror.toInjectionPoint(container: Type): InjectionPoint.Field {
    return InjectionPoint.Field(container, this, getInjectee())
  }

  private fun MethodMirror.getInjectees(): List<Injectee> {
    return ArrayList<Injectee>(parameters.size).apply {
      parameters.forEachIndexed { index, parameter ->
        val type = signature.parameterTypes[index]
        val qualifier = parameter.findQualifier()
        add(type.toInjectee(qualifier))
      }
    }
  }

  private fun FieldMirror.getInjectee(): Injectee {
    return signature.type.toInjectee(findQualifier())
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
          processorContext.reportError("Type $this must be parameterized")
          return Dependency(this, qualifier)
        }
    }

    return Dependency(this, qualifier)
  }

  private fun Annotated.findQualifier(): AnnotationMirror? {
    val qualifierCount = annotations.count { processorContext.isQualifier(it.type) }
    if (qualifierCount > 0) {
      if (qualifierCount > 1) {
        processorContext.reportError("Element $this has multiple qualifiers")
      }
      return annotations.first { processorContext.isQualifier(it.type) }
    } else {
      return null
    }
  }

  private fun Annotated.findScope(): Scope {
    val scopes = annotations.mapNotNull {
      processorContext.findScopeByAnnotationType(it.type)
    }

    return when (scopes.size) {
      0 -> Scope.None
      1 -> scopes[0]
      else -> {
        processorContext.reportError("Element $this has multiple scopes")
        Scope.None
      }
    }
  }

  private fun composeConfiguratorType(moduleType: Type): Type {
    val moduleNameWithDollars = moduleType.internalName.replace('/', '$')
    return Type.getObjectType("io/michaelrocks/lightsaber/InjectorConfigurator\$$moduleNameWithDollars")
  }
}
