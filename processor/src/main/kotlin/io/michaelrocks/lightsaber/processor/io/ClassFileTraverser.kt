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

package io.michaelrocks.lightsaber.processor.io

import java.io.Closeable
import java.io.IOException

abstract class ClassFileTraverser<T> : Closeable {
  @Throws(IOException::class)
  fun processFiles(visitor: ClassFileVisitor) {
    for (file in iterateFiles()) {
      processFile(visitor, file)
    }
    visitor.visitEnd()
  }

  @Throws(IOException::class)
  private fun processFile(visitor: ClassFileVisitor, file: T) {
    val path = getFilePath(file)
    if (path.isEmpty()) {
      // Skip empty path as it's the root path.
    } else if (isDirectory(file)) {
      visitor.visitDirectory(path)
    } else {
      val fileData = readAsByteArray(file)
      if (path.endsWith(".class")) {
        visitor.visitClassFile(path, fileData)
      } else {
        visitor.visitOtherFile(path, fileData)
      }
    }
  }

  @Throws(IOException::class)
  protected abstract fun iterateFiles(): Iterable<T>

  protected abstract fun isDirectory(file: T): Boolean

  protected abstract fun getFilePath(file: T): String

  @Throws(IOException::class)
  protected abstract fun readAsByteArray(file: T): ByteArray
}
