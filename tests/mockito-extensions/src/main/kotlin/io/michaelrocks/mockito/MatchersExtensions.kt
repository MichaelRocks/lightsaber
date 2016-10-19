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

package io.michaelrocks.mockito

import org.mockito.ArgumentMatcher
import org.mockito.ArgumentMatchers
import kotlin.reflect.KClass

@Suppress("CAST_NEVER_SUCCEEDS")
fun <T> Any?.toNotNull(): T = null as T

inline fun <reified T : Any> any(type: KClass<T>): T =
    ArgumentMatchers.any<T>(type.java).toNotNull()

inline fun <reified T : Any> any(): T =
    ArgumentMatchers.any<T>().toNotNull()

fun anyBoolean(): Boolean =
    ArgumentMatchers.anyBoolean()
fun anyByte(): Byte =
    ArgumentMatchers.anyByte()
fun anyChar(): Char =
    ArgumentMatchers.anyChar()
fun anyFloat(): Float =
    ArgumentMatchers.anyFloat()
fun anyDouble(): Double =
    ArgumentMatchers.anyDouble()
fun anyInt(): Int =
    ArgumentMatchers.anyInt()
fun anyLong(): Long =
    ArgumentMatchers.anyLong()
fun anyShort(): Short =
    ArgumentMatchers.anyShort()

fun anyString(): String =
    ArgumentMatchers.anyString().toNotNull()

inline fun <reified T> anyIterable(): Iterable<T> =
    ArgumentMatchers.anyIterable<T>().toNotNull()

inline fun <reified T> anyList(): List<T> =
    ArgumentMatchers.anyList<T>().toNotNull()

inline fun <reified T : Any> anySet(): Set<T> =
    ArgumentMatchers.anySet<T>().toNotNull()

inline fun <reified K : Any, reified V : Any> anyMap(): Map<*, *> =
    ArgumentMatchers.anyMap<K, V>().toNotNull()

inline fun <reified T : Any> anyCollection(): Collection<T> =
    ArgumentMatchers.anyCollection<T>().toNotNull()

inline fun <reified T : Any> isA(): T =
    ArgumentMatchers.isA(T::class.java).toNotNull()

inline fun <reified T : Any> eq(value: T): T =
    ArgumentMatchers.eq(value).toNotNull()

inline fun <reified T : Any> refEq(value: T, vararg excludeFields: String): T =
    ArgumentMatchers.refEq(value, *excludeFields).toNotNull()

inline fun <reified T : Any> same(value: T): T =
    ArgumentMatchers.same(value).toNotNull()

inline fun <reified T : Any> isNull(): T =
    ArgumentMatchers.isNull<T>().toNotNull()

inline fun <reified T : Any> isNotNull(): T =
    ArgumentMatchers.isNotNull<T>().toNotNull()

fun contains(substring: String): String =
    ArgumentMatchers.contains(substring).toNotNull()

fun matches(regex: String): String =
    ArgumentMatchers.matches(regex).toNotNull()

fun endsWith(suffix: String): String =
    ArgumentMatchers.endsWith(suffix).toNotNull()

fun startsWith(prefix: String): String =
    ArgumentMatchers.startsWith(prefix).toNotNull()

inline fun <reified T : Any> argThat(matcher: ArgumentMatcher<T>): T =
    ArgumentMatchers.argThat(matcher).toNotNull()
