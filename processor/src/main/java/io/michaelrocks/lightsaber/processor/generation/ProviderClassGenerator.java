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
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import javax.inject.Provider;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class ProviderClassGenerator {
    private static final String KEY_FIELD_NAME_PREFIX = "key";
    private static final String MODULE_FIELD_NAME = "module";
    private static final String INJECTOR_FIELD_NAME = "injector";

    private static final Type NULL_POINTER_EXCEPTION_TYPE = Type.getType(NullPointerException.class);

    private static final MethodDescriptor KEY_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE);
    private static final MethodDescriptor GET_METHOD =
            MethodDescriptor.forMethod("get", Types.OBJECT_TYPE);
    private static final MethodDescriptor GET_PROVIDER_METHOD =
            MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.KEY_TYPE);
    private static final MethodDescriptor INJECT_MEMBERS_METHOD =
            MethodDescriptor.forMethod("injectMembers", Type.VOID_TYPE, Types.INJECTOR_TYPE, Types.OBJECT_TYPE);

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
                new String[] { Type.getInternalName(Provider.class) });

        generateFields(classVisitor);
        generateStaticInitializer(classVisitor);
        generateConstructor(classVisitor);
        generateGetMethod(classVisitor);

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
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, getProviderConstructor());
        generator.visitCode();
        generator.loadThis();
        generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forConstructor());
        generator.loadThis();
        generator.loadArg(0);
        generator.putField(provider.getProviderType(), MODULE_FIELD_NAME, provider.getModuleType());
        generator.loadThis();
        generator.loadArg(1);
        generator.putField(provider.getProviderType(), INJECTOR_FIELD_NAME, Types.INJECTOR_TYPE);
        generator.returnValue();
        generator.endMethod();
    }

    private void generateGetMethod(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, GET_METHOD);
        generator.visitCode();

        if (provider.getProviderMethod().isConstructor()) {
            generateConstructorInvocation(generator);
            generateInjectMembersInvocation(generator);
        } else {
            generateProviderMethodInvocation(generator);
        }

        generator.valueOf(provider.getProvidableType());

        generator.returnValue();
        generator.endMethod();
    }

    private void generateConstructorInvocation(final GeneratorAdapter generator) {
        generator.newInstance(provider.getProvidableType());
        generator.dup();
        generateProvideMethodArguments(generator);
        generator.invokeConstructor(provider.getProvidableType(), provider.getProviderMethod().getMethod());
    }

    private void generateProviderMethodInvocation(final GeneratorAdapter generator) {
        generator.loadThis();
        generator.getField(provider.getProviderType(), MODULE_FIELD_NAME, provider.getModuleType());
        generateProvideMethodArguments(generator);
        generator.invokeVirtual(provider.getModuleType(), provider.getProviderMethod().getMethod());

        if (Types.isPrimitive(provider.getProvidableType())) {
            return;
        }

        final Label resultIsNullLabel = generator.newLabel();
        generator.dup();
        generator.ifNonNull(resultIsNullLabel);
        generator.throwException(NULL_POINTER_EXCEPTION_TYPE, "Provider method returned null");

        generator.visitLabel(resultIsNullLabel);
    }

    private void generateProvideMethodArguments(final GeneratorAdapter generator) {
        final List<TypeSignature> argumentTypes = provider.getProviderMethod().getArgumentTypes();
        for (int i = 0, count = argumentTypes.size(); i < count; i++) {
            final TypeSignature argumentType = argumentTypes.get(i);
            generateProviderMethodArgument(generator, argumentType, i);
        }
    }

    private void generateProviderMethodArgument(final GeneratorAdapter generator, final TypeSignature argumentType,
            final int argumentIndex) {
        generator.loadThis();
        generator.getField(provider.getProviderType(), INJECTOR_FIELD_NAME, Types.INJECTOR_TYPE);
        generator.getStatic(provider.getProviderType(), KEY_FIELD_NAME_PREFIX + argumentIndex, Types.KEY_TYPE);
        generator.invokeInterface(Types.INJECTOR_TYPE, GET_PROVIDER_METHOD);
        if (argumentType.getParameterType() == null) {
            generator.invokeInterface(Types.PROVIDER_TYPE, GET_METHOD);
        }
        GenerationHelper.convertDependencyToTargetType(generator, argumentType);
    }

    private void generateInjectMembersInvocation(final GeneratorAdapter generator) {
        generator.dup();
        generator.loadThis();
        generator.getField(provider.getProviderType(), INJECTOR_FIELD_NAME, Types.INJECTOR_TYPE);
        generator.swap();
        generator.invokeStatic(processorContext.getInjectorFactoryType(), INJECT_MEMBERS_METHOD);
    }

    private MethodDescriptor getProviderConstructor() {
        return MethodDescriptor.forConstructor(provider.getModuleType(), Type.getType(Injector.class));
    }
}
