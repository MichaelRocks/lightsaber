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

package io.michaelrocks.lightsaber.processor.commons

import org.objectweb.asm.Opcodes

object AccessFlagStringifier {
  fun classAccessFlagToString(accessFlag: Int): String {
    when (accessFlag) {
      Opcodes.ACC_PUBLIC -> return "public"
      Opcodes.ACC_PRIVATE -> return "private"
      Opcodes.ACC_PROTECTED -> return "protected"
      Opcodes.ACC_FINAL -> return "final"
      Opcodes.ACC_SUPER -> return "super"
      Opcodes.ACC_INTERFACE -> return "interface"
      Opcodes.ACC_ABSTRACT -> return "abstract"
      Opcodes.ACC_SYNTHETIC -> return "synthetic"
      Opcodes.ACC_ANNOTATION -> return "annotation"
      Opcodes.ACC_ENUM -> return "enum"
      else -> throw IllegalArgumentException("Unknown class access flag: ${Integer.toHexString(accessFlag)}")
    }
  }

  fun methodAccessFlagToString(accessFlag: Int): String {
    when (accessFlag) {
      Opcodes.ACC_PUBLIC -> return "public"
      Opcodes.ACC_PRIVATE -> return "private"
      Opcodes.ACC_PROTECTED -> return "protected"
      Opcodes.ACC_STATIC -> return "static"
      Opcodes.ACC_FINAL -> return "final"
      Opcodes.ACC_SYNCHRONIZED -> return "synchronized"
      Opcodes.ACC_BRIDGE -> return "bridge"
      Opcodes.ACC_VARARGS -> return "varargs"
      Opcodes.ACC_NATIVE -> return "native"
      Opcodes.ACC_ABSTRACT -> return "abstract"
      Opcodes.ACC_STRICT -> return "strict"
      Opcodes.ACC_SYNTHETIC -> return "synthetic"
      else -> throw IllegalArgumentException("Unknown method access flag: ${Integer.toHexString(accessFlag)}")
    }
  }

  fun fieldAccessFlagToString(accessFlag: Int): String {
    when (accessFlag) {
      Opcodes.ACC_PUBLIC -> return "public"
      Opcodes.ACC_PRIVATE -> return "private"
      Opcodes.ACC_PROTECTED -> return "protected"
      Opcodes.ACC_STATIC -> return "static"
      Opcodes.ACC_FINAL -> return "final"
      Opcodes.ACC_VOLATILE -> return "volatile"
      Opcodes.ACC_TRANSIENT -> return "transient"
      Opcodes.ACC_SYNTHETIC -> return "synthetic"
      else -> throw IllegalArgumentException("Unknown field access flag: ${Integer.toHexString(accessFlag)}")
    }
  }
}
