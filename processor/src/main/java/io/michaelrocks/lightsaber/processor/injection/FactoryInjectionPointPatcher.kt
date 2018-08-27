/*
 * Copyright 2018 Michael Rozumyanskiy
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

import io.michaelrocks.lightsaber.processor.model.FactoryInjectionPoint
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PROTECTED
import org.objectweb.asm.Opcodes.ACC_PUBLIC

class FactoryInjectionPointPatcher(
    classVisitor: ClassVisitor,
    private val factoryInjectionPoint: FactoryInjectionPoint
) : BaseInjectionClassVisitor(classVisitor) {

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?,
      interfaces: Array<String>?) {
    val newAccess = (access and (ACC_PRIVATE or ACC_PROTECTED).inv()) or ACC_PUBLIC
    super.visit(version, access, name, signature, superName, interfaces)
    if (newAccess != access) {
      isDirty = true
    }
  }

  override fun visitMethod(access: Int, name: String, desc: String, signature: String?,
      exceptions: Array<String>?): MethodVisitor? {
    return if (shouldMethodBePatched(name, desc)) {
      val newAccess = access and ACC_PRIVATE.inv()
      if (newAccess != access) {
        isDirty = true
      }
      super.visitMethod(newAccess, name, desc, signature, exceptions)
    } else {
      super.visitMethod(access, name, desc, signature, exceptions)
    }
  }

  private fun shouldMethodBePatched(name: String, descriptor: String): Boolean {
    val method = factoryInjectionPoint.method
    return name == method.name && descriptor == method.type.descriptor
  }
}
