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

package io.michaelrocks.lightsaber.processor.files

import io.michaelrocks.lightsaber.processor.commons.closeQuitely
import io.michaelrocks.lightsaber.processor.io.FileSource
import io.michaelrocks.lightsaber.processor.io.fileSource
import org.objectweb.asm.Type
import java.io.Closeable
import java.io.File
import java.util.*

interface FileRegistry : Closeable {
  fun add(files: Iterable<File>)
  fun add(file: File)
  fun readClass(type: Type): ByteArray
}

class FileRegistryImpl : FileRegistry {
  private val sources = HashMap<File, FileSource>()
  private val filesByTypes = HashMap<Type, File>()

  override fun add(files: Iterable<File>) {
    require(files.all { it !in sources }) { "Some files already added to registry" }
    files.forEach { add(it) }
  }

  override fun add(file: File) {
    require(file !in sources) { "File $file already added to registry" }
    val fileSource = file.fileSource()
    sources.put(file, fileSource)
    fileSource.listFiles { path, type ->
      if (type == FileSource.EntryType.CLASS) {
        filesByTypes.put(Type.getObjectType(path.substringBeforeLast(".class")), file)
      }
    }
  }

  override fun readClass(type: Type): ByteArray {
    val file = filesByTypes[type]!!
    val fileSource = sources[file]!!
    return fileSource.readFile("${type.internalName}.class")
  }

  override fun close() {
    sources.values.forEach { it.closeQuitely() }
    sources.clear()
    filesByTypes.clear()
  }
}
