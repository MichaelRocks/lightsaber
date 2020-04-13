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

package io.michaelrocks.lightsaber.processor.commons

import io.michaelrocks.lightsaber.processor.model.Converter
import io.michaelrocks.lightsaber.processor.model.Injectee
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint

fun ProvisionPoint.getInjectees(): Collection<Injectee> {
  return when (this) {
    is ProvisionPoint.Constructor -> injectionPoint.injectees
    is ProvisionPoint.Method -> injectionPoint.injectees
    is ProvisionPoint.Field -> emptyList()
    is ProvisionPoint.Binding -> listOf(Injectee(binding, Converter.Instance))
  }
}
