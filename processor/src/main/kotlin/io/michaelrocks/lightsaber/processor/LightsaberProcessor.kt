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

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import io.michaelrocks.lightsaber.processor.commons.using
import io.michaelrocks.lightsaber.processor.io.*
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

class LightsaberProcessor(private val parameters: LightsaberParameters) {

  @Throws(Exception::class)
  fun process() {
    if (parameters.jar != null) {
      val jarFile = File(parameters.jar)
      processJarFile(jarFile)
    } else if (parameters.classes != null) {
      val classesDirectory = File(parameters.classes)
      processClasses(classesDirectory)
    }
    logger.info("DONE")
  }

  @Throws(Exception::class)
  private fun processJarFile(file: File) {
    val processedFile = File(parameters.output)
    ClassFileReader(JarClassFileTraverser(file)).use { classFileReader ->
      using(JarClassFileWriter(processedFile)) { classFileWriter ->
        processClassFiles(classFileReader, classFileWriter)
      }
    }
  }

  @Throws(Exception::class)
  private fun processClasses(directory: File) {
    val processedDirectory = File(parameters.output)
    FileUtils.deleteQuietly(processedDirectory)
    if (!processedDirectory.mkdirs()) {
      throw ProcessingException("Failed to create output directory " + processedDirectory)
    }

    ClassFileReader(DirectoryClassFileTraverser(directory)).use { classFileReader ->
      using(DirectoryClassFileWriter(processedDirectory)) { classFileWriter ->
        processClassFiles(classFileReader, classFileWriter)
      }
    }
  }

  @Throws(IOException::class)
  private fun processClassFiles(classFileReader: ClassFileReader, classFileWriter: ClassFileWriter) {
    val classProcessor = ClassProcessor(classFileReader, classFileWriter, parameters.libs)
    classProcessor.processClasses()
  }

  companion object {
    private val DEFAULT_SUFFIX = "-lightsaber"

    private val logger = LoggerFactory.getLogger(LightsaberProcessor::class.java)

    @JvmStatic fun main(args: Array<String>) {
      val parameters = LightsaberParameters()
      val parser = JCommander(parameters)

      try {
        parser.parse(*args)
        validateParameters(parameters)
      } catch (exception: ParameterException) {
        logger.error(exception.message)
        val builder = StringBuilder()
        parser.usage(builder)
        logger.error(builder.toString())
        System.exit(1)
      }

      configureLogging(parameters)

      val processor = LightsaberProcessor(parameters)
      try {
        processor.process()
      } catch (exception: Exception) {
        logger.error(exception.message)
        if (parameters.printStacktrace) {
          exception.printStackTrace()
        }
        System.exit(2)
      }

    }

    private fun validateParameters(parameters: LightsaberParameters) {
      if (parameters.jar == null && parameters.classes == null) {
        throw ParameterException("Either --jar or --classes must be specified")
      }
      if (parameters.jar != null && parameters.classes != null) {
        throw ParameterException("Either --jar or --classes can be specified but not both")
      }

      if (parameters.output == null) {
        if (parameters.jar != null) {
          parameters.output = FilenameUtils.removeExtension(
              parameters.jar) + DEFAULT_SUFFIX + FilenameUtils.getExtension(parameters.jar)
        } else {
          parameters.output = parameters.classes + DEFAULT_SUFFIX
        }
      }

      validateLibraries(parameters.libs)
    }

    private fun validateLibraries(libraries: List<File>?) {
      if (libraries == null || libraries.isEmpty()) {
        return
      }

      for (library in libraries) {
        if (!library.exists()) {
          throw ParameterException("Library doesn't exist: " + library)
        }
        if (!library.isFile) {
          throw ParameterException("Library is not a file: " + library)
        }
      }
    }

    private fun configureLogging(parameters: LightsaberParameters) {
      val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
      root.level = parameters.loggingLevel
    }
  }
}
