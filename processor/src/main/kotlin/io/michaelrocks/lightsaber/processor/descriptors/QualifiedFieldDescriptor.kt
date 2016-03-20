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

import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.signature.FieldSignatureMirror
import org.objectweb.asm.Type

data class QualifiedFieldDescriptor(val field: FieldDescriptor, val qualifier: AnnotationMirror?)

val QualifiedFieldDescriptor.name: String
  get() = field.name

val QualifiedFieldDescriptor.parameterType: Type?
  get() = field.parameterType

val QualifiedFieldDescriptor.parameterized: Boolean
  get() = field.parameterized

val QualifiedFieldDescriptor.signature: FieldSignatureMirror
  get() = field.signature

val QualifiedFieldDescriptor.rawType: Type
  get() = field.rawType

val QualifiedFieldDescriptor.descriptor: String
  get() = field.descriptor
