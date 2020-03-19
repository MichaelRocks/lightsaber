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
import io.michaelrocks.grip.fields
import io.michaelrocks.grip.from
import io.michaelrocks.grip.methodType
import io.michaelrocks.grip.methods
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.not
import io.michaelrocks.grip.or
import io.michaelrocks.grip.returns
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.graph.DirectedGraph
import io.michaelrocks.lightsaber.processor.graph.HashDirectedGraph
import io.michaelrocks.lightsaber.processor.graph.reversed
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.ModuleProvider
import io.michaelrocks.lightsaber.processor.model.ModuleProvisionPoint
import java.io.File

interface ComponentsAnalyzer {
  fun analyze(files: Collection<File>): Collection<Component>
}

class ComponentsAnalyzerImpl(
  private val grip: Grip,
  private val moduleRegistry: ModuleRegistry,
  private val errorReporter: ErrorReporter
) : ComponentsAnalyzer {

  private val logger = getLogger()

  override fun analyze(files: Collection<File>): Collection<Component> {
    val componentsQuery = grip select classes from files where annotatedWith(Types.COMPONENT_TYPE)
    val graph = buildComponentGraph(componentsQuery.execute().types)
    val reversedGraph = graph.reversed()
    return graph.vertices
      .filterNot { it == Types.COMPONENT_NONE_TYPE }
      .map { type ->
        val parent = reversedGraph.getAdjacentVertices(type)?.first()?.takeIf { it != Types.COMPONENT_NONE_TYPE }
        val subcomponents = graph.getAdjacentVertices(type).orEmpty()
        createComponent(type, parent, subcomponents)
      }
  }

  private fun buildComponentGraph(types: Collection<Type.Object>): DirectedGraph<Type.Object> {
    val graph = HashDirectedGraph<Type.Object>()

    for (type in types) {
      val mirror = grip.classRegistry.getClassMirror(type)
      if (mirror.signature.typeVariables.isNotEmpty()) {
        errorReporter.reportError("Component cannot have a type parameters: ${type.className}")
        continue
      }

      val annotation = mirror.annotations[Types.COMPONENT_TYPE]
      if (annotation == null) {
        errorReporter.reportError("Class ${type.className} is not a component")
        continue
      }

      val parent = annotation.values["parent"] as Type?
      if (parent != null && parent != Types.COMPONENT_NONE_TYPE) {
        if (parent is Type.Object) {
          graph.put(parent, type)
        } else {
          errorReporter.reportError("Parent component of ${type.className} is not a class")
        }
      } else {
        graph.put(Types.COMPONENT_NONE_TYPE, type)
      }
    }

    return graph
  }

  private fun createComponent(
    type: Type.Object,
    parent: Type.Object?,
    subcomponents: Iterable<Type.Object>
  ): Component {
    return createComponent(grip.classRegistry.getClassMirror(type), parent, subcomponents.toList())
  }

  private fun createComponent(mirror: ClassMirror, parent: Type.Object?, subcomponents: List<Type.Object>): Component {
    val isImportable = (annotatedWith(Types.PROVIDES_TYPE) or annotatedWith(Types.IMPORT_TYPE))
    val methodsQuery = grip select methods from mirror where (isImportable and methodType(not(returns(Type.Primitive.Void))))
    val fieldsQuery = grip select fields from mirror where isImportable

    logger.debug("Component: {}", mirror.type.className)
    val methods = methodsQuery.execute()[mirror.type].orEmpty().map { method ->
      logger.debug("  Method: {}", method)
      ModuleProvider(createModule(method), ModuleProvisionPoint.Method(method))
    }

    val fields = fieldsQuery.execute()[mirror.type].orEmpty().map { field ->
      logger.debug("  Field: {}", field)
      ModuleProvider(createModule(field), ModuleProvisionPoint.Field(field))
    }

    return Component(mirror.type, methods + fields, parent, subcomponents)
  }

  private fun createModule(method: MethodMirror): Module {
    return createModule(method.signature.returnType)
  }

  private fun createModule(field: FieldMirror): Module {
    return createModule(field.signature.type)
  }

  private fun createModule(generic: GenericType): Module {
    if (generic !is GenericType.Raw) {
      errorReporter.reportError("Module provider cannot have a generic type: $generic")
      return Module(Types.OBJECT_TYPE, emptyList(), emptyList())
    }

    val type = generic.type
    if (type !is Type.Object) {
      errorReporter.reportError("Module provider cannot have an array type: $generic")
      return Module(Types.OBJECT_TYPE, emptyList(), emptyList())
    }

    val mirror = grip.classRegistry.getClassMirror(type)
    if (Types.MODULE_TYPE !in mirror.annotations) {
      errorReporter.reportError("Module is not annotated with @Module: $generic")
      return Module(type, emptyList(), emptyList())
    }

    return moduleRegistry.getModule(type)
  }
}
