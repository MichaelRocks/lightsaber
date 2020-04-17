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

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.mirrors.Type

fun Grip.getAncestors(type: Type.Object): Sequence<Type.Object> = classRegistry.getAncestors(type)

fun ClassRegistry.getAncestors(type: Type.Object): Sequence<Type.Object> = sequence {
  yieldAncestors(type, this@getAncestors)
}

private suspend fun SequenceScope<Type.Object>.yieldAncestors(type: Type.Object, classRegistry: ClassRegistry) {
  yield(type)
  val mirror = classRegistry.getClassMirror(type)
  mirror.superType?.let { yieldAncestors(it, classRegistry) }
  mirror.interfaces.forEach { yieldAncestors(it, classRegistry) }
}
