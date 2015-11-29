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

import io.michaelrocks.lightsaber.processor.ProcessingException
import io.michaelrocks.lightsaber.processor.commons.closeQuitely
import io.michaelrocks.lightsaber.processor.commons.using
import org.apache.commons.collections4.iterators.EnumerationIterator
import org.apache.commons.collections4.iterators.IteratorIterable
import java.io.File
import java.io.IOException
import java.util.jar.JarEntry
import java.util.jar.JarFile

class JarClassFileTraverser @Throws(IOException::class) constructor(
    sourceFile: File
) : ClassFileTraverser<JarEntry>() {
  private val jarFile: JarFile =
      try {
        JarFile(sourceFile, true)
      } catch (exception: IOException) {
        throw ProcessingException(exception, sourceFile.name)
      }

  @Throws(IOException::class)
  override fun iterateFiles(): Iterable<JarEntry> {
    val entriesIterator = EnumerationIterator(jarFile.entries())
    return IteratorIterable(entriesIterator)
  }

  override fun isDirectory(file: JarEntry): Boolean = file.isDirectory

  override fun getFilePath(file: JarEntry): String = file.name

  @Throws(IOException::class)
  override fun readAsByteArray(file: JarEntry): ByteArray =
      using(jarFile.getInputStream(file)) { stream -> stream.readBytes() }

  override fun close() = jarFile.closeQuitely()
}
