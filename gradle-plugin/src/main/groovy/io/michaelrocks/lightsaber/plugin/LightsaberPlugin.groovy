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

import com.android.build.gradle.api.BaseVariant
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.compile.AbstractCompile

class LightsaberPlugin implements Plugin<Project> {
    private Project project
    private Logger logger

    @Override
    void apply(final Project project) {
        this.project = project
        this.logger = project.logger

        project.afterEvaluate {
            if (project.plugins.hasPlugin('com.android.application')) {
                setupLightsaberForAndroid(project.android.applicationVariants)
            } else if (project.plugins.hasPlugin('com.android.library')) {
                setupLightsaberForAndroid(project.android.libraryVariants)
            } else if (project.plugins.hasPlugin('java')) {
                setupLightsaberForJava()
            } else {
                throw new GradleException("Project should use either Android or Java plugin")
            }
        }
    }

    private void setupLightsaberForAndroid(final Collection<BaseVariant> variants) {
        logger.info("Setting up Lightsaber task for Android project ${project.name}...")
        variants.all { final BaseVariant variant ->
            final def classpath = project.android.bootClasspath.toList() + variant.javaCompiler.classpath.toList()
            final def variantName = variant.name.capitalize()
            final def newTaskName = "lightsaberProcess${variantName}"
            logger.trace("Creating Lightsaber task for variant ${variantName}")
            final def lightsaberProcess = createLightsaberProcessTask(newTaskName, variant.javaCompiler, classpath)
            lightsaberProcess.doLast {
                final def newDestinationDir = lightsaberProcess.outputDir
                logger.info("Changing JavaCompile destination dir...")
                logger.info("  from [${variant.javaCompiler.destinationDir.absolutePath}]")
                logger.info("    to [${newDestinationDir}]")
                variant.javaCompiler.destinationDir = newDestinationDir
            }
        }
    }

    private void setupLightsaberForJava() {
        logger.info("Setting up Lightsaber task for Java project ${project.name}...")

        final def lightsaberProcess = createLightsaberProcessTask("lightsaberProcess", project.tasks.compileJava,
                project.tasks.compileJava.classpath.toList())
        lightsaberProcess.doLast {
            final def newDestinationDir = lightsaberProcess.outputDir
            logger.info("Changing classes dir for main source set...")
            logger.info("  from [${project.sourceSets.main.output.classesDir}]")
            logger.info("    to [${newDestinationDir}]")
            project.sourceSets.main.output.classesDir = newDestinationDir
        }
    }

    private LightsaberTask createLightsaberProcessTask(final String taskName, final AbstractCompile javaCompile,
            final List<File> libraries) {
        logger.info("Creating Lighsaber task ${taskName}...")
        final def originalClasses = javaCompile.destinationDir.absolutePath
        final def lightsaberClasses = originalClasses + "-lightsaber"
        logger.info("  Source classes directory [$originalClasses]")
        logger.info("  Processed classes directory [$lightsaberClasses]")

        final def lightsaberProcess = project.task(taskName, type: LightsaberTask) {
            description 'Processes .class files with Lightsaber Processor.'
            classesDir originalClasses
            outputDir lightsaberClasses
            classpath = libraries
        } as LightsaberTask

        lightsaberProcess.mustRunAfter javaCompile
        lightsaberProcess.dependsOn javaCompile
        javaCompile.finalizedBy lightsaberProcess
        return lightsaberProcess
    }
}
