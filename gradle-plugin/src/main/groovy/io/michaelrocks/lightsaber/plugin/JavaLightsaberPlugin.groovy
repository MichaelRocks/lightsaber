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

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.compile.AbstractCompile

class JavaLightsaberPlugin extends BaseLightsaberPlugin {
  @Override
  void apply(final Project project) {
    super.apply(project)

    addDependencies('compile')
    addDependencies('testCompile')

    project.afterEvaluate {
      if (project.plugins.hasPlugin('java')) {
        setupLightsaberForJava()
        setupLightsaberForJavaTest()
      } else {
        throw new GradleException("Project should use Java plugin")
      }
    }
  }

  private void setupLightsaberForJava() {
    logger.info("Setting up Lightsaber task for Java project ${project.name}...")
    final File classesDir = project.sourceSets.main.output.classesDir
    final File backupDir = new File(project.buildDir, "lightsaber")
    final List<File> classpath = project.tasks.compileJava.classpath.toList()
    final AbstractCompile compileTask = project.tasks.compileJava as AbstractCompile
    createTasks(classesDir, backupDir, classpath, compileTask)
  }

  private void setupLightsaberForJavaTest() {
    logger.info("Setting up Lightsaber task for Java test project ${project.name}...")
    final File classesDir = project.sourceSets.test.output.classesDir
    final File backupDir = new File(project.buildDir, "lightsaberTest")
    final List<File> classpath = project.tasks.compileTestJava.classpath.toList()
    final AbstractCompile compileTask = project.tasks.compileTestJava as AbstractCompile
    createTasks(classesDir, backupDir, classpath, compileTask, "test")
  }

  private void createTasks(final File classesDir, final File backupDir, final List<File> classpath,
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
