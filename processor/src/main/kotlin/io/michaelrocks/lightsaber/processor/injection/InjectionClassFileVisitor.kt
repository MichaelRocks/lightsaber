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

package io.michaelrocks.lightsaber.processor.injection

import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.io.ClassFileVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.IOException

class InjectionClassFileVisitor(
    classFileVisitor: ClassFileVisitor,
    private val processorContext: ProcessorContext
) : ClassFileVisitor(classFileVisitor) {

  @Throws(IOException::class)
  override fun visitClassFile(path: String, classData: ByteArray) {
    val classReader = ClassReader(classData)
    val classWriter = StandaloneClassWriter(
        classReader, ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, processorContext.typeGraph)
    val classVisitor = InjectionDispatcher(classWriter, processorContext)
    classReader.accept(classVisitor, ClassReader.SKIP_FRAMES)
    super.visitClassFile(path, classWriter.toByteArray())
  }
}
