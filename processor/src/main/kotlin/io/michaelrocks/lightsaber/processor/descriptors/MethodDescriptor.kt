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

package io.michaelrocks.lightsaber.processor.descriptors

import io.michaelrocks.lightsaber.processor.signature.MethodSignature
import io.michaelrocks.lightsaber.processor.signature.TypeSignature
import org.objectweb.asm.Type

private val DEFAULT_CONSTRUCTOR_DESCRIPTOR = MethodDescriptor.forConstructor()
private val STATIC_INITIALIZER_DESCRIPTOR =
    MethodDescriptor.forMethod(MethodDescriptor.STATIC_INITIALIZER_NAME, Type.VOID_TYPE)

fun MethodDescriptor(name: String, desc: String): MethodDescriptor = MethodDescriptor(name, Type.getType(desc))
fun MethodDescriptor(name: String, type: Type): MethodDescriptor = MethodDescriptor(name, MethodSignature(type))

data class MethodDescriptor(val name: String, val signature: MethodSignature) {
  companion object {
    const val CONSTRUCTOR_NAME = "<init>"
    const val STATIC_INITIALIZER_NAME = "<clinit>"

    fun forMethod(name: String, returnType: Type, vararg argumentTypes: Type): MethodDescriptor {
      return MethodDescriptor(name, Type.getMethodType(returnType, *argumentTypes))
    }

    fun forConstructor(vararg argumentTypes: Type): MethodDescriptor {
      return MethodDescriptor(CONSTRUCTOR_NAME, Type.getMethodType(Type.VOID_TYPE, *argumentTypes))
    }

    fun forDefaultConstructor(): MethodDescriptor {
      return DEFAULT_CONSTRUCTOR_DESCRIPTOR
    }

    fun forStaticInitializer(): MethodDescriptor {
      return STATIC_INITIALIZER_DESCRIPTOR
    }

    fun isConstructor(methodName: String): Boolean {
      return CONSTRUCTOR_NAME == methodName
    }

    fun isDefaultConstructor(methodName: String, methodDesc: String): Boolean {
      return isConstructor(methodName) && Type.getMethodDescriptor(Type.VOID_TYPE) == methodDesc
    }

    fun isStaticInitializer(methodName: String): Boolean {
      return STATIC_INITIALIZER_NAME == methodName
    }
  }
}

val MethodDescriptor.type: Type
  get() = signature.methodType

val MethodDescriptor.descriptor: String
  get() = signature.methodType.descriptor

val MethodDescriptor.returnType: TypeSignature
  get() = signature.returnType

val MethodDescriptor.argumentTypes: List<TypeSignature>
  get() = signature.argumentTypes

val MethodDescriptor.isConstructor: Boolean
  get() = MethodDescriptor.isConstructor(name)

val MethodDescriptor.isDefaultConstructor: Boolean
  get() = MethodDescriptor.isDefaultConstructor(name, signature.methodType.descriptor)

val MethodDescriptor.isStaticInitializer: Boolean
  get() = STATIC_INITIALIZER_DESCRIPTOR == this
