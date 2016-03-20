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

import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.mirrors.signature.MethodSignatureMirror
import org.objectweb.asm.Type

private val DEFAULT_CONSTRUCTOR_DESCRIPTOR = MethodDescriptor.forConstructor()
private val STATIC_INITIALIZER_DESCRIPTOR =
    MethodDescriptor.forMethod(MethodDescriptor.STATIC_INITIALIZER_NAME, Type.VOID_TYPE)

fun MethodDescriptor(name: String, desc: String) = MethodDescriptor(name, Type.getType(desc))
fun MethodDescriptor(name: String, type: Type) = MethodDescriptor(name, type, type.toMethodSignatureMirror())

class MethodDescriptor(val name: String, val type: Type, val signature: MethodSignatureMirror) {
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

  override fun equals(other: Any?): Boolean {
    if (this === other) {
      return true
    }

    val that = other as? MethodDescriptor ?: return false
    return name == that.name && type == that.type
  }

  override fun hashCode(): Int {
    var hashCode = 31
    hashCode = hashCode * 17 + name.hashCode()
    hashCode = hashCode * 17 + type.hashCode()
    return hashCode
  }

  override fun toString() = "MethodDescriptor{name = $name, type = $type}"
}

val MethodDescriptor.descriptor: String
  get() = type.descriptor

val MethodDescriptor.returnType: GenericType
  get() = signature.returnType

val MethodDescriptor.argumentTypes: List<GenericType>
  get() = signature.parameterTypes

val MethodDescriptor.isConstructor: Boolean
  get() = MethodDescriptor.isConstructor(name)

val MethodDescriptor.isDefaultConstructor: Boolean
  get() = MethodDescriptor.isDefaultConstructor(name, type.descriptor)

val MethodDescriptor.isStaticInitializer: Boolean
  get() = STATIC_INITIALIZER_DESCRIPTOR == this

private fun Type.toMethodSignatureMirror(): MethodSignatureMirror =
    MethodSignatureMirror.Builder().run {
      returnType(GenericType.RawType(returnType))
      for (argumentType in argumentTypes) {
        addParameterType(GenericType.RawType(argumentType))
      }
      build()
    }
