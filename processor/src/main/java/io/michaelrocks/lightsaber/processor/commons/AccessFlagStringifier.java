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

package io.michaelrocks.lightsaber.processor.commons;

import org.objectweb.asm.Opcodes;

public class AccessFlagStringifier {
    public static String classAccessFlagToString(final int accessFlag) {
        switch (accessFlag) {
            case Opcodes.ACC_PUBLIC:
                return "public";
            case Opcodes.ACC_PRIVATE:
                return "private";
            case Opcodes.ACC_PROTECTED:
                return "protected";
            case Opcodes.ACC_FINAL:
                return "final";
            case Opcodes.ACC_SUPER:
                return "super";
            case Opcodes.ACC_INTERFACE:
                return "interface";
            case Opcodes.ACC_ABSTRACT:
                return "abstract";
            case Opcodes.ACC_SYNTHETIC:
                return "synthetic";
            case Opcodes.ACC_ANNOTATION:
                return "annotation";
            case Opcodes.ACC_ENUM:
                return "enum";
            default:
                throw new IllegalArgumentException("Unknown class access flag: " + Integer.toHexString(accessFlag));
        }
    }

    public static String methodAccessFlagToString(final int accessFlag) {
        switch (accessFlag) {
            case Opcodes.ACC_PUBLIC:
                return "public";
            case Opcodes.ACC_PRIVATE:
                return "private";
            case Opcodes.ACC_PROTECTED:
                return "protected";
            case Opcodes.ACC_STATIC:
                return "static";
            case Opcodes.ACC_FINAL:
                return "final";
            case Opcodes.ACC_SYNCHRONIZED:
                return "synchronized";
            case Opcodes.ACC_BRIDGE:
                return "bridge";
            case Opcodes.ACC_VARARGS:
                return "varargs";
            case Opcodes.ACC_NATIVE:
                return "native";
            case Opcodes.ACC_ABSTRACT:
                return "abstract";
            case Opcodes.ACC_STRICT:
                return "strict";
            case Opcodes.ACC_SYNTHETIC:
                return "synthetic";
            default:
                throw new IllegalArgumentException("Unknown method access flag: " + Integer.toHexString(accessFlag));
        }
    }

    public static String fieldAccessFlagToString(final int accessFlag) {
        switch (accessFlag) {
            case Opcodes.ACC_PUBLIC:
                return "public";
            case Opcodes.ACC_PRIVATE:
                return "private";
            case Opcodes.ACC_PROTECTED:
                return "protected";
            case Opcodes.ACC_STATIC:
                return "static";
            case Opcodes.ACC_FINAL:
                return "final";
            case Opcodes.ACC_VOLATILE:
                return "volatile";
            case Opcodes.ACC_TRANSIENT:
                return "transient";
            case Opcodes.ACC_SYNTHETIC:
                return "synthetic";
            default:
                throw new IllegalArgumentException("Unknown field access flag: " + Integer.toHexString(accessFlag));
        }
    }
}
