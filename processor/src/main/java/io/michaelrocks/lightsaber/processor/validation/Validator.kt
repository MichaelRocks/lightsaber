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

package io.michaelrocks.lightsaber.processor.validation

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.Element
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.boxed
import io.michaelrocks.lightsaber.processor.graph.findCycles
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.Factory
import io.michaelrocks.lightsaber.processor.model.FactoryInjectee
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import io.michaelrocks.lightsaber.processor.model.InjectionPoint
import io.michaelrocks.lightsaber.processor.model.InjectionTarget

class Validator(
  private val classRegistry: ClassRegistry,
  private val errorReporter: ErrorReporter,
  private val context: InjectionContext
) {

  fun validate() {
    performSanityChecks()
    validateComponents()
  }

  private fun performSanityChecks() {
    SanityChecker(classRegistry, errorReporter).performSanityChecks(context)
  }

  private fun validateComponents() {
    val componentGraph = buildComponentGraph(context.components)
    val cycles = componentGraph.findCycles()
    for (cycle in cycles) {
      errorReporter.reportError("Component cycle ${cycle.joinToString(" -> ")}")
    }

    context.components
      .filter { it.parent == null }
      .forEach { component ->
        validateNoModuleDuplicates(component, emptyMap())
        validateNoDependencyDuplicates(component, emptyMap())
        validateDependenciesAreResolved(component, DependencyResolver(context))
        validateNoDependencyCycles(component, DependencyGraphBuilder(context, true))
        validateFactories(component, DependencyResolver(context))
      }

    validateInjectionTargetsAreResolved(context.injectableTargets, context.components)
  }

  private fun validateNoModuleDuplicates(
    component: Component,
    moduleToComponentsMap: Map<Type.Object, List<Type.Object>>
  ) {
    val newModuleTypeToComponentMap = HashMap(moduleToComponentsMap)
    component.getModulesWithDescendants().forEach { module ->
      val oldComponents = newModuleTypeToComponentMap[module.type]
      val newComponents = if (oldComponents == null) listOf(component.type) else oldComponents + component.type
      newModuleTypeToComponentMap[module.type] = newComponents
    }

    if (component.subcomponents.isEmpty()) {
      // FIXME: This code will report duplicate errors in some cases.
      newModuleTypeToComponentMap.forEach { (moduleType, componentTypes) ->
        if (componentTypes.size > 1) {
          val moduleName = moduleType.className
          val componentNames = componentTypes.joinToString { it.className }
          errorReporter.reportError(
            "Module $moduleName provided multiple times in a single component hierarchy: $componentNames"
          )
        }
      }
    } else {
      component.subcomponents.forEach { subcomponentType ->
        val subcomponent = context.findComponentByType(subcomponentType)
        if (subcomponent != null) {
          validateNoModuleDuplicates(subcomponent, newModuleTypeToComponentMap)
        } else {
          val subcomponentName = subcomponentType.className
          val componentName = component.type.className
          errorReporter.reportError("Subcomponent $subcomponentName of component $componentName not found")
        }
      }
    }
  }

  private fun validateNoDependencyDuplicates(
    component: Component,
    dependencyTypeToModuleMap: Map<Dependency, List<Type.Object>>
  ) {
    val newDependencyTypeToModuleMap = HashMap(dependencyTypeToModuleMap)
    component.getModulesWithDescendants().forEach { module ->
      module.providers.forEach { provider ->
        val oldModules = newDependencyTypeToModuleMap[provider.dependency]
        val newModules = if (oldModules == null) listOf(module.type) else oldModules + listOf(module.type)
        newDependencyTypeToModuleMap[provider.dependency] = newModules
      }
    }

    if (component.subcomponents.isEmpty()) {
      // FIXME: This code will report duplicate errors in some cases.
      newDependencyTypeToModuleMap.forEach { (dependency, moduleTypes) ->
        if (moduleTypes.size > 1) {
          val moduleNames = moduleTypes.joinToString { it.className }
          errorReporter.reportError(
            "Dependency $dependency provided multiple times in a single component hierarchy by modules: $moduleNames"
          )
        }
      }
    } else {
      component.subcomponents.forEach { subcomponentType ->
        val subcomponent = context.findComponentByType(subcomponentType)
        if (subcomponent != null) {
          validateNoDependencyDuplicates(subcomponent, newDependencyTypeToModuleMap)
        } else {
          val subcomponentName = subcomponentType.className
          val componentName = component.type.className
          errorReporter.reportError("Subcomponent $subcomponentName of component $componentName not found")
        }
      }
    }
  }

  private fun validateDependenciesAreResolved(component: Component, resolver: DependencyResolver) {
    resolver.add(component)
    val unresolvedDependencies = resolver.getUnresolvedDependenciesAndResolveAllDependencies()
    if (unresolvedDependencies.isNotEmpty()) {
      val componentName = component.type.className
      for (unresolvedDependency in unresolvedDependencies) {
        errorReporter.reportError("Unresolved dependency $unresolvedDependency in component $componentName")
      }
    }
  }

  private fun validateNoDependencyCycles(component: Component, builder: DependencyGraphBuilder) {
    builder.add(component)
    val dependencyGraph = builder.build()
    val cycles = dependencyGraph.findCycles()
    if (cycles.isNotEmpty()) {
      val componentName = component.type.className
      for (cycle in cycles) {
        val cycleString = cycle.joinToString(" -> ")
        errorReporter.reportError("Dependency cycle $cycleString in component $componentName")
      }
    }
  }

  private fun validateFactories(component: Component, resolver: DependencyResolver) {
    resolver.add(component)
    component.getModulesWithDescendants()
      .flatMap { module -> module.factories.asSequence() }
      .distinctBy { factory -> factory.type }
      .forEach { factory ->
        for (provisionPoint in factory.provisionPoints) {
          val injectees = provisionPoint.injectionPoint.injectees
          val resolvedDependencies = resolver.getResolvedDependencies()
          for (injectee in injectees) {
            val shouldBeResolved = shouldFactoryInjecteeBeResolved(injectee)
            validateFactoryDependency(component, factory, injectee.dependency, resolvedDependencies, shouldBeResolved)
          }
        }
      }
  }

  private fun shouldFactoryInjecteeBeResolved(injectee: FactoryInjectee): Boolean {
    return when (injectee) {
      is FactoryInjectee.FromInjector -> true
      is FactoryInjectee.FromMethod -> false
    }
  }

  private fun validateFactoryDependency(
    component: Component,
    factory: Factory,
    dependency: Dependency,
    resolvedDependencies: Set<Dependency>,
    shouldBeResolved: Boolean
  ) {
    val isResolved = dependency.boxed() in resolvedDependencies
    if (!isResolved && shouldBeResolved) {
      val factoryName = factory.type.className
      val componentName = component.type.className
      errorReporter.reportError("Unresolved dependency $dependency in factory $factoryName in component $componentName")
    }
  }

  private fun validateInjectionTargetsAreResolved(
    injectionTargets: Iterable<InjectionTarget>,
    components: Iterable<Component>
  ) {
    val dependencyResolver = DependencyResolver(context)
    components.forEach { dependencyResolver.add(it) }
    val resolvedDependencies = dependencyResolver.getResolvedDependencies()

    injectionTargets.forEach { injectionTarget ->
      injectionTarget.injectionPoints.forEach { injectionPoint ->
        validateInjectionPointIsResolved(injectionTarget.type, injectionPoint, resolvedDependencies)
      }
    }
  }

  private fun validateInjectionPointIsResolved(
    injectionTargetType: Type.Object,
    injectionPoint: InjectionPoint,
    resolvedDependencies: Set<Dependency>
  ) {
    val dependencies = getDependenciesForInjectionPoint(injectionPoint)
    val element = getElementForInjectionPoint(injectionPoint)

    val unresolvedDependencies = dependencies.filter { it !in resolvedDependencies }
    if (unresolvedDependencies.isNotEmpty()) {
      val injectionTargetName = injectionTargetType.className
      unresolvedDependencies.forEach { dependency ->
        errorReporter.reportError(
          "Unresolved dependency $dependency in $element at $injectionTargetName"
        )
      }
    }
  }

  private fun getDependenciesForInjectionPoint(injectionPoint: InjectionPoint): Collection<Dependency> {
    return when (injectionPoint) {
      is InjectionPoint.Field -> listOf(injectionPoint.injectee.dependency.boxed())
      is InjectionPoint.Method -> injectionPoint.injectees.map { it.dependency.boxed() }
    }
  }

  private fun getElementForInjectionPoint(injectionPoint: InjectionPoint): Element<out Type> {
    return when (injectionPoint) {
      is InjectionPoint.Field -> injectionPoint.field
      is InjectionPoint.Method -> injectionPoint.method
    }
  }
}
