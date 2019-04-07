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

package io.michaelrocks.lightsaber.processor.descriptors

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getMethodType
import org.objectweb.asm.Type as AsmType

private val DEFAULT_CONSTRUCTOR_DESCRIPTOR = AsmType.getMethodDescriptor(AsmType.VOID_TYPE)

private val DEFAULT_CONSTRUCTOR = MethodDescriptor.forConstructor()
private val STATIC_INITIALIZER =
    MethodDescriptor.forMethod(MethodDescriptor.STATIC_INITIALIZER_NAME, Type.Primitive.Void)

fun MethodDescriptor(name: String, desc: String) = MethodDescriptor(name, getMethodType(desc))

data class MethodDescriptor(val name: String, val type: Type.Method) {
  companion object {
    const val CONSTRUCTOR_NAME = "<init>"
    const val STATIC_INITIALIZER_NAME = "<clinit>"

    fun forMethod(name: String, returnType: Type, vararg argumentTypes: Type): MethodDescriptor {
      return MethodDescriptor(name, getMethodType(returnType, *argumentTypes))
    }

    fun forConstructor(vararg argumentTypes: Type): MethodDescriptor {
      return MethodDescriptor(CONSTRUCTOR_NAME, getMethodType(Type.Primitive.Void, *argumentTypes))
    }

    fun forDefaultConstructor(): MethodDescriptor {
      return DEFAULT_CONSTRUCTOR
    }

    fun forStaticInitializer(): MethodDescriptor {
      return STATIC_INITIALIZER
    }

    fun isConstructor(methodName: String): Boolean {
      return CONSTRUCTOR_NAME == methodName
    }

    fun isDefaultConstructor(methodName: String, methodDesc: String): Boolean {
      return isConstructor(methodName) && DEFAULT_CONSTRUCTOR_DESCRIPTOR == methodDesc
    }

    fun isStaticInitializer(methodName: String): Boolean {
      return STATIC_INITIALIZER_NAME == methodName
    }
  }
}

val MethodDescriptor.descriptor: String
  get() = type.descriptor

val MethodDescriptor.returnType: Type
  get() = type.returnType

val MethodDescriptor.argumentTypes: List<Type>
  get() = type.argumentTypes

val MethodDescriptor.isConstructor: Boolean
  get() = MethodDescriptor.isConstructor(name)

val MethodDescriptor.isDefaultConstructor: Boolean
  get() = MethodDescriptor.isDefaultConstructor(name, descriptor)

val MethodDescriptor.isStaticInitializer: Boolean
  get() = STATIC_INITIALIZER == this
