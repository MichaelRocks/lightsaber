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
import io.michaelrocks.lightsaber.processor.graph.TypeGraph
import io.michaelrocks.lightsaber.processor.io.ClassFileReader
import io.michaelrocks.lightsaber.processor.io.ClassFileVisitor
import io.michaelrocks.lightsaber.processor.io.DirectoryClassFileTraverser
import io.michaelrocks.lightsaber.processor.io.JarClassFileTraverser
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor

import java.io.File
import java.io.IOException

class Analyzer(private val processorContext: ProcessorContext) {
  private val typeGraphBuilder = TypeGraph.Builder()

  @Throws(IOException::class)
  fun analyze(classFileReader: ClassFileReader, libraries: List<File>) {
    analyzeTypes(classFileReader, libraries)
    analyzeInjectionTargets(classFileReader)
  }

  @Throws(IOException::class)
  private fun analyzeTypes(classFileReader: ClassFileReader, libraries: List<File>) {
    val compositeClassVisitor = CompositeClassVisitor()
    compositeClassVisitor.addVisitor(TypeGraphComposer(typeGraphBuilder))
    compositeClassVisitor.addVisitor(AnnotationAnalysisDispatcher(processorContext))
    analyzeLibraries(libraries, compositeClassVisitor)
    analyzeClassesFromReader(classFileReader, compositeClassVisitor)
    processorContext.typeGraph = typeGraphBuilder.build()
  }

  @Throws(IOException::class)
  private fun analyzeInjectionTargets(classFileReader: ClassFileReader) {
    val compositeClassVisitor = CompositeClassVisitor()
    compositeClassVisitor.addVisitor(ModuleClassAnalyzer(processorContext))
    compositeClassVisitor.addVisitor(InjectionTargetAnalyzer(processorContext))
    analyzeClassesFromReader(classFileReader, compositeClassVisitor)
  }

  @Throws(IOException::class)
  private fun analyzeLibraries(libraries: List<File>, classVisitor: ClassVisitor) {
    for (library in libraries) {
      if (!library.exists()) {
        continue
      }

      if (library.isDirectory) {
        analyzeClassesFromDirectory(library, classVisitor)
      } else {
        analyzeClassesFromJar(library, classVisitor)
      }
    }
  }

  @Throws(IOException::class)
  private fun analyzeClassesFromJar(jarFile: File, classVisitor: ClassVisitor) {
    try {
      using(ClassFileReader(JarClassFileTraverser(jarFile))) { classFileReader ->
        analyzeClassesFromReader(classFileReader, classVisitor)
      }
    } catch (exception: Exception) {
      throw IOException(exception)
    }
  }

  @Throws(IOException::class)
  private fun analyzeClassesFromDirectory(classesDir: File, classVisitor: ClassVisitor) {
    try {
      using(ClassFileReader(DirectoryClassFileTraverser(classesDir))) { classFileReader ->
        analyzeClassesFromReader(classFileReader, classVisitor)
      }
    } catch (exception: Exception) {
      throw IOException(exception)
    }

  }

  @Throws(IOException::class)
  private fun analyzeClassesFromReader(classFileReader: ClassFileReader, classVisitor: ClassVisitor) {
    classFileReader.accept(object : ClassFileVisitor(null) {
      @Throws(IOException::class)
      override fun visitClassFile(path: String, classData: ByteArray) {
        processorContext.classFilePath = path
        try {
          val classReader = ClassReader(classData)
          classReader.accept(classVisitor, ClassReader.SKIP_FRAMES or ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG)
          super.visitClassFile(path, classData)
        } finally {
          processorContext.classFilePath = null
        }
      }
    })
  }
}
