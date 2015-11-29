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

abstract class ClassFileWriter : ClassFileVisitor(null), Closeable {
  @Throws(IOException::class)
  override fun visitClassFile(path: String, classData: ByteArray) {
    writeFile(path, classData)
  }

  @Throws(IOException::class)
  override fun visitOtherFile(path: String, fileData: ByteArray) {
    writeFile(path, fileData)
  }

  @Throws(IOException::class)
  override fun visitDirectory(path: String) {
    createDirectory(path)
  }

  @Throws(IOException::class)
  protected abstract fun writeFile(path: String, fileData: ByteArray)

  @Throws(IOException::class)
  protected abstract fun createDirectory(path: String)
}
