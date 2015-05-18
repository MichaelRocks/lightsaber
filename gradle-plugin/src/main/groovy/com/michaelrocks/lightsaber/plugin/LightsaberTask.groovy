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

package com.michaelrocks.lightsaber.plugin

import com.michaelrocks.lightsaber.processor.LightsaberParameters
import com.michaelrocks.lightsaber.processor.LightsaberProcessor;
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.GradleScriptException
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.TaskAction;

public class LightsaberTask extends DefaultTask {
    private File classesDir
    private File outputDir

    LightsaberTask() {
        logging.captureStandardOutput LogLevel.INFO
    }

    File getClassesDir() {
        return classesDir
    }

    void setClassesDir(final Object path) {
        classesDir = project.file(path)
        inputs.dir(path)
    }

    File getOutputDir() {
        return outputDir
    }

    void setOutputDir(final Object path) {
        outputDir = project.file(path)
        // FIXME: Need to handle UP-TO-DATE somehow.
        // Currently if LightsaberTask is UP-TO-DATE it doesn't change destinationDir of JavaCompile task.
        // outputs.dir(path)
    }

    @TaskAction
    def process() {
        final def parameters = new LightsaberParameters()
        parameters.classes = classesDir.absolutePath
        parameters.output = outputDir.absolutePath
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
