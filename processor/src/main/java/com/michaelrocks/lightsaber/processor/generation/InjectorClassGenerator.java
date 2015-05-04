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
import com.michaelrocks.lightsaber.internal.TypeInjector;
import com.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Provider;

import static org.objectweb.asm.Opcodes.*;

public class InjectorClassGenerator {
    private static final String INJECT_MEMBERS_METHOD_NAME = "injectMembers";
    private static final String GET_INSTANCE_METHOD_NAME = "getInstance";
    private static final String GET_PROVIDER_METHOD_NAME = "getProvider";

    private static final MethodDescriptor GET_INSTANCE_METHOD =
            MethodDescriptor.forMethod(GET_INSTANCE_METHOD_NAME,
                    Type.getType(Object.class), Type.getType(Class.class));
    private static final MethodDescriptor GET_PROVIDER_METHOD =
            MethodDescriptor.forMethod(GET_PROVIDER_METHOD_NAME,
                    Type.getType(Provider.class), Type.getType(Class.class));

    private final InjectorDescriptor injector;

    public InjectorClassGenerator(final InjectorDescriptor injector) {
        this.injector = injector;
    }

    public byte[] generate() {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                injector.getInjectorType().getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(TypeInjector.class) });

        generateConstructor(classWriter);
        generateInjectMembersMethod(classWriter);

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateConstructor(final ClassWriter classWriter) {
        final MethodDescriptor defaultConstructor = MethodDescriptor.forConstructor();
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
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
                ACC_PUBLIC,
                INJECT_MEMBERS_METHOD_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Injector.class), Type.getType(Object.class)),
                null,
                null);
        methodVisitor.visitCode();

        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitTypeInsn(CHECKCAST, injector.getInjectableTarget().getTargetType().getInternalName());
        methodVisitor.visitVarInsn(ASTORE, 3);
        for (final FieldDescriptor fieldDescriptor : injector.getInjectableTarget().getInjectableFields()) {
            generateFieldInitializer(methodVisitor, fieldDescriptor);
        }

        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateFieldInitializer(final MethodVisitor methodVisitor, final FieldDescriptor fieldDescriptor) {
        methodVisitor.visitVarInsn(ALOAD, 3);
        methodVisitor.visitVarInsn(ALOAD, 1);

        methodVisitor.visitLdcInsn(getDependencyTypeForField(fieldDescriptor));
        final MethodDescriptor method = getInjectorMethodForField(fieldDescriptor);
        methodVisitor.visitMethodInsn(
                INVOKEINTERFACE,
                Type.getInternalName(Injector.class),
                method.getName(),
                method.getDescriptor(),
                true);
        if (!fieldDescriptor.isParameterized()) {
            methodVisitor.visitTypeInsn(CHECKCAST, fieldDescriptor.getRawType().getInternalName());
        }
        methodVisitor.visitFieldInsn(
                PUTFIELD,
                injector.getInjectableTarget().getTargetType().getInternalName(),
                fieldDescriptor.getName(),
                fieldDescriptor.getRawType().getDescriptor());
    }

    private static Type getDependencyTypeForField(final FieldDescriptor field) {
        return field.isParameterized() ? field.getArgumentType() : field.getRawType();
    }

    private static MethodDescriptor getInjectorMethodForField(final FieldDescriptor field) {
        if (field.isParameterized()) {
            return GET_PROVIDER_METHOD;
        } else {
            return GET_INSTANCE_METHOD;
        }
    }
}
