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

import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type

sealed class ProvisionPoint {
  abstract val containerType: Type.Object
  abstract val dependency: Dependency
  abstract val bridge: ProvisionPoint.Method?

  abstract class AbstractMethod : ProvisionPoint() {
    override val containerType: Type.Object
      get() = injectionPoint.containerType

    abstract val injectionPoint: InjectionPoint.Method

    val method: MethodMirror
      get() = injectionPoint.method
  }

  data class Constructor(
      override val dependency: Dependency,
      override val injectionPoint: InjectionPoint.Method
  ) : AbstractMethod() {
    override val bridge: ProvisionPoint.Method? get() = null
  }

  data class Method(
      override val dependency: Dependency,
      override val injectionPoint: InjectionPoint.Method,
      override val bridge: ProvisionPoint.Method?
  ) : AbstractMethod()

  data class Field(
      override val containerType: Type.Object,
      override val dependency: Dependency,
      override val bridge: ProvisionPoint.Method?,
      val field: FieldMirror
  ) : ProvisionPoint()
}
