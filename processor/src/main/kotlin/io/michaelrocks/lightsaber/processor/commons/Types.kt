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

package io.michaelrocks.lightsaber.processor.commons

import io.michaelrocks.lightsaber.Injector
import io.michaelrocks.lightsaber.Key
import io.michaelrocks.lightsaber.Module
import io.michaelrocks.lightsaber.Provides
import org.apache.commons.collections4.BidiMap
import org.apache.commons.collections4.bidimap.DualHashBidiMap
import org.objectweb.asm.Type
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

object Types {
  val OBJECT_TYPE = getType<Any>()
  val STRING_TYPE = getType<String>()
  val INJECT_TYPE = getType<Inject>()
  val PROVIDES_TYPE = getType<Provides>()
  val MODULE_TYPE = getType<Module>()
  val INJECTOR_TYPE = getType<Injector>()
  val PROVIDER_TYPE = getType<Provider<Any>>()
  val KEY_TYPE = getType<Key<Any>>()
  val CLASS_TYPE = getType<Class<Any>>()
  val ANNOTATION_TYPE = getType<Annotation>()

  val BOXED_VOID_TYPE = getType<Byte>()
  val BOXED_BOOLEAN_TYPE = getType<Boolean>()
  val BOXED_BYTE_TYPE = getType<Byte>()
  val BOXED_CHAR_TYPE = getType<Char>()
  val BOXED_DOUBLE_TYPE = getType<Double>()
  val BOXED_FLOAT_TYPE = getType<Float>()
  val BOXED_INT_TYPE = getType<Int>()
  val BOXED_LONG_TYPE = getType<Long>()
  val BOXED_SHORT_TYPE = getType<Short>()

  private val primitiveToBoxedMap: BidiMap<Type, Type>

  init {
    primitiveToBoxedMap = DualHashBidiMap<Type, Type>()
    primitiveToBoxedMap.put(Type.VOID_TYPE, BOXED_VOID_TYPE)
    primitiveToBoxedMap.put(Type.BOOLEAN_TYPE, BOXED_BOOLEAN_TYPE)
    primitiveToBoxedMap.put(Type.BYTE_TYPE, BOXED_BYTE_TYPE)
    primitiveToBoxedMap.put(Type.CHAR_TYPE, BOXED_CHAR_TYPE)
    primitiveToBoxedMap.put(Type.DOUBLE_TYPE, BOXED_DOUBLE_TYPE)
    primitiveToBoxedMap.put(Type.FLOAT_TYPE, BOXED_FLOAT_TYPE)
    primitiveToBoxedMap.put(Type.INT_TYPE, BOXED_INT_TYPE)
    primitiveToBoxedMap.put(Type.LONG_TYPE, BOXED_LONG_TYPE)
    primitiveToBoxedMap.put(Type.SHORT_TYPE, BOXED_SHORT_TYPE)
  }

  fun box(type: Type): Type = primitiveToBoxedMap[type] ?: type
  fun unbox(type: Type): Type = primitiveToBoxedMap.getKey(type) ?: type

  fun isPrimitive(type: Type): Boolean = primitiveToBoxedMap.containsKey(type)

  fun getArrayType(type: Type): Type {
    return Type.getType("[" + type.descriptor)
  }

  fun getPackageName(type: Type): String {
    check(type.sort == Type.OBJECT)
    val internalName = type.internalName
    val lastSeparatorIndex = internalName.lastIndexOf('/')
    return if (lastSeparatorIndex == -1) "" else internalName.substring(0, lastSeparatorIndex)
  }
}

inline fun <reified T : Any> getType(): Type = Type.getType(T::class.java)

val Type.isPrimitive: Boolean
  get() = Types.isPrimitive(this)
val Type.packageName: String
  get() = Types.getPackageName(this)

fun Type.box() = Types.box(this)
fun Type.unbox() = Types.unbox(this)

fun Type.toArrayType(): Type = Types.getArrayType(this)

val Class<*>.type: Type
  get() = Type.getType(this)
val Class<*>.internalName: String
  get() = Type.getInternalName(this)
val Class<*>.descriptor: String
  get() = Type.getDescriptor(this)

val KClass<*>.type: Type
  get() = Type.getType(java)
val KClass<*>.internalName: String
  get() = java.internalName
val KClass<*>.descriptor: String
  get() = java.descriptor