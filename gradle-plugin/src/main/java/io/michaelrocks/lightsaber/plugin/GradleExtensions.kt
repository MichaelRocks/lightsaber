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

import com.android.build.gradle.BaseExtension
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.compile.JavaCompile

val Project.sourceSets: SourceSetContainer
  get() {
    val javaConvention = convention.getPlugin(JavaPluginConvention::class.java) as JavaPluginConvention
    return javaConvention.sourceSets
  }

val Project.hasAndroid: Boolean
  get() = extensions.findByName("android") is BaseExtension
val Project.android: BaseExtension
  get() = extensions.getByName("android") as BaseExtension

val SourceSetContainer.main: SourceSet
  get() = getByName("main")
val SourceSetContainer.test: SourceSet
  get() = getByName("test")

val TaskContainer.compileJava: JavaCompile
  get() = getByName("compileJava") as JavaCompile
val TaskContainer.compileTestJava: JavaCompile
  get() = getByName("compileTestJava") as JavaCompile

operator fun TaskContainer.get(name: String): Task? {
  return findByName(name)
}