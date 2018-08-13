/*
 * Copyright 2018 Michael Rozumyanskiy
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

data class InjectionContext(
    val components: Collection<Component>,
    val injectableTargets: Collection<InjectionTarget>,
    val providableTargets: Collection<InjectionTarget>
) {
  private val componentsByType = components.associateBy { it.type }
  private val modulesByType = components.flatMap { it.modules }.associateBy { it.type }
  private val injectableTargetsByType = injectableTargets.associateBy { it.type }
  private val providableTargetsByType = providableTargets.associateBy { it.type }

  fun findComponentByType(componentType: Type.Object): Component? =
      componentsByType[componentType]

  fun findModuleByType(moduleType: Type.Object): Module? =
      modulesByType[moduleType]

  fun findInjectableTargetByType(injectableTargetType: Type.Object): InjectionTarget? =
      injectableTargetsByType[injectableTargetType]

  fun findProvidableTargetByType(providableTargetType: Type.Object): InjectionTarget? =
      providableTargetsByType[providableTargetType]
}
