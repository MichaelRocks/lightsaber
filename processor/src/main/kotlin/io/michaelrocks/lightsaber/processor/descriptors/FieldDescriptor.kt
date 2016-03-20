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

package io.michaelrocks.lightsaber.processor.descriptors

import io.michaelrocks.grip.mirrors.signature.FieldSignatureMirror
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.commons.isParameterized
import io.michaelrocks.lightsaber.processor.commons.parameterType
import io.michaelrocks.lightsaber.processor.commons.rawType
import org.objectweb.asm.Type

fun FieldDescriptor(name: String, desc: String): FieldDescriptor = FieldDescriptor(name, Type.getType(desc))
fun FieldDescriptor(name: String, type: Type): FieldDescriptor = FieldDescriptor(name, type.toFieldSignatureMirror())

data class FieldDescriptor(
    val name: String,
    val signature: FieldSignatureMirror
)

val FieldDescriptor.parameterized: Boolean
  get() = signature.type.isParameterized

val FieldDescriptor.rawType: Type
  get() = signature.type.rawType

val FieldDescriptor.parameterType: Type?
  get() = signature.type.parameterType

val FieldDescriptor.descriptor: String
  get() = signature.type.rawType.descriptor

fun Type.toFieldSignatureMirror(): FieldSignatureMirror =
    FieldSignatureMirror.Builder()
        .type(GenericType.RawType(this))
        .build()