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

package io.michaelrocks.lightsaber.processor.commons

import io.michaelrocks.grip.mirrors.signature.GenericType
import org.objectweb.asm.Type

val GenericType.isParameterized: Boolean
  get() = this is GenericType.ParameterizedType

val GenericType.rawType: Type
  get() = when (this) {
    is GenericType.RawType -> type
    is GenericType.ParameterizedType -> type
    else -> throw IllegalArgumentException("Unsupported generic type: $this")
  }
val GenericType.parameterType: Type?
  get() = when (this) {
    is GenericType.ParameterizedType -> typeArguments[0].rawType
    is GenericType.RawType -> null
    else -> throw IllegalArgumentException("Unsupported generic type: $this")
  }
