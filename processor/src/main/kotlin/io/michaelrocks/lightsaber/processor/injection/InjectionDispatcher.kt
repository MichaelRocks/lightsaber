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

import io.michaelrocks.lightsaber.processor.model.InjectionContext
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class InjectionDispatcher(
    classVisitor: ClassVisitor,
    private val context: InjectionContext
) : ClassVisitor(Opcodes.ASM5, classVisitor) {

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?,
      interfaces: Array<String>?) {
    val type = Type.getObjectType(name)

    context.findModuleByType(type)?.let {
      cv = ModulePatcher(cv, it)
    }

    context.findInjectableTargetByType(type)?.let {
      cv = InjectableTargetPatcher(cv, it)
    }

    context.findProvidableTargetByType(type)?.let {
      cv = ProvidableTargetPatcher(cv, it)
    }

    super.visit(version, access, name, signature, superName, interfaces)
  }
}
