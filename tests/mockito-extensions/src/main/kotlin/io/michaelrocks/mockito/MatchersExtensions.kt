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
import org.mockito.Matchers
import kotlin.reflect.KClass

@Suppress("CAST_NEVER_SUCCEEDS")
fun <T> Any?.toNotNull(): T = null as T

inline fun <reified T : Any> anyObject(): T =
    Matchers.anyObject<T>().toNotNull()

inline fun <reified T : Any> anyVararg(): T =
    Matchers.anyVararg<T>().toNotNull()

inline fun <reified T : Any> any(type: KClass<T>): T =
    Matchers.any<T>(type.java).toNotNull()

inline fun <reified T : Any> any(): T =
    Matchers.any<T>().toNotNull()

fun anyString(): String =
    Matchers.anyString().toNotNull()

fun anyList(): List<*> =
    Matchers.anyList().toNotNull()

inline fun <reified T : Any> anyListOf(): List<T> =
    Matchers.anyListOf(T::class.java).toNotNull()

inline fun <reified T : Any> anySet(): Set<*> =
    Matchers.anySet().toNotNull()

inline fun <reified T : Any> anySetOf(): Set<T> =
    Matchers.anySetOf(T::class.java).toNotNull()

inline fun <reified T : Any> anyMap(): Map<*, *> =
    Matchers.anyMap().toNotNull()

inline fun <reified K : Any, reified V : Any> anyMapOf(): Map<K, V> =
    Matchers.anyMapOf(K::class.java, V::class.java).toNotNull()

inline fun <reified T : Any> anyCollection(): Collection<*> =
    Matchers.anyCollection().toNotNull()

inline fun <reified T : Any> anyCollectionOf(): Collection<T> =
    Matchers.anyCollectionOf(T::class.java).toNotNull()

inline fun <reified T : Any> isA(): T =
    Matchers.isA(T::class.java).toNotNull()

inline fun <reified T : Any> eq(value: T): T =
    Matchers.eq(value).toNotNull()

inline fun <reified T : Any> refEq(value: T, vararg excludeFields: String): T =
    Matchers.refEq(value, *excludeFields).toNotNull()

inline fun <reified T : Any> same(value: T): T =
    Matchers.same(value).toNotNull()

inline fun <reified T : Any> isNull(): T =
    Matchers.isNull(T::class.java).toNotNull()

inline fun <reified T : Any> notNull(): T =
    Matchers.notNull().toNotNull()

inline fun <reified T : Any> isNotNull(): T =
    Matchers.isNotNull(T::class.java).toNotNull()

fun contains(substring: String): String =
    Matchers.contains(substring).toNotNull()

fun matches(regex: String): String =
    Matchers.matches(regex).toNotNull()

fun endsWith(suffix: String): String =
    Matchers.endsWith(suffix).toNotNull()

fun startsWith(prefix: String): String =
    Matchers.startsWith(prefix).toNotNull()

inline fun <reified T : Any> argThat(matcher: ArgumentMatcher<T>): T =
    Matchers.argThat(matcher).toNotNull()
