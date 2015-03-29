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

package com.michaelrocks.lightsaber.processor.generation;

import com.michaelrocks.lightsaber.Injector;
import com.michaelrocks.lightsaber.processor.FieldDescriptor;
import com.michaelrocks.lightsaber.processor.MethodDescriptor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class InjectorClassGenerator {
    private static final String INJECT_MEMBERS_METHOD_NAME = "injectMembers";
    private static final String GET_INSTANCE_METHOD_NAME = "getInstance";

    private final Type injectorType;
    private final Type targetType;
    private final List<FieldDescriptor> targetFields;

    public InjectorClassGenerator(final Type injectorType, final Type targetType,
            final List<FieldDescriptor> targetFields) {
        this.injectorType = injectorType;
        this.targetType = targetType;
        this.targetFields = targetFields;
    }

    public byte[] generate() {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(
                V1_6,
                ACC_SUPER,
                injectorType.getInternalName(),
                null,
                Type.getInternalName(Object.class),
                null);

        generateConstructor(classWriter);
        generateInjectMembersMethod(classWriter);

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateConstructor(final ClassWriter classWriter) {
        final MethodDescriptor defaultConstructor = MethodDescriptor.forConstructor();
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                0,
                defaultConstructor.getName(),
                defaultConstructor.getType().getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(Object.class),
                defaultConstructor.getName(),
                defaultConstructor.getType().getDescriptor(),
                false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateInjectMembersMethod(final ClassWriter classWriter) {
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_STATIC,
                INJECT_MEMBERS_METHOD_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Injector.class), targetType),
                null,
                null);
        methodVisitor.visitCode();

        for (final FieldDescriptor fieldDescriptor : targetFields) {
            generateFieldInitializer(methodVisitor, fieldDescriptor);
        }

        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateFieldInitializer(final MethodVisitor methodVisitor, final FieldDescriptor fieldDescriptor) {
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitLdcInsn(fieldDescriptor.getType());
        methodVisitor.visitMethodInsn(
                INVOKEINTERFACE,
                Type.getInternalName(Injector.class),
                GET_INSTANCE_METHOD_NAME,
                Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Class.class)),
                true);
        methodVisitor.visitTypeInsn(CHECKCAST, fieldDescriptor.getType().getInternalName());
        methodVisitor.visitFieldInsn(
                PUTFIELD,
                targetType.getInternalName(),
                fieldDescriptor.getName(),
                fieldDescriptor.getType().getDescriptor());
    }
}
