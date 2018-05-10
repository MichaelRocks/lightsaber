/*
 * Copyright 2018 Michael Rozumyanskiy
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
import org.gradle.api.plugins.JavaPlugin

class AndroidLightsaberPlugin : BaseLightsaberPlugin() {
  override fun apply(project: Project) {
    super.apply(project)

    if (project.hasAndroid) {
      addDependencies(getConfigurationName())
      project.android.registerTransform(LightsaberTransform(project))
    } else {
      throw GradleException("Lightsaber plugin must be applied *AFTER* Android plugin")
    }
  }

  private fun getConfigurationName(): String {
    return if (PluginVersion.major >= 3) {
      JavaPlugin.IMPLEMENTATION_CONFIGURATION_NAME
    } else {
      @Suppress("DEPRECATION")
      JavaPlugin.COMPILE_CONFIGURATION_NAME
    }
  }
}
