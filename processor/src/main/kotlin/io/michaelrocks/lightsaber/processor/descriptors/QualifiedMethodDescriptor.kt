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
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.mirrors.signature.MethodSignatureMirror
import org.objectweb.asm.Type
import java.util.*

fun QualifiedMethodDescriptor(method: MethodDescriptor): QualifiedMethodDescriptor =
    QualifiedMethodDescriptor(method, emptyMap(), null)

fun QualifiedMethodDescriptor(method: MethodDescriptor,
    parameterQualifiers: Map<Int, AnnotationMirror>): QualifiedMethodDescriptor =
    QualifiedMethodDescriptor(method, parameterQualifiers, null)

fun QualifiedMethodDescriptor(
    method: MethodDescriptor,
    parameterQualifiersMap: Map<Int, AnnotationMirror>,
    resultQualifier: AnnotationMirror?
): QualifiedMethodDescriptor {
  val parameterCount = method.argumentTypes.size
  val parameterQualifiers = toQualifierList(parameterQualifiersMap, parameterCount)
  return QualifiedMethodDescriptor(method, parameterQualifiers, resultQualifier)
}

private fun toQualifierList(
    parameterQualifiers: Map<Int, AnnotationMirror>,
    parameterCount: Int
): List<AnnotationMirror?> {
  val qualifiers = ArrayList<AnnotationMirror?>(parameterCount)
  for (i in 0..parameterCount - 1) {
    qualifiers.add(parameterQualifiers[i])
  }
  return Collections.unmodifiableList(qualifiers)
}

data class QualifiedMethodDescriptor internal constructor(
    val method: MethodDescriptor,
    val parameterQualifiers: List<AnnotationMirror?>,
    val resultQualifier: AnnotationMirror?
)

val QualifiedMethodDescriptor.name: String
  get() = method.name

val QualifiedMethodDescriptor.descriptor: String
  get() = method.descriptor

val QualifiedMethodDescriptor.argumentTypes: List<GenericType>
  get() = method.argumentTypes

val QualifiedMethodDescriptor.returnType: GenericType
  get() = method.returnType

val QualifiedMethodDescriptor.type: Type
  get() = method.type

val QualifiedMethodDescriptor.signature: MethodSignatureMirror
  get() = method.signature

val QualifiedMethodDescriptor.isConstructor: Boolean
  get() = method.isConstructor

val QualifiedMethodDescriptor.isDefaultConstructor: Boolean
  get() = method.isDefaultConstructor

val QualifiedMethodDescriptor.isStaticInitializer: Boolean
  get() = method.isStaticInitializer
