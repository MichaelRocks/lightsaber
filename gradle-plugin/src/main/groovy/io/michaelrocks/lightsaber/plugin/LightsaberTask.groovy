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

import io.michaelrocks.lightsaber.processor.LightsaberParameters
import io.michaelrocks.lightsaber.processor.LightsaberProcessor
import io.michaelrocks.lightsaber.processor.watermark.WatermarkChecker
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

public class LightsaberTask extends DefaultTask {
    @InputDirectory
    File backupDir
    @OutputDirectory
    File classesDir
    @InputFiles
    List<File> classpath

    LightsaberTask() {
        logging.captureStandardOutput LogLevel.INFO
    }

    @TaskAction
    void process() {
        final def parameters = new LightsaberParameters()
        parameters.classes = backupDir.absolutePath
        parameters.output = classesDir.absolutePath
        parameters.libs = classpath
        parameters.verbose = logger.isEnabled(LogLevel.DEBUG)
        logger.info("Starting Lightsaber processor: $parameters")
        final def processor = new LightsaberProcessor(parameters)
        try {
            processor.process()
        } catch (final Exception exception) {
            throw new GradleScriptException('Lightsaber processor failed to process files', exception)
        }
    }

    void clean() {
        logger.info("Removing patched files...")
        logger.info("  from [$classesDir]")

        final Path classesPath = classesDir.toPath()
        Files.walkFileTree(classesPath, new SimpleFileVisitor<Path>() {
            @Override
            FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                super.visitFile(file, attrs)
                logger.debug("Checking $file...")
                if (WatermarkChecker.isLightsaberClass(file)) {
                    logger.debug("File was patched - removing")
                    Files.delete(file)
                } else {
                    logger.debug("File wasn't patched - skipping")
                }
                return FileVisitResult.CONTINUE
            }

            @Override
            FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
                PathMethods.deleteDirectoryIfEmpty(dir)
                return super.postVisitDirectory(dir, exc)
            }
        })
    }
}
