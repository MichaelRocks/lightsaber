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
import org.gradle.api.DefaultTask
import org.gradle.api.GradleScriptException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

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
    def process() {
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
}
