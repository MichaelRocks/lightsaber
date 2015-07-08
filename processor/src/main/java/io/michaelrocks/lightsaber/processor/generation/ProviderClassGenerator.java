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

import io.michaelrocks.lightsaber.CopyableProvider;
import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
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
    private static final String INJECT_MEMBERS_METHOD_NAME = "injectMembers";
    private static final String COPY_WITH_INJECTOR_METHOD_NAME = "copyWithInjector";

    private static final String VALUE_OF_METHOD_NAME = "valueOf";

    private final ProcessorContext processorContext;
    private final ProviderDescriptor provider;

    public ProviderClassGenerator(final ProcessorContext processorContext, final ProviderDescriptor provider) {
        this.processorContext = processorContext;
        this.provider = provider;
    }

    public byte[] generate() {
        final ClassWriter classWriter =
                new StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, processorContext);
        classWriter.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                provider.getProviderType().getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(CopyableProvider.class) });

        generateModuleField(classWriter);
        generateInjectorField(classWriter);
        generateConstructor(classWriter);
        generateGetMethod(classWriter);
        generateCopyWithInjector(classWriter);

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateModuleField(final ClassWriter classWriter) {
        final FieldVisitor fieldVisitor = classWriter.visitField(
                ACC_PRIVATE | ACC_FINAL,
                MODULE_FIELD_NAME,
                provider.getModuleType().getDescriptor(),
                null,
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
        final MethodDescriptor providerConstructor = getProviderConstructor();
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                providerConstructor.getName(),
                providerConstructor.getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        final MethodDescriptor objectConstructor = MethodDescriptor.forConstructor();
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(Object.class),
                objectConstructor.getName(),
                objectConstructor.getDescriptor(),
                false);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitFieldInsn(
                PUTFIELD,
                provider.getProviderType().getInternalName(),
                MODULE_FIELD_NAME,
                provider.getModuleType().getDescriptor());
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitFieldInsn(
                PUTFIELD,
                provider.getProviderType().getInternalName(),
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

        if (provider.getProviderMethod().isConstructor()) {
            generateConstructorInvocation(methodVisitor);
            generateInjectMembersInvocation(methodVisitor);
        } else {
            generateProviderMethodInvocation(methodVisitor);
        }

        generateResultBoxingIfNecessary(methodVisitor);

        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateConstructorInvocation(final MethodVisitor methodVisitor) {
        methodVisitor.visitTypeInsn(NEW, provider.getProvidableType().getInternalName());
        methodVisitor.visitInsn(DUP);
        generateProvideMethodArguments(methodVisitor);
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                provider.getProvidableType().getInternalName(),
                provider.getProviderMethod().getName(),
                provider.getProviderMethod().getDescriptor(),
                false);
    }

    private void generateProviderMethodInvocation(final MethodVisitor methodVisitor) {
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(
                GETFIELD,
                provider.getProviderType().getInternalName(),
                MODULE_FIELD_NAME,
                provider.getModuleType().getDescriptor());
        generateProvideMethodArguments(methodVisitor);
        methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                provider.getModuleType().getInternalName(),
                provider.getProviderMethod().getName(),
                provider.getProviderMethod().getDescriptor(),
                false);
    }

    private void generateProvideMethodArguments(final MethodVisitor methodVisitor) {
        for (final TypeSignature argumentType : provider.getProviderMethod().getArgumentTypes()) {
            generateProviderMethodArgument(methodVisitor, argumentType);
        }
    }

    private void generateProviderMethodArgument(final MethodVisitor methodVisitor, final TypeSignature argumentType) {
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(
                GETFIELD,
                provider.getProviderType().getInternalName(),
                INJECTOR_FIELD_NAME,
                Type.getDescriptor(Injector.class));
        final Type dependencyType =
                argumentType.getParameterType() != null ? argumentType.getParameterType() : argumentType.getRawType();
        methodVisitor.visitLdcInsn(dependencyType);
        methodVisitor.visitMethodInsn(
                INVOKEINTERFACE,
                Type.getInternalName(Injector.class),
                GET_PROVIDER_METHOD_NAME,
                Type.getMethodDescriptor(Type.getType(Provider.class), Type.getType(Class.class)),
                true);
        if (argumentType.getParameterType() == null) {
            methodVisitor.visitMethodInsn(
                    INVOKEINTERFACE,
                    Type.getInternalName(Provider.class),
                    GET_METHOD_NAME,
                    Type.getMethodDescriptor(Type.getType(Object.class)),
                    true);
            methodVisitor.visitTypeInsn(CHECKCAST, dependencyType.getInternalName());
        }
    }

    private void generateInjectMembersInvocation(final MethodVisitor methodVisitor) {
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ASTORE, 1);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(
                GETFIELD,
                provider.getProviderType().getInternalName(),
                INJECTOR_FIELD_NAME,
                Type.getDescriptor(Injector.class));
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(
                INVOKESTATIC,
                processorContext.getInjectorFactoryType().getInternalName(),
                INJECT_MEMBERS_METHOD_NAME,
                Type.getMethodDescriptor(
                        Type.VOID_TYPE, Type.getType(Injector.class), Type.getType(Object.class)),
                false);
    }

    private void generateResultBoxingIfNecessary(final MethodVisitor methodVisitor) {
        final Type rawProvidableType = provider.getProvidableType();
        final Type boxedProvidableType = Types.box(rawProvidableType);
        if (!rawProvidableType.equals(boxedProvidableType)) {
            final MethodDescriptor valueOfMethod =
                    MethodDescriptor.forMethod(VALUE_OF_METHOD_NAME, boxedProvidableType, rawProvidableType);
            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    boxedProvidableType.getInternalName(),
                    valueOfMethod.getName(),
                    valueOfMethod.getDescriptor(),
                    false);
        }
    }

    private void generateCopyWithInjector(final ClassWriter classWriter) {
        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                COPY_WITH_INJECTOR_METHOD_NAME,
                Type.getMethodDescriptor(Type.getType(CopyableProvider.class), Type.getType(Injector.class)),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitTypeInsn(NEW, provider.getProviderType().getInternalName());
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(
                GETFIELD,
                provider.getProviderType().getInternalName(),
                MODULE_FIELD_NAME,
                provider.getModuleType().getDescriptor());
        methodVisitor.visitVarInsn(ALOAD, 1);
        final MethodDescriptor providerConstructor = getProviderConstructor();
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                provider.getProviderType().getInternalName(),
                providerConstructor.getName(),
                providerConstructor.getDescriptor(),
                false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private MethodDescriptor getProviderConstructor() {
        return MethodDescriptor.forConstructor(provider.getModuleType(), Type.getType(Injector.class));
    }
}
