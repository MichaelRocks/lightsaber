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

import com.michaelrocks.lightsaber.Injector;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Provider;

import static org.objectweb.asm.Opcodes.*;

public class ProviderClassGenerator {
    private static final String MODULE_FIELD_NAME = "module";
    private static final String INJECTOR_FIELD_NAME = "injector";
    private static final String GET_METHOD_NAME = "get";
    private static final String GET_PROVIDER_METHOD_NAME = "getProvider";

    private final Type providerType;
    private final Type moduleType;
    private final MethodDescriptor providerMethodDescriptor;

    public ProviderClassGenerator(final Type providerType, final Type moduleType,
            final MethodDescriptor providerMethodDescriptor) {
        this.providerType = providerType;
        this.moduleType = moduleType;
        this.providerMethodDescriptor = providerMethodDescriptor;
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
        generateInjectorField(classWriter);
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

    private void generateInjectorField(final ClassWriter classWriter) {
        final FieldVisitor fieldVisitor = classWriter.visitField(
                    ACC_PRIVATE | ACC_FINAL,
                    INJECTOR_FIELD_NAME,
                    Type.getDescriptor(Injector.class),
                    null,
                    null);
        fieldVisitor.visitEnd();
    }

    private void generateConstructor(final ClassWriter classWriter) {
        final MethodDescriptor providerConstructor =
                MethodDescriptor.forConstructor(moduleType, Type.getType(Injector.class));
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                0,
                providerConstructor.getName(),
                providerConstructor.getType().getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        final MethodDescriptor objectConstructor = MethodDescriptor.forConstructor();
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(Object.class),
                objectConstructor.getName(),
                objectConstructor.getType().getDescriptor(),
                false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitFieldInsn(
                PUTFIELD,
                providerType.getInternalName(),
                MODULE_FIELD_NAME,
                moduleType.getDescriptor());
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitFieldInsn(
                PUTFIELD,
                providerType.getInternalName(),
                INJECTOR_FIELD_NAME,
                Type.getDescriptor(Injector.class));
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
        generateProvideMethodArguments(methodVisitor);
        methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                moduleType.getInternalName(),
                providerMethodDescriptor.getName(),
                providerMethodDescriptor.getType().getDescriptor(),
                false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateProvideMethodArguments(final MethodVisitor methodVisitor) {
        for (final Type argumentType : providerMethodDescriptor.getType().getArgumentTypes()) {
            generateProviderMethodArgument(methodVisitor, argumentType);
        }
    }

    private void generateProviderMethodArgument(final MethodVisitor methodVisitor, final Type argumentType) {
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(
                GETFIELD,
                providerType.getInternalName(),
                INJECTOR_FIELD_NAME,
                Type.getDescriptor(Injector.class));
        methodVisitor.visitLdcInsn(argumentType);
        methodVisitor.visitMethodInsn(
                INVOKEINTERFACE,
                Type.getInternalName(Injector.class),
                GET_PROVIDER_METHOD_NAME,
                Type.getMethodDescriptor(Type.getType(Provider.class), Type.getType(Class.class)),
                true);
        methodVisitor.visitMethodInsn(
                INVOKEINTERFACE,
                Type.getInternalName(Provider.class),
                GET_METHOD_NAME,
                Type.getMethodDescriptor(Type.getType(Object.class)),
                true);
        methodVisitor.visitTypeInsn(CHECKCAST, argumentType.getInternalName());
    }
}
