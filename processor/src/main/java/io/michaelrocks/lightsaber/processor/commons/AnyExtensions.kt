/*
 * Copyright 2019 Michael Rozumyanskiy
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

import java.io.Closeable

inline fun <T : Closeable, R> using(closeable: T, block: (T) -> R): R {
  try {
    return block(closeable)
  } finally {
    try {
      closeable.close()
    } catch (exception: Exception) {
      // Ignore the exception.
    }
  }
}

inline fun <reified T> Any.cast(): T =
  this as T

inline fun <T : Any> given(condition: Boolean, body: () -> T): T? =
  if (condition) body() else null

infix inline fun <T : Any> T?.or(body: () -> T): T =
  this ?: body()

fun exhaustive(@Suppress("UNUSED_PARAMETER") ignored: Any?) = Unit
