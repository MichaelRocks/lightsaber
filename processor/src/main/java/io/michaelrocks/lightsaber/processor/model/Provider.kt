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

data class Provider(
    val type: Type.Object,
    val provisionPoint: ProvisionPoint,
    val moduleType: Type.Object,
    val scope: Scope
) {
  val dependency: Dependency
    get() = provisionPoint.dependency
}

val Provider.isConstructorProvider: Boolean
  get() = provisionPoint is ProvisionPoint.Constructor
