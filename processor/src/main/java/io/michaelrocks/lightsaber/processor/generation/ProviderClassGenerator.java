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
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator;
import io.michaelrocks.lightsaber.processor.commons.Boxer;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import io.michaelrocks.lightsaber.processor.warermark.WatermarkClassVisitor;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Provider;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class ProviderClassGenerator {
    private static final String KEY_FIELD_NAME_PREFIX = "key";
    private static final String MODULE_FIELD_NAME = "module";
    private static final String INJECTOR_FIELD_NAME = "injector";
    private static final String GET_METHOD_NAME = "get";
    private static final String GET_PROVIDER_METHOD_NAME = "getProvider";
    private static final String INJECT_MEMBERS_METHOD_NAME = "injectMembers";
    private static final String COPY_WITH_INJECTOR_METHOD_NAME = "copyWithInjector";

    private static final MethodDescriptor KEY_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE);

    private final ProcessorContext processorContext;
    private final AnnotationCreator annotationCreator;
    private final ProviderDescriptor provider;

    public ProviderClassGenerator(final ProcessorContext processorContext, final AnnotationCreator annotationCreator,
            final ProviderDescriptor provider) {
        this.processorContext = processorContext;
        this.annotationCreator = annotationCreator;
        this.provider = provider;
    }

    public byte[] generate() {
        final ClassWriter classWriter =
                new StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, processorContext);
        final ClassVisitor classVisitor = new WatermarkClassVisitor(classWriter, true);
        classVisitor.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                provider.getProviderType().getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(CopyableProvider.class) });

        generateFields(classVisitor);
        generateStaticInitializer(classVisitor);
        generateConstructor(classVisitor);
        generateGetMethod(classVisitor);
        generateCopyWithInjector(classVisitor);

        classVisitor.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateFields(final ClassVisitor classVisitor) {
        generateKeyFields(classVisitor);
        generateModuleField(classVisitor);
        generateInjectorField(classVisitor);
    }

    private void generateKeyFields(final ClassVisitor classVisitor) {
        final List<TypeSignature> argumentTypes = provider.getProviderMethod().getArgumentTypes();
        for (int i = 0, count = argumentTypes.size(); i < count; ++i) {
            final FieldVisitor fieldVisitor = classVisitor.visitField(
                    ACC_PRIVATE | ACC_STATIC | ACC_FINAL,
                    KEY_FIELD_NAME_PREFIX + i,
                    Types.KEY_TYPE.getDescriptor(),
                    null,
                    null);
            fieldVisitor.visitEnd();
        }
    }

    private void generateModuleField(final ClassVisitor classVisitor) {
        final FieldVisitor fieldVisitor = classVisitor.visitField(
                ACC_PRIVATE | ACC_FINAL,
                MODULE_FIELD_NAME,
                provider.getModuleType().getDescriptor(),
                null,
                null);
        fieldVisitor.visitEnd();
    }

    private void generateInjectorField(final ClassVisitor classVisitor) {
        final FieldVisitor fieldVisitor = classVisitor.visitField(
                ACC_PRIVATE | ACC_FINAL,
                INJECTOR_FIELD_NAME,
                Type.getDescriptor(Injector.class),
                null,
                null);
        fieldVisitor.visitEnd();
    }

    private void generateStaticInitializer(final ClassVisitor classVisitor) {
        final List<TypeSignature> argumentTypes = provider.getProviderMethod().getArgumentTypes();
        final List<AnnotationData> parameterQualifiers = provider.getProviderMethod().getParameterQualifiers();
        Validate.isTrue(argumentTypes.size() == parameterQualifiers.size());

        if (argumentTypes.isEmpty()) {
            return;
        }

        final MethodDescriptor staticInitializer = MethodDescriptor.forStaticInitializer();
        final GeneratorAdapter generator =
                new GeneratorAdapter(classVisitor, ACC_STATIC, staticInitializer, null, null);
        generator.visitCode();

        for (int i = 0, count = argumentTypes.size(); i < count; ++i) {
            final TypeSignature argumentType = argumentTypes.get(i);
            final AnnotationData parameterQualifier = parameterQualifiers.get(i);
            final Type dependencyType = argumentType.getParameterType() != null
                    ? argumentType.getParameterType() : Types.box(argumentType.getRawType());

            generator.newInstance(Types.KEY_TYPE);
            generator.dup();
            generator.push(dependencyType);
            if (parameterQualifier == null) {
                generator.pushNull();
            } else {
                annotationCreator.newAnnotation(generator, parameterQualifier);
            }
            generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR);
            generator.putStatic(provider.getProviderType(), KEY_FIELD_NAME_PREFIX + i, Types.KEY_TYPE);
        }

        generator.returnValue();
        generator.endMethod();
    }

    private void generateConstructor(final ClassVisitor classVisitor) {
        final MethodDescriptor providerConstructor = getProviderConstructor();
        final MethodVisitor methodVisitor = classVisitor.visitMethod(
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

    private void generateGetMethod(final ClassVisitor classVisitor) {
        final MethodVisitor methodVisitor = classVisitor.visitMethod(
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

        Boxer.box(methodVisitor, provider.getProvidableType());

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
        final List<TypeSignature> argumentTypes = provider.getProviderMethod().getArgumentTypes();
        for (int i = 0, count = argumentTypes.size(); i < count; i++) {
            final TypeSignature argumentType = argumentTypes.get(i);
            generateProviderMethodArgument(methodVisitor, argumentType, i);
        }
    }

    private void generateProviderMethodArgument(final MethodVisitor methodVisitor, final TypeSignature argumentType,
            final int argumentIndex) {
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(
                GETFIELD,
                provider.getProviderType().getInternalName(),
                INJECTOR_FIELD_NAME,
                Type.getDescriptor(Injector.class));
        methodVisitor.visitFieldInsn(
                GETSTATIC,
                provider.getProviderType().getInternalName(),
                KEY_FIELD_NAME_PREFIX + argumentIndex,
                Types.KEY_TYPE.getDescriptor());
        methodVisitor.visitMethodInsn(
                INVOKEINTERFACE,
                Type.getInternalName(Injector.class),
                GET_PROVIDER_METHOD_NAME,
                Type.getMethodDescriptor(Type.getType(Provider.class), Types.KEY_TYPE),
                true);
        if (argumentType.getParameterType() == null) {
            methodVisitor.visitMethodInsn(
                    INVOKEINTERFACE,
                    Type.getInternalName(Provider.class),
                    GET_METHOD_NAME,
                    Type.getMethodDescriptor(Type.getType(Object.class)),
                    true);
        }
        GenerationHelper.convertDependencyToTargetType(methodVisitor, argumentType);
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
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Injector.class), Type.getType(Object.class)),
                false);
    }

    private void generateCopyWithInjector(final ClassVisitor classVisitor) {
        final MethodVisitor methodVisitor = classVisitor.visitMethod(
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
