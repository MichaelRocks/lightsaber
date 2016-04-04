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

package io.michaelrocks.lightsaber.processor.generation.model

import io.michaelrocks.lightsaber.processor.commons.Types
import org.objectweb.asm.Type
import java.util.*

data class GenerationConfiguration(
    val packageInjectorConfigurators: Collection<InjectorConfigurator>,
    val injectorConfigurators: Collection<InjectorConfigurator>,
    val membersInjectors: Collection<MembersInjector>,
    val packageInvaders: Collection<PackageInvader>
) {
  private val packageInvadersByPackageName = HashMap<String, PackageInvader>()

  val allInjectorConfigurators: Collection<InjectorConfigurator>
    get() = packageInjectorConfigurators + injectorConfigurators

  init {
    packageInvaders.associateByTo(packageInvadersByPackageName) { it.packageName }
  }

  fun findPackageInvaderByTargetType(targetType: Type): PackageInvader? =
      packageInvadersByPackageName[Types.getPackageName(targetType)]
}
