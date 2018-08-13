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

package io.michaelrocks.lightsaber.processor.commons

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

fun ClassVisitor.newMethod(access: Int, method: MethodDescriptor, body: GeneratorAdapter.() -> Unit) {
  GeneratorAdapter(this, access, method).apply {
    visitCode()
    body()
    returnValue()
    endMethod()
  }
}

fun ClassVisitor.newDefaultConstructor(access: Int = Opcodes.ACC_PUBLIC, superType: Type.Object = Types.OBJECT_TYPE) {
  newMethod(access, MethodDescriptor.forDefaultConstructor()) {
    loadThis()
    invokeConstructor(superType, MethodDescriptor.forDefaultConstructor())
  }
}
