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

package io.michaelrocks.lightsaber.processor.io

import java.io.File
import java.io.InputStream
import kotlin.io.readBytes as readBytesKotlin

// XXX: Kotlin 1.3.xx is ABI-incompatible with 1.2.xx because readBytes(Int) extension function was replaced with readBytes().
// Depending on Gradle version it may happen so that the plugin is loaded with 1.2.xx version of Kotlin.
// In this case we don't want to crash and try to workaround ABI incompatibility with these hacks.

@Suppress("deprecation")
internal fun File.readBytes(): ByteArray {
  return if (KotlinVersion.CURRENT.isAtLeast(1, 3, 0)) {
    readBytesKotlin()
  } else {
    inputStream().use { it.readBytesKotlin(DEFAULT_BUFFER_SIZE) }
  }
}

@Suppress("deprecation")
internal fun InputStream.readBytes(): ByteArray {
  return if (KotlinVersion.CURRENT.isAtLeast(1, 3, 0)) {
    readBytesKotlin()
  } else {
    readBytesKotlin(DEFAULT_BUFFER_SIZE)
  }
}
