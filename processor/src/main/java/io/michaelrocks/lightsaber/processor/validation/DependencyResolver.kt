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

import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.boxed
import io.michaelrocks.lightsaber.processor.commons.getDependencies
import io.michaelrocks.lightsaber.processor.model.Component
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import io.michaelrocks.lightsaber.processor.model.Module

class DependencyResolver(
  private val context: InjectionContext
) {

  private val providedDependencies = HashSet<Dependency>()
  private val requiredDependencies = HashSet<Dependency>()

  init {
    providedDependencies += Dependency(GenericType.Raw(Types.INJECTOR_TYPE))
  }

  fun add(module: Module): DependencyResolver = apply {
    for (provider in module.providers) {
      providedDependencies += provider.dependency.boxed()
      requiredDependencies += provider.getDependencies(context)
    }

    add(module.modules)
  }

  fun add(modules: Iterable<Module>): DependencyResolver = apply {
    modules.forEach { add(it) }
  }

  fun add(component: Component): DependencyResolver = apply {
    add(component.modules)
  }

  fun getResolvedDependencies(): Set<Dependency> {
    return providedDependencies.toSet()
  }

  fun getUnresolvedDependencies(): Set<Dependency> {
    return requiredDependencies.toHashSet().apply {
      removeAll(providedDependencies)
    }
  }

  fun resolveAllDependencies() {
    requiredDependencies.clear()
  }

  fun getUnresolvedDependenciesAndResolveAllDependencies(): Set<Dependency> {
    return getUnresolvedDependencies().also {
      resolveAllDependencies()
    }
  }
}
