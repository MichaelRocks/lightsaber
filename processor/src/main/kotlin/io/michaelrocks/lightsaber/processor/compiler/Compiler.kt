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

package io.michaelrocks.lightsaber.processor.compiler

import io.michaelrocks.lightsaber.processor.ErrorReporter
import java.io.File
import javax.tools.*

interface Compiler {
  fun compile(sourcePath: File, outputPath: File)
}

class JavaToolsCompiler(
    private val classpath: List<File>,
    private val bootClasspath: List<File>,
    private val errorReporter: ErrorReporter
) : Compiler {
  override fun compile(sourcePath: File, outputPath: File) {
    val javaCompiler = ToolProvider.getSystemJavaCompiler()
    val diagnosticsListener = DiagnosticCollector<JavaFileObject>()
    val fileManager = javaCompiler.getStandardFileManager(diagnosticsListener, null, null)
    fileManager.setLocation(StandardLocation.SOURCE_PATH, listOf(sourcePath))
    fileManager.setLocation(StandardLocation.CLASS_OUTPUT, listOf(outputPath))
    fileManager.setLocation(StandardLocation.CLASS_PATH, classpath + listOf(outputPath))
    fileManager.setLocation(StandardLocation.PLATFORM_CLASS_PATH, bootClasspath)
    val compilationUnits = fileManager.getCompilationUnits(sourcePath)
    val task = javaCompiler.getTask(newLoggerWriter(), fileManager, diagnosticsListener, null, null, compilationUnits)
    try {
      if (!task.call()) {
        errorReporter.reportError(diagnosticsListener.toErrorMessage())
      }
    } catch (exception: Exception) {
      errorReporter.reportError(exception)
    }
  }

  fun StandardJavaFileManager.getCompilationUnits(sourcePath: File): List<JavaFileObject> {
    val javaFiles = sourcePath.walk()
        .filter { it.isFile && it.name.endsWith(".java") }
        .asIterable()
    return getJavaFileObjectsFromFiles(javaFiles).toList()
  }

  fun DiagnosticCollector<JavaFileObject>.toErrorMessage(): String {
    return diagnostics
        .map { "${it.source.name}:${it.lineNumber}:${it.columnNumber}: ${it.getMessage(null)}" }
        .joinToString("\n")
  }
}
