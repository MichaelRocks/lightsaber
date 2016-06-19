/*
 * Copyright 2016 Michael Rozumyanskiy
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

import groovy.transform.CompileStatic
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger

@CompileStatic
class BaseLightsaberPlugin implements Plugin<Project> {
  Project project
  Logger logger

  @Override
  void apply(final Project project) {
    this.project = project
    this.logger = project.logger
  }

  void addDependencies(final String configurationName) {
    final String version = Build.VERSION
    project.dependencies.add(configurationName, "io.michaelrocks:lightsaber-core:$version")
  }
}
