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

import java.io.File

private enum class FileType { DIRECTORY, JAR }

private val File.fileType: FileType
  get() = when {
    isDirectory -> FileType.DIRECTORY
    extension.endsWith("jar", ignoreCase = true) -> FileType.JAR
    else -> error("Unknown file type for file $this")
  }

fun File.fileSource(): FileSource =
    when (fileType) {
      FileType.DIRECTORY -> DirectoryFileSource(this)
      FileType.JAR -> JarFileSource(this)
    }

fun File.fileSink(intputFile: File): FileSink =
    when (intputFile.fileType) {
      FileType.DIRECTORY -> DirectoryFileSink(this)
      FileType.JAR -> JarFileSink(this)
    }
