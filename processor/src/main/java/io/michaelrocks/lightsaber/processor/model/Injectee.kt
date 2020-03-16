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

package io.michaelrocks.lightsaber.processor.model

import io.michaelrocks.grip.mirrors.Annotated
import io.michaelrocks.grip.mirrors.AnnotationCollection
import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.Type

data class Injectee(
  val dependency: Dependency,
  val converter: Converter,
  override val annotations: AnnotationCollection = EmptyAnnotationCollection
) : Annotated {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Injectee

    if (dependency != other.dependency) return false
    if (converter != other.converter) return false

    return true
  }

  override fun hashCode(): Int {
    var result = dependency.hashCode()
    result = 31 * result + converter.hashCode()
    return result
  }

  override fun toString(): String {
    return "Injectee(dependency=$dependency, converter=$converter)"
  }

  private object EmptyAnnotationCollection : AnnotationCollection, Collection<AnnotationMirror> by emptyList() {
    override fun contains(type: Type.Object): Boolean = false
    override fun get(type: Type.Object): AnnotationMirror? = null
  }
}
