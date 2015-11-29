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
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.name
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import java.util.*

class ModulePatcher(
    processorContext: ProcessorContext,
    classVisitor: ClassVisitor,
    module: ModuleDescriptor
) : BaseInjectionClassVisitor(processorContext, classVisitor) {
  private val providableFields: MutableSet<String>
  private val providableMethods: MutableSet<MethodDescriptor>

  init {
    providableFields = HashSet<String>(module.providers.size)
    providableMethods = HashSet<MethodDescriptor>(module.providers.size)
    for (provider in module.providers) {
      if (provider.providerField != null) {
        providableFields.add(provider.providerField.name)
      }
      if (provider.providerMethod != null) {
        providableMethods.add(provider.providerMethod.method)
      }
    }
  }

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?,
      interfaces: Array<String>?) {
    val newAccess = (access and (ACC_PRIVATE or ACC_PROTECTED).inv()) or ACC_PUBLIC
    super.visit(version, newAccess, name, signature, superName, interfaces)
    isDirty = isDirty or (newAccess != access)
  }

  override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor? {
    if (providableFields.contains(name)) {
      val newAccess = access and ACC_PRIVATE.inv()
      isDirty = isDirty or (newAccess != access)
      return super.visitField(newAccess, name, desc, signature, value)
    } else {
      return super.visitField(access, name, desc, signature, value)
    }
  }

  override fun visitMethod(access: Int, name: String, desc: String, signature: String?,
      exceptions: Array<String>?): MethodVisitor? {
    val method = MethodDescriptor(name, desc)
    if (providableMethods.contains(method)) {
      val newAccess = access and ACC_PRIVATE.inv()
      isDirty = isDirty or (newAccess != access)
      return super.visitMethod(newAccess, name, desc, signature, exceptions)
    } else {
      return super.visitMethod(access, name, desc, signature, exceptions)
    }
  }
}
