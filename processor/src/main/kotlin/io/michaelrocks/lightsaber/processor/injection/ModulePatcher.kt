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

package io.michaelrocks.lightsaber.processor.injection

import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.commons.toFieldDescriptor
import io.michaelrocks.lightsaber.processor.commons.toMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import java.util.*

class ModulePatcher(
    processorContext: ProcessorContext,
    classVisitor: ClassVisitor,
    module: Module
) : BaseInjectionClassVisitor(processorContext, classVisitor) {
  private val providableFields: MutableSet<FieldDescriptor>
  private val providableMethods: MutableSet<MethodDescriptor>

  init {
    providableFields = HashSet<FieldDescriptor>(module.providers.size)
    providableMethods = HashSet<MethodDescriptor>(module.providers.size)
    for (provider in module.providers) {
      val provisionPoint = provider.provisionPoint
      when (provisionPoint) {
        is ProvisionPoint.Field -> providableFields.add(provisionPoint.field.toFieldDescriptor())
        is ProvisionPoint.AbstractMethod -> providableMethods.add(provisionPoint.method.toMethodDescriptor())
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
    val field = FieldDescriptor(name, desc)
    if (providableFields.contains(field)) {
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
