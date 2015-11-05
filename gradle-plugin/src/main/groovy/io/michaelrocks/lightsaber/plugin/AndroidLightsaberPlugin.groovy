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
import com.android.build.transform.api.Transform
import org.gradle.api.GradleException
import org.gradle.api.Project

class AndroidLightsaberPlugin extends BaseLightsaberPlugin {
    @Override
    void apply(final Project project) {
        super.apply(project)

        if (project.hasProperty('android')) {
            addDependencies('compile')
            if (isTransformAvailable()) {
                project.android.registerTransform(new LightsaberTransform())
            } else {
                project.afterEvaluate {
                    if (project.plugins.hasPlugin('com.android.application')) {
                        setupLightsaberForLegacyAndroid(project.android.applicationVariants)
                    } else if (project.plugins.hasPlugin('com.android.library')) {
                        setupLightsaberForLegacyAndroid(project.android.libraryVariants)
                    } else {
                        throw new GradleException("Only application and library Android projects are supported")
                    }
                }
            }
        } else {
            throw new GradleException("Lightsaber plugin must be applied *AFTER* Android plugin")
        }
    }

    private boolean isTransformAvailable() {
        try {
            Class.forName("com.android.build.transform.api.Transform")
            return project.android.respondsTo('registerTransform', Transform, Object[])
        } catch (final ClassNotFoundException ignored) {
            return false
        }
    }

    private void setupLightsaberForLegacyAndroid(final Collection<BaseVariant> variants) {
        logger.info("Setting up Lightsaber task for Android project ${project.name}...")
        variants.all { final BaseVariant variant ->
            logger.trace("Creating Lightsaber tasks for variant ${variant.name}")
            final String variantName = variant.name.capitalize()
            final File classesDir = variant.javaCompiler.destinationDir
            final File backupDir = new File(project.buildDir, "lightsaber/${variant.name}")
            final List<File> classpath =
                    project.android.bootClasspath.toList() + variant.javaCompiler.classpath.toList()
            createTasks(classesDir, backupDir, classpath, variant.javaCompiler, variantName)
        }
    }
}
