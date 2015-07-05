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

package io.michaelrocks.lightsaber.processor.generation;

import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.internal.TypeAgent;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Provider;

import static org.objectweb.asm.Opcodes.*;

public class TypeAgentClassGenerator {
    private static final String GET_TYPE_METHOD_NAME = "getType";
    private static final String INJECT_FIELDS_METHOD_NAME = "injectFields";
    private static final String INJECT_METHODS_METHOD_NAME = "injectMethods";
    private static final String GET_INSTANCE_METHOD_NAME = "getInstance";
    private static final String GET_PROVIDER_METHOD_NAME = "getProvider";

    private static final MethodDescriptor GET_INSTANCE_METHOD =
            MethodDescriptor.forMethod(GET_INSTANCE_METHOD_NAME,
                    Type.getType(Object.class), Type.getType(Class.class));
    private static final MethodDescriptor GET_PROVIDER_METHOD =
            MethodDescriptor.forMethod(GET_PROVIDER_METHOD_NAME,
                    Type.getType(Provider.class), Type.getType(Class.class));

    private final ProcessorContext processorContext;
    private final InjectorDescriptor injector;

    public TypeAgentClassGenerator(final ProcessorContext processorContext, final InjectorDescriptor injector) {
        this.processorContext = processorContext;
        this.injector = injector;
    }

    public byte[] generate() {
        final ClassWriter classWriter =
                new StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, processorContext);
        classWriter.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                injector.getInjectorType().getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(TypeAgent.class) });

        generateConstructor(classWriter);
        generateGetTypeMethod(classWriter);
        generateInjectFieldsMethod(classWriter);
        generateInjectMethodsMethod(classWriter);

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateConstructor(final ClassWriter classWriter) {
        final MethodDescriptor defaultConstructor = MethodDescriptor.forConstructor();
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                defaultConstructor.getName(),
                defaultConstructor.getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(Object.class),
                defaultConstructor.getName(),
                defaultConstructor.getDescriptor(),
                false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateGetTypeMethod(final ClassWriter classWriter) {
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                GET_TYPE_METHOD_NAME,
                Type.getMethodDescriptor(Type.getType(Class.class)),
                null,
                null);
        methodVisitor.visitCode();

        methodVisitor.visitLdcInsn(injector.getInjectableTarget().getTargetType());

        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateInjectFieldsMethod(final ClassWriter classWriter) {
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                INJECT_FIELDS_METHOD_NAME,
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

        methodVisitor.visitLdcInsn(getDependencyTypeForType(fieldDescriptor.getSignature()));
        final MethodDescriptor method = getInjectorMethodForType(fieldDescriptor.getSignature());
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

    private void generateInjectMethodsMethod(final ClassWriter classWriter) {
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                INJECT_METHODS_METHOD_NAME,
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Injector.class), Type.getType(Object.class)),
                null,
                null);
        methodVisitor.visitCode();

        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitTypeInsn(CHECKCAST, injector.getInjectableTarget().getTargetType().getInternalName());
        methodVisitor.visitVarInsn(ASTORE, 3);
        for (final MethodDescriptor methodDescriptor : injector.getInjectableTarget().getInjectableMethods()) {
            generateMethodInvocation(methodVisitor, methodDescriptor);
        }

        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateMethodInvocation(final MethodVisitor methodVisitor, final MethodDescriptor methodDescriptor) {
        methodVisitor.visitVarInsn(ALOAD, 3);

        for (final TypeSignature argumentType : methodDescriptor.getArgumentTypes()) {
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitLdcInsn(getDependencyTypeForType(argumentType));
            final MethodDescriptor method = getInjectorMethodForType(argumentType);
            methodVisitor.visitMethodInsn(
                    INVOKEINTERFACE,
                    Type.getInternalName(Injector.class),
                    method.getName(),
                    method.getDescriptor(),
                    true);
            if (!argumentType.isParameterized()) {
                methodVisitor.visitTypeInsn(CHECKCAST, argumentType.getRawType().getInternalName());
            }
        }
        methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                injector.getInjectableTarget().getTargetType().getInternalName(),
                methodDescriptor.getName(),
                methodDescriptor.getDescriptor(),
                false);
    }

    private static Type getDependencyTypeForType(final TypeSignature type) {
        return type.isParameterized() ? type.getParameterType() : type.getRawType();
    }

    private static MethodDescriptor getInjectorMethodForType(final TypeSignature type) {
        if (type.isParameterized()) {
            return GET_PROVIDER_METHOD;
        } else {
            return GET_INSTANCE_METHOD;
        }
    }
}
