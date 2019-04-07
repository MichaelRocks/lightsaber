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

package io.michaelrocks.lightsaber.processor.commons

import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.mirrors.toArrayType
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor

val GenericType.isParameterized: Boolean
  get() = this is GenericType.Parameterized

val GenericType.rawType: Type
  get() = when (this) {
    is GenericType.Raw -> type
    is GenericType.Parameterized -> type
    is GenericType.Array -> elementType.rawType.toArrayType()
    else -> throw IllegalArgumentException("Unsupported generic type: $this")
  }
val GenericType.parameterType: Type?
  get() = when (this) {
    is GenericType.Parameterized -> typeArguments[0].rawType
    is GenericType.Raw -> null
    is GenericType.Array -> null
    else -> throw IllegalArgumentException("Unsupported generic type: $this")
  }

fun MethodMirror.toMethodDescriptor(): MethodDescriptor {
  return MethodDescriptor(name, type)
}

fun FieldMirror.toFieldDescriptor(): FieldDescriptor {
  return FieldDescriptor(name, type)
}
