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

import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class GeneratorAdapter extends org.objectweb.asm.commons.GeneratorAdapter {
    public GeneratorAdapter(final MethodVisitor methodVisitor, final int access, final String name, final String desc) {
        super(ASM5, methodVisitor, access, name, desc);
    }

    public GeneratorAdapter(final MethodVisitor methodVisitor, final int access, final MethodDescriptor method) {
        this(methodVisitor, access, method.getName(), method.getDescriptor());
    }

    public GeneratorAdapter(final ClassVisitor classVisitor, final int access, final MethodDescriptor method) {
        this(visitMethod(classVisitor, access, method, null, null), access, method);
    }

    public GeneratorAdapter(final ClassVisitor classVisitor, final int access, final MethodDescriptor method,
            final String signature, final Type[] exceptions) {
        this(visitMethod(classVisitor, access, method, signature, exceptions), access, method);
    }

    private static MethodVisitor visitMethod(final ClassVisitor classVisitor, final int access,
            final MethodDescriptor method, final String signature, final Type[] exceptions) {
        return classVisitor.visitMethod(access, method.getName(), method.getDescriptor(), signature,
                getInternalNames(exceptions));
    }

    private static String[] getInternalNames(final Type[] types) {
        if (types == null) {
            return null;
        }
        final String[] names = new String[types.length];
        for (int i = 0; i < names.length; ++i) {
            names[i] = types[i].getInternalName();
        }
        return names;
    }

    public void newArray(final Type type, final int size) {
        push(size);
        super.newArray(type);
    }

    public void invokeVirtual(final Type owner, final MethodDescriptor method) {
        invoke(INVOKEVIRTUAL, owner, method, false);
    }

    public void invokeConstructor(final Type type, final MethodDescriptor method) {
        invoke(INVOKESPECIAL, type, method, false);
    }

    public void invokeStatic(final Type owner, final MethodDescriptor method) {
        invoke(INVOKESTATIC, owner, method, false);
    }

    public void invokeInterface(final Type owner, final MethodDescriptor method) {
        invoke(INVOKEINTERFACE, owner, method, true);
    }

    private void invoke(final int opcode, final Type type, final MethodDescriptor method,
            final boolean ownerIsInterface) {
        final String owner = type.getSort() == Type.ARRAY ? type.getDescriptor() : type.getInternalName();
        visitMethodInsn(opcode, owner, method.getName(), method.getDescriptor(), ownerIsInterface);
    }

    public void getField(final Type owner, final FieldDescriptor field) {
        getField(owner, field.getName(), field.getRawType());
    }

    public void putField(final Type owner, final FieldDescriptor field) {
        putField(owner, field.getName(), field.getRawType());
    }

    public void getStatic(final Type owner, final FieldDescriptor field) {
        getStatic(owner, field.getName(), field.getRawType());
    }

    public void putStatic(final Type owner, final FieldDescriptor field) {
        putStatic(owner, field.getName(), field.getRawType());
    }

    public void pushNull() {
        visitInsn(ACONST_NULL);
    }

    public void visitFrame(final int type, final int nLocal, final Type[] local, final int nStack, final Type[] stack) {
        final Object[] localObjects = convertTypeArrayToFrameObjectArray(local);
        final Object[] stackObjects = convertTypeArrayToFrameObjectArray(stack);
        visitFrame(type, nLocal, localObjects, nStack, stackObjects);
    }

    private Object[] convertTypeArrayToFrameObjectArray(final Type[] types) {
        if (types == null) {
            return null;
        }

        final List<Object> objects = new ArrayList<>(types.length * 2);
        for (final Type type : types) {
            switch (type.getSort()) {
                case Type.BOOLEAN:
                case Type.CHAR:
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                    objects.add(INTEGER);
                    break;
                case Type.FLOAT:
                    objects.add(FLOAT);
                    break;
                case Type.LONG:
                    objects.add(LONG);
                    objects.add(TOP);
                    break;
                case Type.DOUBLE:
                    objects.add(DOUBLE);
                    objects.add(TOP);
                    break;
                case Type.ARRAY:
                    objects.add(type.getDescriptor());
                    break;
                case Type.OBJECT:
                    objects.add(type.getInternalName());
                    break;
                default:
                    throw new IllegalArgumentException("Illegal type used in frame: " + type);
            }
        }
        return objects.toArray(new Object[objects.size()]);
    }
}