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

import org.apache.commons.io.FileUtils

import java.io.File
import java.io.IOException

class DirectoryClassFileWriter @Throws(IOException::class)
constructor(private val classesDirectory: File) : ClassFileWriter() {

  @Throws(IOException::class)
  override fun writeFile(path: String, fileData: ByteArray) {
    val file = File(classesDirectory, path)
    val directory = file.parentFile
    directory?.mkdirs()
    FileUtils.writeByteArrayToFile(file, fileData)
  }

  @Throws(IOException::class)
  override fun createDirectory(path: String) {
    val file = File(classesDirectory, path)
    // noinspection ResultOfMethodCallIgnored
    file.mkdirs()
  }

  override fun close() {
  }
}
