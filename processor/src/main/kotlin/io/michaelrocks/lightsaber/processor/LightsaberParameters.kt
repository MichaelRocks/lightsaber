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

package io.michaelrocks.lightsaber.processor

import ch.qos.logback.classic.Level
import com.beust.jcommander.Parameter
import com.beust.jcommander.converters.FileConverter
import java.io.File

data class LightsaberParameters(
    @Parameter(names = arrayOf("--inputs"), description = "Jar files and/or class directories to process")
    var inputs: List<File> = emptyList(),
    @Parameter(
        names = arrayOf("--outputs"),
        description = "Output jar files and/or class directories matching inputs"
    )
    var outputs: List<File> = emptyList(),
    @Parameter(
        names = arrayOf("--classpath"),
        listConverter = FileConverter::class, description = "Classpath",
        variableArity = true
    )
    var classpath: List<File> = emptyList(),
    @Parameter(
        names = arrayOf("--bootclasspath"),
        listConverter = FileConverter::class, description = "Boot classpath",
        variableArity = true
    )
    var bootClasspath: List<File> = emptyList(),
    @Parameter(names = arrayOf("--source"), description = "Output directory for .java files")
    var source: File? = null,
    @Parameter(names = arrayOf("--gen"), description = "Output directory for generated .class files")
    var gen: File? = null,
    @Parameter(names = arrayOf("-i", "--info"), description = "Enable detailed logging")
    var info: Boolean = false,
    @Parameter(names = arrayOf("-d", "--debug"), description = "Enable verbose logging")
    var debug: Boolean = false,
    @Parameter(names = arrayOf("--stacktrace"), description = "Print stack traces")
    var printStacktrace: Boolean = false
) {
  val loggingLevel: Level
    get() {
      if (debug) {
        return Level.DEBUG
      } else if (info) {
        return Level.INFO
      } else {
        return Level.WARN
      }
    }
}
