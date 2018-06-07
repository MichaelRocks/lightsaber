/*
 * Copyright 2018 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.plugin

import io.michaelrocks.lightsaber.processor.LightsaberParameters
import io.michaelrocks.lightsaber.processor.LightsaberProcessor
import io.michaelrocks.lightsaber.processor.watermark.WatermarkChecker
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class LightsaberTask : DefaultTask() {
  @InputFiles
  var backupDirs: List<File> = emptyList()
  @OutputDirectories
  var classesDirs: List<File> = emptyList()
  @OutputDirectory
  var sourceDir: File? = null
  @InputFiles
  @Classpath
  var classpath: List<File> = emptyList()
  @InputFiles
  @Classpath
  var bootClasspath: List<File> = emptyList()

  init {
    logging.captureStandardOutput(LogLevel.INFO)
  }

  @TaskAction
  fun process() {
    validate()

    val parameters = LightsaberParameters(
        inputs = backupDirs,
        outputs = classesDirs,
        classpath = classpath,
        bootClasspath = bootClasspath,
        gen = classesDirs[0],
        debug = logger.isDebugEnabled,
        info = logger.isInfoEnabled
    )

    logger.info("Starting Lightsaber processor: {}", parameters)
    val processor = LightsaberProcessor(parameters)
    try {
      processor.process()
    } catch (exception: Exception) {
      throw GradleScriptException("Lightsaber processor failed to process files", exception)
    }
  }

  fun clean() {
    validate()
    logger.info("Removing patched files from {}", classesDirs)

    for (classesDir in classesDirs) {
      if (!classesDir.exists()) {
        continue
      }

      classesDir.walkBottomUp().forEach { file ->
        if (file.isDirectory) {
          file.delete()
        } else {
          logger.debug("Checking {}...", file)
          if (WatermarkChecker.isLightsaberClass(file)) {
            logger.debug("File was patched - removing")
            file.delete()
          } else {
            logger.debug("File wasn't patched - skipping")
          }
        }
      }
    }

    sourceDir?.let { sourceDir ->
      logger.info("Removing a directory with generated source files: {}", sourceDir)
      sourceDir.deleteRecursively()
    }
  }

  private fun validate() {
    require(classesDirs.isNotEmpty()) { "classesDirs is not set" }
    require(backupDirs.isNotEmpty()) { "backupDirs is not set" }
    require(classesDirs.size == backupDirs.size) { "classesDirs and backupDirs must have equal size" }
    requireNotNull(sourceDir) { "sourceDir is not set" }
  }
}
