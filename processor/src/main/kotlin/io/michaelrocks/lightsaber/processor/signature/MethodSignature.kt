/*
 * Copyright 2015 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.processor.signature

import io.michaelrocks.lightsaber.processor.commons.mapToArray
import org.objectweb.asm.Type
import java.util.*

class MethodSignature {
  val methodType: Type
  val returnType: TypeSignature
  val argumentTypes: List<TypeSignature>

  constructor(methodType: Type) {
    this.methodType = methodType
    this.returnType = TypeSignature(methodType.returnType)
    this.argumentTypes = Collections.unmodifiableList(methodType.argumentTypes.map { TypeSignature(it) })
  }

  constructor(returnType: TypeSignature, argumentTypes: List<TypeSignature>) {
    this.methodType = createRawType(returnType, argumentTypes)
    this.returnType = returnType
    this.argumentTypes = Collections.unmodifiableList(ArrayList(argumentTypes))
  }

  private fun createRawType(returnTypeSignature: TypeSignature, argumentTypesSignatures: List<TypeSignature>): Type {
    val argumentTypes = argumentTypesSignatures.mapToArray { it.rawType }
    return Type.getMethodType(returnTypeSignature.rawType, *argumentTypes)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    if (other == null || javaClass != other.javaClass) {
      return false
    }

    val that = other as MethodSignature
    return methodType == that.methodType
  }

  override fun hashCode(): Int = 17 * 37 + methodType.hashCode()

  override fun toString(): String = argumentTypes.joinToString(", ", "(", "): $returnType")
}
