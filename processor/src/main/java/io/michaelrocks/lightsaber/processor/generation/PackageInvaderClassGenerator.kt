/*
 * Copyright 2019 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.processor.generation

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.boxed
import io.michaelrocks.lightsaber.processor.commons.newDefaultConstructor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.descriptor
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_STATIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6

class PackageInvaderClassGenerator(
  private val classRegistry: ClassRegistry,
  private val packageInvader: PackageInvader
) {

  fun generate(): ByteArray {
    val classWriter = StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
      V1_6,
      ACC_PUBLIC or ACC_SUPER,
      packageInvader.type.internalName,
      null,
      Types.OBJECT_TYPE.internalName,
      null
    )

    generateFields(classVisitor)
    generateStaticInitializer(classVisitor)
    classVisitor.newDefaultConstructor()

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateFields(classVisitor: ClassVisitor) {
    for (field in packageInvader.fields.values) {
      val fieldVisitor = classVisitor.visitField(
        ACC_PUBLIC or ACC_STATIC or ACC_FINAL,
        field.name,
        field.descriptor,
        null,
        null
      )
      fieldVisitor.visitEnd()
    }
  }

  private fun generateStaticInitializer(classVisitor: ClassVisitor) {
    val staticInitializer = MethodDescriptor.forStaticInitializer()
    val generator = GeneratorAdapter(classVisitor, ACC_STATIC, staticInitializer)
    generator.visitCode()

    for ((type, field) in packageInvader.fields.entries) {
      generator.push(type.boxed())
      generator.putStatic(packageInvader.type, field)
    }

    generator.returnValue()
    generator.endMethod()
  }
}
