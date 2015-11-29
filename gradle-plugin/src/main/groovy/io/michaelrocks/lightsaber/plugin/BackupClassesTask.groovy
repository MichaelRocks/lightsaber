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

import io.michaelrocks.lightsaber.processor.watermark.WatermarkChecker
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

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

    final Path classesPath = classesDir.toPath()
    final Path backupPath = backupDir.toPath()
    final Set<Path> visitedPaths = copyUpdatedFiles(classesPath, backupPath)
    removeUnvisitedFiles(backupPath, visitedPaths)
  }

  private Set<Path> copyUpdatedFiles(final Path classesPath, final Path backupPath) {
    logger.info("Copying updated files...")
    logger.info("  from [$classesPath]")
    logger.info("    to [$backupPath]")

    final Set<Path> visitedPaths = new HashSet<>()
    Files.walkFileTree(classesPath, new SimpleFileVisitor<Path>() {
      @Override
      FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        logger.debug("Checking $file...")
        super.visitFile(file, attrs)
        final Path relativePath = classesPath.relativize(file)
        visitedPaths.add(relativePath)
        if (WatermarkChecker.isLightsaberClass(file)) {
          logger.debug("Watermark found - skipping")
          return FileVisitResult.CONTINUE
        }

        final Path backupFile = backupPath.resolve(relativePath)
        if (!Files.exists(backupFile)) {
          logger.debug("Backup file doesn't exist - copying")
          Files.createDirectories(backupFile.parent)
          Files.copy(file, backupFile, StandardCopyOption.COPY_ATTRIBUTES)
          return FileVisitResult.CONTINUE
        }

        final BasicFileAttributes fileAttributes =
            Files.readAttributes(file, BasicFileAttributes.class)
        final BasicFileAttributes backupFileAttributes =
            Files.readAttributes(backupFile, BasicFileAttributes.class)
        if (fileAttributes.lastModifiedTime() != backupFileAttributes.lastModifiedTime()) {
          logger.debug("File was updated - copying")
          Files.createDirectories(backupFile.parent)
          Files.copy(file, backupFile,
              StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
        } else {
          logger.debug("File wasn't updated - skipping")
        }
        return FileVisitResult.CONTINUE
      }
    })
    return visitedPaths
  }

  private void removeUnvisitedFiles(final Path backupPath, final Set<Path> visitedPaths) {
    logger.info("Removing abandoned files...")
    logger.info("  from [$backupPath]")

    Files.walkFileTree(backupPath, new SimpleFileVisitor<Path>() {
      @Override
      FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        super.visitFile(file, attrs)
        logger.debug("Checking $file...")
        final Path relativePath = backupPath.relativize(file)
        if (!visitedPaths.contains(relativePath)) {
          logger.debug("File is abandoned - removing")
          Files.delete(file)
        } else {
          logger.debug("File isn't abandoned - skipping")
        }
        return FileVisitResult.CONTINUE
      }

      @Override
      FileVisitResult postVisitDirectory(final Path dir, final IOException exception) throws IOException {
        PathMethods.deleteDirectoryIfEmpty(dir)
        return super.postVisitDirectory(dir, exception)
      }
    })
  }

  void clean() {
    logger.info("Restoring patched files...")
    logger.info("  from [$backupDir]")
    logger.info("    to [$classesDir]")

    final Path classesPath = classesDir.toPath()
    final Path backupPath = backupDir.toPath()
    Files.walkFileTree(backupPath, new SimpleFileVisitor<Path>() {
      @Override
      FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
        super.visitFile(file, attrs)
        logger.debug("Reverting $file...")
        final Path relativePath = backupPath.relativize(file)
        final Path classesFile = classesPath.resolve(relativePath)
        Files.createDirectories(classesFile.parent)
        Files.copy(file, classesFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES)
        return FileVisitResult.CONTINUE
      }
    })
  }
}
