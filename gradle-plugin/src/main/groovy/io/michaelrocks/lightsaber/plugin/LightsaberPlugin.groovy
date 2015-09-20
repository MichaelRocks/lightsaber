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
import org.gradle.api.Task
import org.gradle.api.logging.Logger

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
            logger.trace("Creating Lightsaber tasks for variant ${variant.name}")
            final String variantName = variant.name.capitalize()
            final String newTaskName = "lightsaberProcess$variantName"
            final File classesDir = variant.javaCompiler.destinationDir
            final File backupDir = new File(project.buildDir, "lightsaber/${variant.name}")
            final List<File> classpath = project.android.bootClasspath.toList() + variant.javaCompiler.classpath.toList()
            final LightsaberTask lightsaberProcess =
                    createLightsaberProcessTask(newTaskName, classesDir, backupDir, classpath)
            final BackupClassesTask backupTask =
                    createBackupClassFilesTask("lightsaberBackupClasses$variantName", classesDir, backupDir)
            configureTasks(lightsaberProcess, backupTask, variant.javaCompiler)
        }
    }

    private void setupLightsaberForJava() {
        logger.info("Setting up Lightsaber task for Java project ${project.name}...")
        final File classesDir = project.sourceSets.main.output.classesDir
        final File backupDir = new File(project.buildDir, "lightsaber")
        final List<File> classpath = project.tasks.compileJava.classpath.toList()
        final LightsaberTask lightsaberProcess =
                createLightsaberProcessTask("lightsaberProcess", classesDir, backupDir, classpath)
        final BackupClassesTask backupTask =
                createBackupClassFilesTask("lightsaberBackupClasses", classesDir, backupDir)
        configureTasks(lightsaberProcess, backupTask, project.tasks.compileJava)
    }

    private void configureTasks(final LightsaberTask lightsaberTask, final BackupClassesTask backupTask,
            final Task compileTask) {
        lightsaberTask.mustRunAfter compileTask
        lightsaberTask.dependsOn compileTask
        lightsaberTask.dependsOn backupTask
        compileTask.finalizedBy lightsaberTask

        final Task cleanBackupTask = project.tasks["clean${backupTask.name.capitalize()}"]
        final Task cleanLightsaberTask = project.tasks["clean${lightsaberTask.name.capitalize()}"]

        cleanBackupTask.doFirst {
            backupTask.clean()
        }

        cleanLightsaberTask.deleteAllActions()
        cleanLightsaberTask.doFirst {
            lightsaberTask.clean()
        }

        cleanLightsaberTask.dependsOn cleanBackupTask
    }

    private LightsaberTask createLightsaberProcessTask(final String taskName, final File classesDir,
            final File backupDir, final List<File> libraries) {
        logger.info("Creating Lighsaber task $taskName...")
        logger.info("  Source classes directory [$backupDir]")
        logger.info("  Processed classes directory [$classesDir]")

        return project.task(taskName, type: LightsaberTask) {
            description 'Processes .class files with Lightsaber Processor.'
            setBackupDir(backupDir)
            setClassesDir(classesDir)
            setClasspath(libraries)
        } as LightsaberTask
    }

    private BackupClassesTask createBackupClassFilesTask(final String taskName, final File classesDir,
            final File backupDir) {
        return project.task(taskName, type: BackupClassesTask) {
            description 'Back up original .class files.'
            setClassesDir(classesDir)
            setBackupDir(backupDir)
        } as BackupClassesTask
    }
}
