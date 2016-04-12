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
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.cast
import io.michaelrocks.lightsaber.processor.commons.given
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.*
import org.objectweb.asm.Type
import java.io.File
import java.util.*

private val PACKAGE_MODULE_CLASS_NAME = "Lightsaber\$PackageModule"

class Analyzer(
    private val grip: Grip,
    private val errorReporter: ErrorReporter
) {
  private val logger = getLogger("Analyzer")
  private val scopeRegistry = ScopeRegistry()

  fun analyze(files: Collection<File>): InjectionContext {
    val modules = analyzeModules(files)
    val context = createInjectionTargetsContext(files)
    val injectableTargets = analyzeInjectableTargets(context)
    val providableTargets = analyzeProvidableTargets(context)
    val packageModules = composePackageModules(providableTargets)
    return InjectionContext(modules, packageModules, injectableTargets, providableTargets)
  }

  fun analyzeModules(files: Collection<File>): Collection<Module> {
    val modulesQuery = grip select classes from files where annotatedWith(Types.MODULE_TYPE)
    val methodsQuery = grip select methods from modulesQuery where
        (annotatedWith(Types.PROVIDES_TYPE) and type(not(returns(Type.VOID_TYPE))) and not(isStatic()))
    val fieldsQuery = grip select fields from modulesQuery where
        (annotatedWith(Types.PROVIDES_TYPE) and not(isStatic()))

    val modulesResult = modulesQuery.execute()
    val methodsResult = methodsQuery.execute()
    val fieldsResult = fieldsQuery.execute()

    return modulesResult.map { moduleResult ->
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

      Module(moduleType, methods + fields)
    }
  }

  private fun createInjectionTargetsContext(files: Collection<File>): InjectionTargetsContext {
    val methodsQuery = grip select methods from files where annotatedWith(Types.INJECT_TYPE)
    val fieldsQuery = grip select fields from files where annotatedWith(Types.INJECT_TYPE)

    val methodsResult = methodsQuery.execute()
    val fieldsResult = fieldsQuery.execute()

    val types = HashSet<Type>(methodsResult.size + fieldsResult.size).apply {
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
        if (method.isConstructor()) null else method.toInjectionPoint(type)
      }

      context.fields[type]?.mapTo(injectionPoints) { field ->
        logger.debug("  Field: {}", field)
        field.toInjectionPoint(type)
      }

      given(injectionPoints.isNotEmpty()) { InjectionTarget(type, injectionPoints) }
    }
  }

  private fun analyzeProvidableTargets(context: InjectionTargetsContext): Collection<InjectionTarget> {
    return context.types.mapNotNull { type ->
      logger.debug("Target: {}", type)
      val constructors = context.methods[type].orEmpty().mapNotNull { method ->
        logger.debug("  Method: {}", method)
        given(method.isConstructor()) { method.toInjectionPoint(type) }
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

  private fun composePackageModules(
      providableTargets: Iterable<InjectionTarget>): Collection<Module> {
    val injectionTargetsByPackageName = providableTargets.groupByTo(HashMap()) {
      it.type.internalName.substringBeforeLast('/', "")
    }
    return injectionTargetsByPackageName.entries.map {
      val (packageName, injectionTargets) = it
      val providers = injectionTargets.map {
        it.injectionPoints.first().toProvider(grip.classRegistry.getClassMirror(it.type))
      }
      val moduleType = composePackageModuleType(packageName)
      Module(moduleType, providers)
    }
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
          errorReporter.reportError("Type $this must be parameterized")
          return Dependency(this, qualifier)
        }
    }

    return Dependency(this, qualifier)
  }

  private fun Annotated.findQualifier(): AnnotationMirror? {
    fun isQualifier(annotationType: Type): Boolean {
      return grip.classRegistry.getClassMirror(annotationType).annotations.contains(Types.QUALIFIER_TYPE)
    }

    val qualifierCount = annotations.count { isQualifier(it.type) }
    if (qualifierCount > 0) {
      if (qualifierCount > 1) {
        errorReporter.reportError("Element $this has multiple qualifiers")
      }
      return annotations.first { isQualifier(it.type) }
    } else {
      return null
    }
  }

  private fun Annotated.findScope(): Scope {
    val scopeProviders = annotations.mapNotNull {
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

  private fun composePackageModuleType(packageName: String): Type {
    val name = if (packageName.isEmpty()) PACKAGE_MODULE_CLASS_NAME else "$packageName/$PACKAGE_MODULE_CLASS_NAME"
    return Type.getObjectType(name)
  }

  private class InjectionTargetsContext(
      val types: Collection<Type>,
      val methods: MethodsResult,
      val fields: FieldsResult
  )
}
