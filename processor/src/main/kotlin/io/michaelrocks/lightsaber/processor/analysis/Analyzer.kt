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

package io.michaelrocks.lightsaber.processor.analysis

import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.commons.CompositeClassVisitor
import io.michaelrocks.lightsaber.processor.commons.using
import io.michaelrocks.lightsaber.processor.io.FileSource
import io.michaelrocks.lightsaber.processor.io.fileSource
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import java.io.File
import java.io.IOException

class Analyzer(private val processorContext: ProcessorContext) {
  @Throws(IOException::class)
  fun analyze(fileSource: FileSource, libraries: List<File>) {
    analyzeTypes(fileSource, libraries)
    analyzeInjectionTargets(fileSource)
  }

  @Throws(IOException::class)
  private fun analyzeTypes(fileSource: FileSource, libraries: List<File>) {
    val compositeClassVisitor = CompositeClassVisitor()
    compositeClassVisitor.addVisitor(AnnotationAnalysisDispatcher(processorContext))
    analyzeLibraries(libraries, compositeClassVisitor)
    analyzeClasses(fileSource, compositeClassVisitor)
  }

  @Throws(IOException::class)
  private fun analyzeInjectionTargets(fileSource: FileSource) {
    val compositeClassVisitor = CompositeClassVisitor()
    compositeClassVisitor.addVisitor(ModuleClassAnalyzer(processorContext))
    compositeClassVisitor.addVisitor(InjectionTargetAnalyzer(processorContext))
    analyzeClasses(fileSource, compositeClassVisitor)
  }

  @Throws(IOException::class)
  private fun analyzeLibraries(libraries: List<File>, classVisitor: ClassVisitor) {
    for (library in libraries) {
      if (!library.exists()) {
        continue
      }

      using(library.fileSource()) { fileSource ->
        analyzeClasses(fileSource, classVisitor)
      }
    }
  }

  @Throws(IOException::class)
  private fun analyzeClasses(fileSource: FileSource, classVisitor: ClassVisitor) {
    fileSource.listFiles { path, type ->
      if (type == FileSource.EntryType.CLASS) {
        processorContext.classFilePath = path
        try {
          val classReader = ClassReader(fileSource.readFile(path))
          classReader.accept(classVisitor, ClassReader.SKIP_FRAMES or ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG)
        } finally {
          processorContext.classFilePath = null
        }
      }
    }
  }
}
