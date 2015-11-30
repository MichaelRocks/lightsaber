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

package io.michaelrocks.lightsaber.processor

import io.michaelrocks.lightsaber.processor.commons.using
import io.michaelrocks.lightsaber.processor.io.*
import io.michaelrocks.lightsaber.processor.logging.getLogger
import java.io.File

class LightsaberProcessor(private val parameters: LightsaberParameters) {
  private val logger = getLogger()

  @Throws(Exception::class)
  fun process() {
    val jar = parameters.jar
    val classes = parameters.classes
    if (jar != null) {
      processJarFile(jar)
    } else if (classes != null) {
      processClasses(classes)
    }
    logger.info("DONE")
  }

  private fun processJarFile(file: File) {
    ClassFileReader(JarClassFileTraverser(file)).use { classFileReader ->
      using(JarClassFileWriter(parameters.output!!)) { classFileWriter ->
        processClassFiles(classFileReader, classFileWriter)
      }
    }
  }

  private fun processClasses(directory: File) {
    parameters.output!!.let { output ->
      output.deleteRecursively()
      if (!output.mkdirs()) {
        throw ProcessingException("Failed to create output directory $output")
      }

      ClassFileReader(DirectoryClassFileTraverser(directory)).use { classFileReader ->
        using(DirectoryClassFileWriter(output)) { classFileWriter ->
          processClassFiles(classFileReader, classFileWriter)
        }
      }
    }
  }

  private fun processClassFiles(classFileReader: ClassFileReader, classFileWriter: ClassFileWriter) {
    val classProcessor = ClassProcessor(classFileReader, classFileWriter, parameters.libs)
    classProcessor.processClasses()
  }
}
