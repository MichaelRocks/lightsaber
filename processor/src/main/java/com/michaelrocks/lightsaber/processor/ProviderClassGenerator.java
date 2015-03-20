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

package com.michaelrocks.lightsaber.processor;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Provider;

import static org.objectweb.asm.Opcodes.*;

public class ProviderClassGenerator {
    private static final String MODULE_FIELD_NAME = "module";
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String GET_METHOD_NAME = "get";

    private final Type providerType;
    private final Type moduleType;
    private final String providerMethodName;
    private final Type providerMethodType;

    public ProviderClassGenerator(final Type providerType, final Type moduleType, final String providerMethodName,
            final Type providerMethodType) {
        this.providerType = providerType;
        this.moduleType = moduleType;
        this.providerMethodName = providerMethodName;
        this.providerMethodType = providerMethodType;
    }

    public byte[] generate() {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(
                V1_6,
                ACC_SUPER,
                providerType.getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(Provider.class) });

        generateModuleField(classWriter);
        generateConstructor(classWriter);
        generateGetMethod(classWriter);

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateModuleField(final ClassWriter classWriter) {
        final FieldVisitor fieldVisitor =
                classWriter.visitField(ACC_PRIVATE | ACC_FINAL, MODULE_FIELD_NAME, moduleType.getDescriptor(), null,
                        null);
        fieldVisitor.visitEnd();
    }

    private void generateConstructor(final ClassWriter classWriter) {
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                0,
                CONSTRUCTOR_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, moduleType),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(Object.class),
                CONSTRUCTOR_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE),
                false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitFieldInsn(
                PUTFIELD,
                providerType.getInternalName(),
                MODULE_FIELD_NAME,
                moduleType.getDescriptor());
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateGetMethod(final ClassWriter classWriter) {
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                GET_METHOD_NAME,
                Type.getMethodDescriptor(Type.getType(Object.class)),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(
                GETFIELD,
                providerType.getInternalName(),
                MODULE_FIELD_NAME,
                moduleType.getDescriptor());
        methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                moduleType.getInternalName(),
                providerMethodName,
                providerMethodType.getDescriptor(),
                false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }
}

