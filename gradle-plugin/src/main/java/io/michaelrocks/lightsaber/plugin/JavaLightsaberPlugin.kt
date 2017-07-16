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
import org.gradle.api.tasks.SourceSetOutput
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
    val lightsaberDir = File(project.buildDir, getLightsaberRelativePath(nameSuffix))
    val classesDirs = getClassesDirs(sourceSet.output)
    val backupDirs = getBackupDirs(project.buildDir, lightsaberDir, classesDirs)
    val sourceDir = File(lightsaberDir, "src")
    val classpath = compileTask.classpath.toList()
    val bootClasspathString = compileTask.options.bootClasspath ?: System.getProperty("sun.boot.class.path")
    val bootClasspath = bootClasspathString?.split(File.pathSeparator)?.map { File(it) } ?: emptyList()
    val lightsaberTask =
        createLightsaberProcessTask(
            "lightsaberProcess$suffix",
            classesDirs,
            backupDirs,
            sourceDir,
            classpath,
            bootClasspath
        )
    val backupTask =
        createBackupClassFilesTask("lightsaberBackupClasses$suffix", classesDirs, backupDirs)
    configureTasks(lightsaberTask, backupTask, compileTask)
  }

  private fun getLightsaberRelativePath(suffix: String): String {
    return if (suffix.isEmpty()) LIGHTSABER_PATH else LIGHTSABER_PATH + File.separatorChar + suffix
  }

  private fun getClassesDirs(output: SourceSetOutput): List<File> {
    val version = GradleVersion.parse(project.gradle.gradleVersion)
    if (version.isAtLeast(4,0, 0)) {
      return output.classesDirs.files.toList()
    } else {
      @Suppress("DEPRECATION")
      return listOf(output.classesDir)
    }
  }

  private fun getBackupDirs(buildDir: File, lightsaberDir: File, classesDirs: List<File>): List<File> {
    return classesDirs.map { classesDir ->
      val relativeFile = classesDir.relativeToOrSelf(buildDir)
      // XXX: What if relativeFile is rooted? Maybe we need to remove the root part from it.
      File(lightsaberDir, relativeFile.path)
    }
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
      classesDirs: List<File>,
      backupDirs: List<File>,
      sourceDir: File,
      classpath: List<File>,
      bootClasspath: List<File>
  ): LightsaberTask {
    logger.info("Creating Lightsaber task {}...", taskName)
    logger.info("  Source classes directories: {}", backupDirs)
    logger.info("  Processed classes directories: {}", classesDirs)

    return project.tasks.create(taskName, LightsaberTask::class.java) { task ->
      task.description = "Processes .class files with Lightsaber Processor."
      task.backupDirs = backupDirs
      task.classesDirs = classesDirs
      task.sourceDir = sourceDir
      task.classpath = classpath
      task.bootClasspath = bootClasspath
    }
  }

  private fun createBackupClassFilesTask(
      taskName: String,
      classesDirs: List<File>,
      backupDirs: List<File>
  ): BackupClassesTask {
    return project.tasks.create(taskName, BackupClassesTask::class.java) { task ->
      task.description = "Back up original .class files."
      task.classesDirs = classesDirs
      task.backupDirs = backupDirs
    }
  }

  companion object {
    private const val LIGHTSABER_PATH = "lightsaber"
  }
}
