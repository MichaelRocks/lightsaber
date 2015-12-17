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

package io.michaelrocks.lightsaber.plugin

import groovy.io.FileVisitResult
import io.michaelrocks.lightsaber.processor.watermark.WatermarkChecker
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class BackupClassesTask extends DefaultTask {
  @InputDirectory
  File classesDir

  @OutputDirectory
  File backupDir

  @TaskAction
  void backupClasses() {
    logger.info("Backing up classes...")
    logger.info("  from [$classesDir]")
    logger.info("    to [$backupDir]")

    final Set<String> visitedFiles = copyUpdatedFiles(classesDir, backupDir)
    removeUnvisitedFiles(backupDir, visitedFiles)
  }

  private Set<String> copyUpdatedFiles(final File classesDir, final File backupDir) {
    logger.info("Copying updated files...")
    logger.info("  from [$classesDir]")
    logger.info("    to [$backupDir]")

    final Set<String> visitedPaths = new HashSet<>()
    classesDir.traverse { final file ->
      if (file.isDirectory()) {
        return FileVisitResult.CONTINUE
      }

      logger.debug("Checking $file...")
      final String relativePath = FileMethods.relativize(classesDir, file)
      visitedPaths.add(relativePath)
      if (WatermarkChecker.isLightsaberClass(file)) {
        logger.debug("Watermark found - skipping")
        return FileVisitResult.CONTINUE
      }

      final File backupFile = FileMethods.resolve(backupDir, relativePath)
      if (!backupFile.exists()) {
        logger.debug("Backup file doesn't exist - copying")
        FileMethods.createParentDirectories(backupFile)
        FileMethods.copyTo(file, backupFile)
        return FileVisitResult.CONTINUE
      }

      if (file.lastModified() != backupFile.lastModified()) {
        logger.debug("File was updated - copying")
        FileMethods.createParentDirectories(backupFile)
        FileMethods.copyTo(file, backupFile, true)
      } else {
        logger.debug("File wasn't updated - skipping")
      }
      return FileVisitResult.CONTINUE
    }
    return visitedPaths
  }

  private void removeUnvisitedFiles(final File backupDir, final Set<String> visitedPaths) {
    logger.info("Removing abandoned files...")
    logger.info("  from [$backupDir]")

    backupDir.traverse(
        postDir: { final File dir -> FileMethods.deleteDirectoryIfEmpty(dir) }
    ) { final file ->
      if (file.isDirectory()) {
        return FileVisitResult.CONTINUE
      }

      logger.debug("Checking $file...")
      final String relativePath = FileMethods.relativize(backupDir, file)
      if (!visitedPaths.contains(relativePath)) {
        logger.debug("File is abandoned - removing")
        file.delete()
      } else {
        logger.debug("File isn't abandoned - skipping")
      }
      return FileVisitResult.CONTINUE
    }
  }

  void clean() {
    logger.info("Restoring patched files...")
    logger.info("  from [$backupDir]")
    logger.info("    to [$classesDir]")

    backupDir.traverse { final file ->
      if (file.isDirectory()) {
        return FileVisitResult.CONTINUE
      }

      logger.debug("Reverting $file...")
      final String relativePath = FileMethods.relativize(backupDir, file)
      final File classesFile = FileMethods.resolve(classesDir, relativePath)
      FileMethods.createParentDirectories(classesFile)
      FileMethods.copyTo(file, classesFile, true)
      return FileVisitResult.CONTINUE
    }
  }
}
