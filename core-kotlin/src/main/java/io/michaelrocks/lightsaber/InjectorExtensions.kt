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

package io.michaelrocks.lightsaber

import javax.inject.Provider
import kotlin.reflect.KClass

val lightsaber: Lightsaber
  get() = Lightsaber.get()

fun <T : Any> Injector.getInstance(type: Class<out T>, annotation: Annotation): T =
    Lightsaber.getInstance(this, type, annotation)

fun <T : Any> Injector.getInstance(type: KClass<out T>): T =
    Lightsaber.getInstance(this, type.java)

fun <T : Any> Injector.getInstance(type: KClass<out T>, annotation: Annotation): T =
    Lightsaber.getInstance(this, type.java, annotation)

inline fun <reified T : Any> Injector.getInstance(): T =
    getInstance(T::class)

inline fun <reified T : Any> Injector.getInstance(annotation: Annotation): T =
    getInstance(T::class, annotation)

fun <T : Any> Injector.getProvider(type: Class<out T>, annotation: Annotation): Provider<T> =
    Lightsaber.getProvider(this, type, annotation)

fun <T : Any> Injector.getProvider(type: KClass<out T>): Provider<T> =
    Lightsaber.getProvider(this, type.java)

fun <T : Any> Injector.getProvider(type: KClass<out T>, annotation: Annotation): Provider<T> =
    Lightsaber.getProvider(this, type.java, annotation)

inline fun <reified T : Any> Injector.getProvider(): Provider<T> =
    getProvider(T::class)

inline fun <reified T : Any> Injector.getProvider(annotation: Annotation): Provider<T> =
    getProvider(T::class, annotation)

@Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")
inline fun <T> inject(): T = null as T
