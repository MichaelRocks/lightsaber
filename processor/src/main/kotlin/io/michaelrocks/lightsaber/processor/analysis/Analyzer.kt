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
import io.michaelrocks.grip.mirrors.Annotated
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.isConstructor
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.mirrors.signature.MethodSignatureMirror
import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.descriptors.*
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.signature.MethodSignature
import io.michaelrocks.lightsaber.processor.signature.TypeSignature
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.io.File
import java.io.IOException
import java.util.*

class Analyzer(private val processorContext: ProcessorContext) {
  private val logger = getLogger("Analyzer")

  @Throws(IOException::class)
  fun analyze() {
    analyzeModules(processorContext.grip, processorContext.inputFile)
    analyzeInjectionTargets(processorContext.grip, processorContext.inputFile)
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
      val type = moduleResult.type
      val module = ModuleDescriptor.Builder(type).run {
        logger.debug("Module: {}", moduleResult)
        methodsResult[type]?.forEach { methodResult ->
          logger.debug("  Method: {}", methodResult)
          val qualifiedMethod = methodResult.toQualifiedMethodDescriptor()
          val scope = findScope(methodResult)
          addProviderMethod(qualifiedMethod, scope)
        }

        fieldsResult[type]?.forEach { fieldResult ->
          logger.debug("  Field: {}", fieldResult)
          val qualifiedField = fieldResult.toQualifiedFieldDescriptor()
          addProviderField(qualifiedField)
        }

        build()
      }
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

    for (type in types) {
      val target = InjectionTargetDescriptor.Builder(type).run {
        logger.debug("Target: {}", type)
        methodsResult[type]?.forEach { methodResult ->
          logger.debug("  Method: {}", methodResult)
          val qualifiedMethod = methodResult.toQualifiedMethodDescriptor()
          if (methodResult.isConstructor()) {
            addInjectableConstructor(qualifiedMethod)
          } else if (methodResult.access and Opcodes.ACC_STATIC == 0) {
            addInjectableMethod(qualifiedMethod)
          } else {
            addInjectableStaticMethod(qualifiedMethod)
          }
        }

        fieldsResult[type]?.forEach { fieldResult ->
          logger.debug("  Field: {}", fieldResult)
          val qualifiedField = fieldResult.toQualifiedFieldDescriptor()
          if (fieldResult.access and Opcodes.ACC_STATIC == 0) {
            addInjectableField(qualifiedField)
          } else {
            addInjectableStaticField(qualifiedField)
          }
        }

        build()
      }

      if (!target.injectableFields.isEmpty() || !target.injectableMethods.isEmpty()) {
        processorContext.addInjectableTarget(target)
      }
      if (target.injectableConstructors.size > 1) {
        val separator = "\n  "
        val constructors = target.injectableConstructors.keys.joinToString(separator)
        processorContext.reportError("Class has multiple injectable constructors:$separator$constructors")
      } else if (!target.injectableConstructors.isEmpty()) {
        processorContext.addProvidableTarget(target)
      }
    }
  }

  private fun MethodMirror.toQualifiedMethodDescriptor(): QualifiedMethodDescriptor {
    val method = toMethodDescriptor()
    val resultQualifier = findQualifier(this)
    val parametersQualifiers = parameters.map { findQualifier(it) }
    return QualifiedMethodDescriptor(method, parametersQualifiers, resultQualifier)
  }

  private fun FieldMirror.toQualifiedFieldDescriptor(): QualifiedFieldDescriptor {
    val field = FieldDescriptor(name, genericType.toTypeSignature())
    val qualifier = findQualifier(this)
    return QualifiedFieldDescriptor(field, qualifier)
  }

  private fun MethodMirror.toMethodDescriptor(): MethodDescriptor {
    return MethodDescriptor(name, signature.toMethodSignature())
  }

  private fun MethodSignatureMirror.toMethodSignature(): MethodSignature {
    val returnType = returnType.toTypeSignature()
    val argumentTypes = parameterTypes.map { it.toTypeSignature() }
    return MethodSignature(returnType, argumentTypes)
  }

  private fun GenericType.toTypeSignature(): TypeSignature =
      when (this) {
        is GenericType.ParameterizedType -> TypeSignature(type, (typeArguments[0] as GenericType.RawType).type)
        is GenericType.RawType -> TypeSignature(type)
        else -> error("Unsupported generic type: $this")
      }

  private fun findQualifier(annotated: Annotated): AnnotationData? {
    val qualifierCount = annotated.annotations.count { processorContext.isQualifier(it.type) }
    if (qualifierCount > 0) {
      if (qualifierCount > 1) {
        processorContext.reportError("Element $annotated has multiple qualifiers")
      }
      val annotation = annotated.annotations.first { processorContext.isQualifier(it.type) }
      return AnnotationData(annotation.type, annotation.values)
    } else {
      return null
    }
  }

  private fun findScope(annotated: Annotated): ScopeDescriptor? {
    annotated.annotations.forEach {
      val scope = processorContext.findScopeByAnnotationType(it.type)
      if (scope != null) {
        return scope
      }
    }
    return null
  }
}
