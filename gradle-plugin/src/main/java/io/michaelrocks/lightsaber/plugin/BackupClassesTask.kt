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

package io.michaelrocks.lightsaber.plugin

import io.michaelrocks.lightsaber.processor.watermark.WatermarkChecker
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

open class BackupClassesTask : DefaultTask() {
  @InputDirectory
  var classesDir: File? = null

  @OutputDirectory
  var backupDir: File? = null

  @TaskAction
  fun backupClasses() {
    val backupDir = checkNotNull(backupDir) { "backupDir is not set" }
    val classesDir = checkNotNull(classesDir) { "classesDir is not set" }

    logger.info("Backing up classes...")
    logger.info("  from [{}]", classesDir)
    logger.info("    to [{}]", backupDir)

    if (!classesDir.exists()) {
      logger.info("Classes directory doesn't exists. Nothing to backup.")
      backupDir.deleteRecursively()
      return
    }

    val visitedFiles = copyUpdatedFiles(classesDir, backupDir)
    removeUnvisitedFiles(backupDir, visitedFiles)
  }

  private fun copyUpdatedFiles(classesDir: File, backupDir: File): Set<String> {
    logger.info("Copying updated files...")
    logger.info("  from [{}]", classesDir)
    logger.info("    to [{}]", backupDir)

    val visitedPaths = mutableSetOf<String>()
    classesDir.walk().forEach { file ->
      if (!file.isDirectory) {
        logger.debug("Checking {}...", file)
        val relativePath = file.toRelativeString(classesDir)
        visitedPaths.add(relativePath)
        if (WatermarkChecker.isLightsaberClass(file)) {
          logger.debug("Watermark found - skipping")
        } else {
          val backupFile = backupDir.resolve(relativePath)
          if (!backupFile.exists()) {
            logger.debug("Backup file doesn't exist - copying")
            file.copyToWithLastModified(backupFile)
          } else if (file.lastModified() != backupFile.lastModified()) {
            logger.debug("File was updated - copying")
            file.copyToWithLastModified(backupFile, true)
          } else {
            logger.debug("File wasn't updated - skipping")
          }
        }
      }
    }
    return visitedPaths
  }

  private fun removeUnvisitedFiles(backupDir: File, visitedPaths: Set<String>) {
    logger.info("Removing abandoned files...")
    logger.info("  from [{}]", backupDir)

    backupDir.walkBottomUp().forEach { file ->
      if (file.isDirectory) {
        file.delete()
      } else {
        logger.debug("Checking {}...", file)
        val relativePath = file.toRelativeString(backupDir)
        if (!visitedPaths.contains(relativePath)) {
          logger.debug("File is abandoned - removing")
          file.delete()
        } else {
          logger.debug("File isn't abandoned - skipping")
        }
      }
    }
  }

  fun clean() {
    val backupDir = checkNotNull(backupDir) { "backupDir is not set" }
    val classesDir = checkNotNull(classesDir) { "classesDir is not set" }

    logger.info("Restoring patched files...")
    logger.info("  from [{}]", backupDir)
    logger.info("    to [{}]", classesDir)

    if (!classesDir.exists()) {
      return
    }

    backupDir.walk().forEach { file ->
      if (!file.isDirectory) {
        logger.debug("Reverting {}...", file)
        val relativePath = file.toRelativeString(backupDir)
        val classesFile = classesDir.resolve(relativePath)
        file.copyToWithLastModified(classesFile, true)
      }
    }
  }

  private fun File.copyToWithLastModified(target: File, overwrite: Boolean = false) {
    copyTo(target, overwrite)
    target.setLastModified(lastModified())
  }
}
