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

import io.michaelrocks.lightsaber.processor.ProcessingException
import io.michaelrocks.lightsaber.processor.model.Binding
import io.michaelrocks.lightsaber.processor.model.Dependency

interface BindingRegistry {
  val bindings: Collection<Binding>

  fun findBindingByDependency(dependency: Dependency): Binding?
  fun findBindingsByAncestor(ancestor: Dependency): Collection<Binding>
}

interface MutableBindingRegistry : BindingRegistry {
  fun registerBinding(binding: Binding)
}

class BindingRegistryImpl : MutableBindingRegistry {
  private val dependencyToBindingMap = HashMap<Dependency, Binding>()
  private val ancestorToBindingsMap = HashMap<Dependency, MutableCollection<Binding>>()
  override val bindings: Collection<Binding> get() = dependencyToBindingMap.values.toList()

  override fun findBindingByDependency(dependency: Dependency): Binding? {
    return dependencyToBindingMap[dependency]
  }

  override fun findBindingsByAncestor(ancestor: Dependency): Collection<Binding> {
    return ancestorToBindingsMap[ancestor].orEmpty()
  }

  override fun registerBinding(binding: Binding) {
    dependencyToBindingMap.put(binding.dependency, binding)?.let {
      throw ProcessingException("Cannot register ancestor ${binding.ancestor} for ${binding.dependency} because another ancestor is registered: ${it.ancestor}")
    }

    val bindings = ancestorToBindingsMap.getOrPut(binding.ancestor) { HashSet() }
    if (binding in bindings) {
      throw ProcessingException("Cannot register ancestor ${binding.ancestor} for ${binding.dependency} because it's already registered")
    }

    bindings += binding
  }
}
