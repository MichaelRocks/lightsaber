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

package io.michaelrocks.lightsaber.processor

import io.michaelrocks.lightsaber.processor.analysis.Analyzer
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.generation.Generator
import io.michaelrocks.lightsaber.processor.graph.CycleSearcher
import io.michaelrocks.lightsaber.processor.graph.DependencyGraph
import io.michaelrocks.lightsaber.processor.graph.UnresolvedDependenciesSearcher
import io.michaelrocks.lightsaber.processor.injection.InjectionDispatcher
import io.michaelrocks.lightsaber.processor.io.FileSource
import io.michaelrocks.lightsaber.processor.validation.SanityChecker
import org.apache.commons.lang3.exception.ExceptionUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File

class ClassProcessor(
    inputFile: File,
    outputFile: File,
    libraries: List<File>
) {
  private val processorContext = ProcessorContext(inputFile, outputFile, libraries)

  private val fileSource = processorContext.fileSourceFactory.createFileSource(inputFile)
  private val fileSink = processorContext.fileSinkFactory.createFileSink(inputFile, outputFile)

  fun processClasses() {
    performAnalysis()
    processorContext.dump()
    validateDependencyGraph()
    performGeneration()
    copyAndPatchClasses()
  }

  private fun performAnalysis() {
    val analyzer = Analyzer(processorContext)
    analyzer.analyze()
    SanityChecker(processorContext).performSanityChecks()
    checkErrors()
  }

  private fun validateDependencyGraph() {
    val dependencyGraph = DependencyGraph(processorContext, processorContext.allModules)

    val unresolvedDependenciesSearcher = UnresolvedDependenciesSearcher(dependencyGraph)
    val unresolvedDependencies = unresolvedDependenciesSearcher.findUnresolvedDependencies()
    for (unresolvedDependency in unresolvedDependencies) {
      processorContext.reportError(
          ProcessingException("Unresolved dependency: " + unresolvedDependency))
    }

    val cycleSearcher = CycleSearcher(dependencyGraph)
    val cycles = cycleSearcher.findCycles()
    for (cycle in cycles) {
      processorContext.reportError(
          ProcessingException("Cycled dependency: " + cycle))
    }

    checkErrors()
  }

  private fun performGeneration() {
    val generator = Generator(processorContext, fileSink)
    generator.generate()
    checkErrors()
  }

  private fun copyAndPatchClasses() {
    fileSource.listFiles { path, type ->
      when (type) {
        FileSource.EntryType.CLASS -> {
          val classReader = ClassReader(fileSource.readFile(path))
          val classWriter = StandaloneClassWriter(
              classReader, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, processorContext.classRegistry)
          val classVisitor = InjectionDispatcher(classWriter, processorContext)
          classReader.accept(classVisitor, ClassReader.SKIP_FRAMES)
          fileSink.createFile(path, classWriter.toByteArray())
        }
        FileSource.EntryType.FILE -> fileSink.createFile(path, fileSource.readFile(path))
        FileSource.EntryType.DIRECTORY -> fileSink.createDirectory(path)
      }
    }

    checkErrors()
  }

  private fun checkErrors() {
    if (processorContext.hasErrors()) {
      throw ProcessingException(composeErrorMessage())
    }
  }

  private fun composeErrorMessage(): String {
    return buildString {
      for (entry in processorContext.errors.entries) {
        val path = entry.key
        for (error in entry.value) {
          append('\n')
          append(path)
          append(": ")
          append(error.message)
          append('\n')
          append(ExceptionUtils.getStackTrace(error))
        }
      }
    }
  }
}
