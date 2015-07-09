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

import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

public class Boxer {
    private static final String VALUE_OF_METHOD_NAME = "valueOf";

    private static final Map<Integer, UnboxMethodDescriptor> unboxMethods;

    static {
        unboxMethods = new HashMap<>();
        registerUnboxMethod(Type.BOOLEAN_TYPE);
        registerUnboxMethod(Type.BYTE_TYPE);
        registerUnboxMethod(Type.CHAR_TYPE);
        registerUnboxMethod(Type.DOUBLE_TYPE);
        registerUnboxMethod(Type.FLOAT_TYPE);
        registerUnboxMethod(Type.INT_TYPE);
        registerUnboxMethod(Type.LONG_TYPE);
        registerUnboxMethod(Type.SHORT_TYPE);
    }

    private Boxer() {
    }

    private static void registerUnboxMethod(final Type unboxedType) {
        unboxMethods.put(unboxedType.getSort(), UnboxMethodDescriptor.forType(unboxedType));
    }

    public static void box(final MethodVisitor methodVisitor, final Type unboxedType) {
        final Type boxedType = Types.box(unboxedType);
        if (!unboxedType.equals(boxedType)) {
            final MethodDescriptor valueOfMethod =
                    MethodDescriptor.forMethod(VALUE_OF_METHOD_NAME, boxedType, unboxedType);
            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    boxedType.getInternalName(),
                    valueOfMethod.getName(),
                    valueOfMethod.getDescriptor(),
                    false);
        }
    }

    public static void unbox(final MethodVisitor methodVisitor, final Type boxedType) {
        final Type unboxedType = Types.unbox(boxedType);
        if (!unboxedType.equals(boxedType)) {
            final UnboxMethodDescriptor unboxMethod = unboxMethods.get(unboxedType.getSort());
            if (unboxMethod != null) {
                methodVisitor.visitMethodInsn(
                        INVOKEVIRTUAL,
                        unboxMethod.getOwnerType().getInternalName(),
                        unboxMethod.getName(),
                        unboxMethod.getDescriptor(),
                        false);
            }
        }
    }

    private static class UnboxMethodDescriptor {
        private static final Type NUMBER_TYPE = Type.getType(Number.class);

        private final Type ownerType;
        private final MethodDescriptor method;

        private UnboxMethodDescriptor(final Type ownerType, final MethodDescriptor method) {
            this.ownerType = ownerType;
            this.method = method;
        }

        public Type getOwnerType() {
            return ownerType;
        }

        public MethodDescriptor getMethod() {
            return method;
        }

        public String getName() {
            return method.getName();
        }

        public String getDescriptor() {
            return method.getDescriptor();
        }

        public static UnboxMethodDescriptor forType(final Type unboxedType) {
            final Type ownerType = findOwnerType(unboxedType);
            final MethodDescriptor method =
                    MethodDescriptor.forMethod(unboxedType.getClassName() + "Value", unboxedType);
            return new UnboxMethodDescriptor(ownerType, method);
        }

        private static Type findOwnerType(final Type unboxedType) {
            switch (unboxedType.getSort()) {
                case Type.BOOLEAN:
                    return Types.BOXED_BOOLEAN_TYPE;
                case Type.CHAR:
                    return Types.BOXED_CHAR_TYPE;
                case Type.BYTE:
                case Type.FLOAT:
                case Type.DOUBLE:
                case Type.INT:
                case Type.LONG:
                case Type.SHORT:
                    return NUMBER_TYPE;
                default:
                    throw new IllegalArgumentException("Cannot find owner type for " + unboxedType.getClassName());
            }
        }
    }
}
