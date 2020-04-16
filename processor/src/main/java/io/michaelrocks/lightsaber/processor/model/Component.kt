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

package io.michaelrocks.lightsaber.processor.model

import io.michaelrocks.grip.mirrors.Type

data class Component(
  val type: Type.Object,
  val parent: Type.Object?,
  val defaultModule: Module,
  val subcomponents: Collection<Type.Object>
) {

  fun getModulesWithDescendants(): Sequence<Module> = sequence {
    yieldModulesWithDescendants(listOf(defaultModule))
  }

  fun getModuleProvidersWithDescendants(): Sequence<ModuleProvider> = sequence {
    yieldModuleProvidersWithDescendants(defaultModule.moduleProviders)
  }

  private suspend fun SequenceScope<Module>.yieldModulesWithDescendants(modules: Iterable<Module>) {
    modules.forEach { module ->
      yield(module)
      yieldModulesWithDescendants(module.modules)
    }
  }

  private suspend fun SequenceScope<ModuleProvider>.yieldModuleProvidersWithDescendants(providers: Iterable<ModuleProvider>) {
    providers.forEach { provider ->
      yield(provider)
      yieldModuleProvidersWithDescendants(provider.module.moduleProviders)
    }
  }
}
