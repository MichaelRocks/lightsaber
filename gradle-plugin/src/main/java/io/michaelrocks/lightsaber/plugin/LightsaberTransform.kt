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

import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import io.michaelrocks.lightsaber.processor.LightsaberParameters
import io.michaelrocks.lightsaber.processor.LightsaberProcessor
import io.michaelrocks.lightsaber.processor.logging.getLogger
import org.gradle.api.Project
import java.io.File
import java.io.IOException
import java.util.EnumSet

class LightsaberTransform(private val project: Project) : Transform() {
  private val logger = getLogger()

  override fun transform(invocation: TransformInvocation) {
    val inputs = invocation.inputs.flatMap { it.jarInputs + it.directoryInputs }
    val outputs = inputs.map { input ->
      val format = if (input is JarInput) Format.JAR else Format.DIRECTORY
      invocation.outputProvider.getContentLocation(
          input.name,
          input.contentTypes,
          input.scopes,
          format
      )
    }

    // For now just skip tests.
    if (invocation.context.path.endsWith("Test")) {
      logger.info("Found a test project. Skipping...")
      inputs.zip(outputs) { input, output ->
        input.file.copyRecursively(output, true)
      }
      return
    }

    val parameters = LightsaberParameters(
        inputs = inputs.map { it.file },
        outputs = outputs,
        gen = invocation.outputProvider.getContentLocation(
            "gen-lightsaber",
            QualifiedContent.DefaultContentType.CLASSES,
            QualifiedContent.Scope.PROJECT,
            Format.DIRECTORY
        ),
        classpath = invocation.referencedInputs.flatMap {
          it.jarInputs.map { it.file } + it.directoryInputs.map { it.file }
        },
        bootClasspath = project.android.bootClasspath,
        debug = logger.isDebugEnabled,
        info = logger.isInfoEnabled
    )
    logger.info("Starting Lightsaber processor: {}", parameters)
    val processor = LightsaberProcessor(parameters)
    try {
      processor.process()
      logger.info("Lightsaber finished processing")
    } catch (exception: IOException) {
      logger.error("Lightsaber failed", exception)
      throw exception
    } catch (exception: Exception) {
      logger.error("Lightsaber failed", exception)
      throw TransformException(exception)
    }
  }

  override fun getName(): String {
    return "lightsaber"
  }

  override fun getInputTypes(): Set<QualifiedContent.ContentType> {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
    return EnumSet.of(
        QualifiedContent.Scope.PROJECT,
        QualifiedContent.Scope.SUB_PROJECTS
    )
  }

  override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
    if (PluginVersion.major >= 3) {
      return EnumSet.of(
          QualifiedContent.Scope.PROJECT,
          QualifiedContent.Scope.SUB_PROJECTS,
          QualifiedContent.Scope.EXTERNAL_LIBRARIES,
          QualifiedContent.Scope.TESTED_CODE,
          QualifiedContent.Scope.PROVIDED_ONLY
      )
    } else {
      @Suppress("DEPRECATION")
      return EnumSet.of(
          QualifiedContent.Scope.PROJECT,
          QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
          QualifiedContent.Scope.SUB_PROJECTS,
          QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
          QualifiedContent.Scope.TESTED_CODE,
          QualifiedContent.Scope.PROVIDED_ONLY
      )
    }
  }

  override fun isIncremental(): Boolean {
    return false
  }

  private fun TransformOutputProvider.getContentLocation(
      name: String,
      contentType: QualifiedContent.ContentType,
      scope: QualifiedContent.Scope,
      format: Format
  ): File {
    return getContentLocation(name, setOf(contentType), EnumSet.of(scope), format)
  }
}
