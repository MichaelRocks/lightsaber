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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.compile.AbstractCompile

class BaseLightsaberPlugin implements Plugin<Project> {
    Project project
    Logger logger

    @Override
    void apply(final Project project) {
        this.project = project
        this.logger = project.logger
    }

    private boolean trySetupLightsaberForAndroid() {

    }

    void createTasks(final File classesDir, final File backupDir, final List<File> classpath,
            final AbstractCompile compileTask, final String nameSuffix = "") {
        final String suffix = nameSuffix.capitalize()
        final LightsaberTask lightsaberTask =
                createLightsaberProcessTask("lightsaberProcess$suffix", classesDir, backupDir, classpath)
        final BackupClassesTask backupTask =
                createBackupClassFilesTask("lightsaberBackupClasses$suffix", classesDir, backupDir)
        configureTasks(lightsaberTask, backupTask, compileTask)

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