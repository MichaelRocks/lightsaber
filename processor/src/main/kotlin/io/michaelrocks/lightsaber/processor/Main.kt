/*
 * Copyright 2017 Michael Rozumyanskiy
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

import ch.qos.logback.classic.Logger
import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import io.michaelrocks.lightsaber.processor.logging.getLogger
import org.slf4j.LoggerFactory
import java.io.File

private val DEFAULT_SUFFIX = "-lightsaber"

fun main(args: Array<String>) {
  val logger = getLogger("Main")
  val parameters = LightsaberParameters()
  val parser = JCommander(parameters)

  try {
    parser.parse(*normalizeArguments(args))
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

private fun normalizeArguments(arguments: Array<String>): Array<String> {
  val result = arguments.copyOf()
  var lastIndex = 0
  for (i in 1..result.size - 1) {
    if (result[lastIndex].endsWith('\\')) {
      result[lastIndex] = "${result[lastIndex].substring(0, result[lastIndex].length - 1)} ${result[i]}"
    } else {
      lastIndex += 1
      result[lastIndex] = result[i]
    }
  }
  return result.copyOfRange(0, lastIndex + 1)
}

private fun validateParameters(parameters: LightsaberParameters) {
  if (parameters.inputs.isEmpty()) {
    throw ParameterException("Inputs mustn't be empty")
  }

  if (parameters.outputs.isEmpty()) {
    parameters.outputs = generateOutputs(parameters.inputs)
  }

  if (parameters.outputs.size != parameters.inputs.size) {
    throw ParameterException("The number of output paths must be the same as the number of input paths")
  }

  if (parameters.source == null) {
    parameters.source = File("src$DEFAULT_SUFFIX")
  }

  if (parameters.gen == null) {
    parameters.gen = File("gen$DEFAULT_SUFFIX")
  }

  validateLibraries(parameters.classpath)
  validateLibraries(parameters.bootClasspath)
}

private fun generateOutputs(inputs: List<File>): List<File> {
  return inputs.map { file ->
    if (file.isDirectory) {
      File("${file.path}$DEFAULT_SUFFIX")
    } else {
      val extension = file.extension
      val path = file.path.substringBeforeLast('.')
      if (extension.isEmpty()) {
        File("$path$DEFAULT_SUFFIX")
      } else {
        File("$path$DEFAULT_SUFFIX.$extension")
      }
    }
  }
}

private fun validateLibraries(libraries: List<File>?) {
  if (libraries == null || libraries.isEmpty()) {
    return
  }

  for (library in libraries) {
    if (!library.exists()) {
      throw ParameterException("Library doesn't exist: " + library)
    }
  }
}

private fun configureLogging(parameters: LightsaberParameters) {
  val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
  root.level = parameters.loggingLevel
}
