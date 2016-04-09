/*
 * Copyright 2016 Michael Rozumyanskiy
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
  val jar = parameters.jar?.absoluteFile
  val classes = parameters.classes?.absoluteFile
  if (jar == null && classes == null) {
    throw ParameterException("Either --jar or --classes must be specified")
  }
  if (jar != null && classes != null) {
    throw ParameterException("Either --jar or --classes can be specified but not both")
  }

  if (parameters.output == null) {
    if (jar != null) {
      parameters.output = File("${jar.nameWithoutExtension}$DEFAULT_SUFFIX.${jar.extension}")
    } else {
      parameters.output = File("$classes$DEFAULT_SUFFIX")
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
  }
}

private fun configureLogging(parameters: LightsaberParameters) {
  val root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
  root.level = parameters.loggingLevel
}