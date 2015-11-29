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

package io.michaelrocks.lightsaber.processor.graph

import io.michaelrocks.lightsaber.processor.descriptors.ClassDescriptor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.Type
import java.util.*

class TypeGraphBuilder : ClassVisitor(ASM5) {
  private val classDescriptors = HashMap<Type, ClassDescriptor>()

  fun build(): TypeGraph {
    return TypeGraph(classDescriptors)
  }

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?,
      interfaces: Array<String>?) {
    super.visit(version, access, name, signature, superName, interfaces)
    val classDescriptor = ClassDescriptor(access, name, superName, interfaces)
    classDescriptors.put(classDescriptor.classType, classDescriptor)
  }
}
