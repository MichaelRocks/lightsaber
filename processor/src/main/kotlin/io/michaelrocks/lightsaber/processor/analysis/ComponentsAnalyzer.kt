/*
 * Copyright 2017 Michael Rozumyanskiy
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
import io.michaelrocks.lightsaber.processor.analysis.ComponentsAnalyzer.Result
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.cast
import io.michaelrocks.lightsaber.processor.graph.DirectedGraph
import io.michaelrocks.lightsaber.processor.graph.HashDirectedGraph
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.InjectionPoint
import io.michaelrocks.lightsaber.processor.model.InjectionTarget
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.ModuleProvider
import io.michaelrocks.lightsaber.processor.model.ModuleProvisionPoint
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint
import java.io.File
import java.util.HashMap

private val PACKAGE_COMPONENT_TYPE = Types.BOXED_VOID_TYPE
private val PACKAGE_MODULE_CLASS_NAME = "Lightsaber\$PackageModule"

interface ComponentsAnalyzer {
  fun analyze(files: Collection<File>, providableTargets: Collection<InjectionTarget>): Result

  data class Result(
      val packageComponent: Component,
      val components: Collection<Component>
  )
}

class ComponentsAnalyzerImpl(
    private val grip: Grip,
    private val analyzerHelper: AnalyzerHelper,
    private val errorReporter: ErrorReporter
) : ComponentsAnalyzer {
  private val logger = getLogger()

  private val moduleRegistry: ModuleRegistry = ModuleRegistryImpl(grip, analyzerHelper, errorReporter)

  override fun analyze(files: Collection<File>, providableTargets: Collection<InjectionTarget>): Result {
    val componentsQuery = grip select classes from files where annotatedWith(Types.COMPONENT_TYPE)
    val graph = buildComponentGraph(componentsQuery.execute().types)
    val components = graph.vertices
        .filterNot { it == PACKAGE_COMPONENT_TYPE }
        .map { type -> type.toComponent(graph.getAdjacentVertices(type).orEmpty()) }
    val packageComponent = composePackageComponent(
        providableTargets,
        graph.getAdjacentVertices(PACKAGE_COMPONENT_TYPE).orEmpty().toList()
    )
    return Result(packageComponent, components)
  }

  private fun buildComponentGraph(types: Collection<Type.Object>): DirectedGraph<Type.Object> {
    val graph = HashDirectedGraph<Type.Object>()

    for (type in types) {
      val mirror = grip.classRegistry.getClassMirror(type)
      if (mirror.signature.typeParameters.isNotEmpty()) {
        errorReporter.reportError("Component cannot have a type parameters: $mirror")
        continue
      }

      val annotation = mirror.annotations[Types.COMPONENT_TYPE]
      if (annotation == null) {
        errorReporter.reportError("Class $mirror is not a component")
        continue
      }

      val parents = annotation
          .values["parents"]!!
          .cast<List<Type>>()

      if (parents.isNotEmpty()) {
        for (parent in parents) {
          if (parent is Type.Object) {
            graph.put(parent, type)
          } else {
            errorReporter.reportError("Parent component of ${type.className} is not a class")
          }
        }
      } else {
        graph.put(PACKAGE_COMPONENT_TYPE, type)
      }
    }

    return graph
  }

  private fun Type.Object.toComponent(subcomponents: Iterable<Type.Object>): Component =
      convertToComponent(grip.classRegistry.getClassMirror(this), subcomponents.toList())

  private fun convertToComponent(mirror: ClassMirror, subcomponents: List<Type.Object>): Component {
    val methodsQuery = grip select methods from mirror where
        (annotatedWith(Types.PROVIDES_TYPE) and methodType(not(returns(Type.Primitive.Void))) and not(isStatic()))
    val fieldsQuery = grip select fields from mirror where
        (annotatedWith(Types.PROVIDES_TYPE) and not(isStatic()))

    logger.debug("Component: {}", mirror)
    val methods = methodsQuery.execute()[mirror.type].orEmpty().map { method ->
      logger.debug("  Method: {}", method)
      ModuleProvider(method.toModule(), ModuleProvisionPoint.Method(method))
    }

    val fields = fieldsQuery.execute()[mirror.type].orEmpty().map { field ->
      logger.debug("  Field: {}", field)
      ModuleProvider(field.toModule(), ModuleProvisionPoint.Field(field))
    }

    return Component(mirror.type, methods + fields, subcomponents)
  }

  private fun MethodMirror.toModule(): Module =
      signature.returnType.toModule()

  private fun FieldMirror.toModule(): Module =
      signature.type.toModule()

  private fun GenericType.toModule(): Module {
    if (this !is GenericType.Raw) {
      errorReporter.reportError("Module provider cannot have a generic type: $this")
      return Module(Types.OBJECT_TYPE, emptyList())
    }

    val type = type as Type.Object
    val mirror = grip.classRegistry.getClassMirror(type)
    if (Types.MODULE_TYPE !in mirror.annotations) {
      errorReporter.reportError("Module is not annotated with @Module: $this")
      return Module(type, emptyList())
    }

    return moduleRegistry.getOrCreateModule(type)
  }

  private fun composePackageComponent(
      providableTargets: Iterable<InjectionTarget>,
      subcomponents: List<Type.Object>
  ): Component {
    val providers = composePackageModules(providableTargets)
        .map { ModuleProvider(it, ModuleProvisionPoint.Null) }
    return Component(PACKAGE_COMPONENT_TYPE, providers, subcomponents)
  }

  private fun composePackageModules(providableTargets: Iterable<InjectionTarget>): List<Module> {
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

  private fun InjectionPoint.toProvider(container: ClassMirror): Provider {
    val providerType = getObjectTypeByInternalName("${containerType.internalName}\$ConstructorProvider")
    val dependency = Dependency(GenericType.Raw(containerType), null)
    val provisionPoint = ProvisionPoint.Constructor(dependency, this as InjectionPoint.Method)
    return Provider(providerType, provisionPoint, container.type, analyzerHelper.findScope(container))
  }

  private fun composePackageModuleType(packageName: String): Type.Object {
    val name = if (packageName.isEmpty()) PACKAGE_MODULE_CLASS_NAME else "$packageName/$PACKAGE_MODULE_CLASS_NAME"
    return getObjectTypeByInternalName(name)
  }
}
