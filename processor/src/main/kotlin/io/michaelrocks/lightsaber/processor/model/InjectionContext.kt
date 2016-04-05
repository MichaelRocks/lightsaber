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

package io.michaelrocks.lightsaber.processor.model

import org.objectweb.asm.Type

data class InjectionContext(
    val modules: Collection<Module>,
    val packageModules: Collection<Module>,
    val injectableTargets: Collection<InjectionTarget>,
    val providableTargets: Collection<InjectionTarget>
) {
  val allModules: Collection<Module> = modules + packageModules

  private val modulesByType = modules.associateBy { it.type }
  private val injectableTargetsByType = injectableTargets.associateBy { it.type }
  private val providableTargetsByType = providableTargets.associateBy { it.type }

  fun findModuleByType(moduleType: Type): Module? =
      modulesByType[moduleType]

  fun findInjectableTargetByType(injectableTargetType: Type): InjectionTarget? =
      injectableTargetsByType[injectableTargetType]

  fun findProvidableTargetByType(providableTargetType: Type): InjectionTarget? =
      providableTargetsByType[providableTargetType]
}
