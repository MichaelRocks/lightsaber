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

import com.android.build.api.transform.*
import io.michaelrocks.lightsaber.processor.LightsaberParameters
import io.michaelrocks.lightsaber.processor.LightsaberProcessor
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

public class LightsaberTransform extends Transform {
  private final Project project
  private final Logger logger = Logging.getLogger(LightsaberTransform)

  LightsaberTransform(final Project project) {
    this.project = project
  }

  @Override
  void transform(final TransformInvocation invocation) throws IOException, TransformException, InterruptedException {
    final LightsaberParameters parameters = new LightsaberParameters()
    final List<QualifiedContent> inputs = invocation.inputs.collectMany {
      final List<QualifiedContent> content = new ArrayList<>(it.jarInputs.size() + it.directoryInputs.size())
      content.addAll(it.jarInputs)
      content.addAll(it.directoryInputs)
      content
    }
    final List<File> outputs = inputs.collect { input ->
      final Format format = input instanceof JarInput ? Format.JAR : Format.DIRECTORY
      invocation.outputProvider.getContentLocation(
          input.name, EnumSet.of(QualifiedContent.DefaultContentType.CLASSES),
          EnumSet.of(QualifiedContent.Scope.PROJECT), format)
    }

    // For now just skip tests.
    if (invocation.context.path.endsWith("Test")) {
      logger.info("Found a test project. Skipping...")
      FileMethods.copyDirectoryTo(input, output, true)
      return
    }

    parameters.inputs = inputs.collect { it.file }
    parameters.outputs = outputs
    parameters.source = new File(invocation.context.temporaryDir, "src")
    parameters.gen = invocation.outputProvider.getContentLocation(
        "gen-lightsaber", EnumSet.of(QualifiedContent.DefaultContentType.CLASSES),
        EnumSet.of(QualifiedContent.Scope.PROJECT), Format.DIRECTORY)
    parameters.classpath = invocation.referencedInputs.collectMany {
      it.directoryInputs.collect { it.file } + it.jarInputs.collect { it.file }
    }
    parameters.bootClasspath = project.android.bootClasspath.toList()
    parameters.debug = logger.isDebugEnabled()
    parameters.info = logger.isInfoEnabled()
    logger.info("Starting Lightsaber processor: $parameters")
    final LightsaberProcessor processor = new LightsaberProcessor(parameters)
    try {
      processor.process()
      logger.info("Lightsaber finished processing")
    } catch (final IOException exception) {
      logger.error("Lightsaber failed", exception)
      throw exception
    } catch (final Exception exception) {
      logger.error("Lightsaber failed", exception)
      throw new TransformException(exception)
    }
  }

  @Override
  String getName() {
    return "lightsaber"
  }

  @Override
  Set<QualifiedContent.ContentType> getInputTypes() {
    return EnumSet.of(QualifiedContent.DefaultContentType.CLASSES)
  }

  @Override
  Set<QualifiedContent.Scope> getScopes() {
    return EnumSet.of(QualifiedContent.Scope.PROJECT)
  }

  @Override
  Set<QualifiedContent.Scope> getReferencedScopes() {
    return EnumSet.of(
        QualifiedContent.Scope.PROJECT_LOCAL_DEPS,
        QualifiedContent.Scope.SUB_PROJECTS,
        QualifiedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
        QualifiedContent.Scope.EXTERNAL_LIBRARIES,
        QualifiedContent.Scope.TESTED_CODE,
        QualifiedContent.Scope.PROVIDED_ONLY
    )
  }

  @Override
  boolean isIncremental() {
    return false
  }
}
