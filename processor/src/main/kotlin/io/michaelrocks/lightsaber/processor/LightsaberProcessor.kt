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

package io.michaelrocks.lightsaber.processor

import io.michaelrocks.lightsaber.processor.logging.getLogger

class LightsaberProcessor(private val parameters: LightsaberParameters) {
  private val logger = getLogger()

  @Throws(Exception::class)
  fun process() {
    val inputs = parameters.inputs
    val outputs = parameters.outputs
    val genPath = parameters.gen!!
    val classpath = parameters.classpath
    val bootClasspath = parameters.bootClasspath
    ClassProcessor(inputs, outputs, genPath, classpath, bootClasspath).apply {
      processClasses()
      close()
    }

    logger.info("DONE")
  }
}
