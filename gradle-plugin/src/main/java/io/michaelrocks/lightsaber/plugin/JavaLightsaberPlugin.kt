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

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.compile.JavaCompile
import java.io.File

class JavaLightsaberPlugin : BaseLightsaberPlugin() {
  override fun apply(project: Project) {
    super.apply(project)

    val lightsaber = project.extensions.create("lightsaber", JavaLightsaberPluginExtension::class.java)

    addDependencies("compile")
    addDependencies("testCompile")

    project.afterEvaluate {
      if (project.plugins.hasPlugin("java")) {
        setupLightsaberForJava()
        if (lightsaber.processTest) {
          setupLightsaberForJavaTest()
        }
      } else {
        throw GradleException("Project should use Java plugin")
      }
    }
  }

  private fun setupLightsaberForJava() {
    logger.info("Setting up Lightsaber task for Java project {}...", project.name)
    createTasks(project.sourceSets.main, project.tasks.compileJava)
  }

  private fun setupLightsaberForJavaTest() {
    logger.info("Setting up Lightsaber task for Java test project {}...", project.name)
    createTasks(project.sourceSets.test, project.tasks.compileTestJava, "test")
  }

  private fun createTasks(sourceSet: SourceSet, compileTask: JavaCompile, nameSuffix: String = "") {
    val suffix = nameSuffix.capitalize()
    val classesDir = sourceSet.output.classesDir
    val backupDir = File(project.buildDir, "lightsaber/classes$suffix")
    val sourceDir = File(project.buildDir, "lightsaber/src$suffix")
    val classpath = compileTask.classpath.toList()
    val bootClasspathString = compileTask.options.bootClasspath ?: System.getProperty("sun.boot.class.path")
    val bootClasspath = bootClasspathString?.split(File.pathSeparator)?.map { File(it) } ?: emptyList()
    val lightsaberTask =
        createLightsaberProcessTask("lightsaberProcess$suffix", classesDir, backupDir, sourceDir, classpath, bootClasspath)
    val backupTask =
        createBackupClassFilesTask("lightsaberBackupClasses$suffix", classesDir, backupDir)
    configureTasks(lightsaberTask, backupTask, compileTask)
  }

  private fun configureTasks(lightsaberTask: LightsaberTask, backupTask: BackupClassesTask, compileTask: Task) {
    lightsaberTask.mustRunAfter(compileTask)
    lightsaberTask.dependsOn(compileTask)
    lightsaberTask.dependsOn(backupTask)
    compileTask.finalizedBy(lightsaberTask)

    val cleanBackupTask = project.tasks["clean${backupTask.name.capitalize()}"]!!
    val cleanLightsaberTask = project.tasks["clean${lightsaberTask.name.capitalize()}"]!!

    cleanBackupTask.doFirst {
      backupTask.clean()
    }

    cleanLightsaberTask.deleteAllActions()
    cleanLightsaberTask.doFirst {
      lightsaberTask.clean()
    }

    cleanLightsaberTask.dependsOn(cleanBackupTask)
  }

  private fun createLightsaberProcessTask(
      taskName: String,
      classesDir: File,
      backupDir: File,
      sourceDir: File,
      classpath: List<File>,
      bootClasspath: List<File>
  ): LightsaberTask {
    logger.info("Creating Lightsaber task {}...", taskName)
    logger.info("  Source classes directory [{}]", backupDir)
    logger.info("  Processed classes directory [{}]", classesDir)

    return project.tasks.create(taskName, LightsaberTask::class.java) { task ->
      task.description = "Processes .class files with Lightsaber Processor."
      task.backupDir = backupDir
      task.classesDir = classesDir
      task.sourceDir = sourceDir
      task.classpath = classpath
      task.bootClasspath = bootClasspath
    }
  }

  private fun createBackupClassFilesTask(taskName: String, classesDir: File, backupDir: File): BackupClassesTask {
    return project.tasks.create(taskName, BackupClassesTask::class.java) { task ->
      task.description = "Back up original .class files."
      task.classesDir = classesDir
      task.backupDir = backupDir
    }
  }
}
