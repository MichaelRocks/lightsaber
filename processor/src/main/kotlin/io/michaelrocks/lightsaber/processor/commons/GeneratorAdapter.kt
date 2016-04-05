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

package io.michaelrocks.lightsaber.processor.commons

import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.descriptor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.util.*

class GeneratorAdapter(
    methodVisitor: MethodVisitor,
    access: Int,
    name: String,
    desc: String
) : org.objectweb.asm.commons.GeneratorAdapter(ASM5, methodVisitor, access, name, desc) {

  companion object {
    private fun visitMethod(
        classVisitor: ClassVisitor,
        access: Int,
        method: MethodDescriptor,
        signature: String?,
        exceptions: Array<Type>?
    ): MethodVisitor =
        classVisitor.visitMethod(access, method.name, method.descriptor, signature, exceptions?.toInternalNames())

    private fun Array<Type>.toInternalNames(): Array<String>? =
        this.mapToArray { it.internalName }
  }

  constructor(
      methodVisitor: MethodVisitor, access: Int, method: MethodDescriptor
  ) : this(methodVisitor, access, method.name, method.descriptor)

  constructor(
      classVisitor: ClassVisitor, access: Int, method: MethodDescriptor
  ) : this(visitMethod(classVisitor, access, method, null, null), access, method)

  constructor(
      classVisitor: ClassVisitor, access: Int, method: MethodDescriptor, signature: String?, exceptions: Array<Type>?
  ) : this(visitMethod(classVisitor, access, method, signature, exceptions), access, method)

  fun newArray(type: Type, size: Int) {
    push(size)
    super.newArray(type)
  }

  fun invokeVirtual(owner: Type, method: MethodDescriptor) {
    invoke(INVOKEVIRTUAL, owner, method, false)
  }

  fun invokeConstructor(type: Type, method: MethodDescriptor) {
    invoke(INVOKESPECIAL, type, method, false)
  }

  fun invokeStatic(owner: Type, method: MethodDescriptor) {
    invoke(INVOKESTATIC, owner, method, false)
  }

  fun invokeInterface(owner: Type, method: MethodDescriptor) {
    invoke(INVOKEINTERFACE, owner, method, true)
  }

  private fun invoke(opcode: Int, type: Type, method: MethodDescriptor, ownerIsInterface: Boolean) {
    val owner = if (type.sort == Type.ARRAY) type.descriptor else type.internalName
    visitMethodInsn(opcode, owner, method.name, method.descriptor, ownerIsInterface)
  }

  fun getField(owner: Type, field: FieldDescriptor) {
    getField(owner, field.name, field.type)
  }

  fun putField(owner: Type, field: FieldDescriptor) {
    putField(owner, field.name, field.type)
  }

  fun getStatic(owner: Type, field: FieldDescriptor) {
    getStatic(owner, field.name, field.type)
  }

  fun putStatic(owner: Type, field: FieldDescriptor) {
    putStatic(owner, field.name, field.type)
  }

  fun pushNull() {
    visitInsn(ACONST_NULL)
  }

  fun visitFrame(type: Int, nLocal: Int, local: Array<Type>?, nStack: Int, stack: Array<Type>?) {
    val localObjects = local?.toFrameObjectArray()
    val stackObjects = stack?.toFrameObjectArray()
    visitFrame(type, nLocal, localObjects, nStack, stackObjects)
  }

  private fun Array<Type>.toFrameObjectArray(): Array<Any>? {
    val objects = ArrayList<Any>(size * 2)
    forEach { type ->
      when (type.sort) {
        Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT -> objects.add(INTEGER)
        Type.FLOAT -> objects.add(FLOAT)
        Type.LONG -> {
          objects.add(LONG)
          objects.add(TOP)
        }
        Type.DOUBLE -> {
          objects.add(DOUBLE)
          objects.add(TOP)
        }
        Type.ARRAY -> objects.add(type.descriptor)
        Type.OBJECT -> objects.add(type.internalName)
        else -> throw IllegalArgumentException("Illegal type used in frame: " + type)
      }
    }
    return objects.toArray(arrayOfNulls<Any>(objects.size))
  }
}