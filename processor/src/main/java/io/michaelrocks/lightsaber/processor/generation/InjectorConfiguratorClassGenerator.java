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

import io.michaelrocks.lightsaber.LightsaberTypes;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.PackageInvaderDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedType;
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class InjectorConfiguratorClassGenerator {
    private static final MethodDescriptor KEY_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE);
    private static final MethodDescriptor CONFIGURE_INJECTOR_METHOD =
            MethodDescriptor.forMethod("configureInjector",
                    Type.VOID_TYPE, LightsaberTypes.LIGHTSABER_INJECTOR_TYPE, Types.OBJECT_TYPE);
    private static final MethodDescriptor REGISTER_PROVIDER_METHOD =
            MethodDescriptor.forMethod("registerProvider", Type.VOID_TYPE, Types.KEY_TYPE, Types.PROVIDER_TYPE);

    private static final MethodDescriptor DELEGATE_PROVIDER_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.PROVIDER_TYPE);

    private final ProcessorContext processorContext;
    private final AnnotationCreator annotationCreator;
    private final ModuleDescriptor module;

    public InjectorConfiguratorClassGenerator(final ProcessorContext processorContext,
            final AnnotationCreator annotationCreator, final ModuleDescriptor module) {
        this.processorContext = processorContext;
        this.annotationCreator = annotationCreator;
        this.module = module;
    }

    public byte[] generate() {
        final ClassWriter classWriter =
                new StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, processorContext);
        final ClassVisitor classVisitor = new WatermarkClassVisitor(classWriter, true);
        classVisitor.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                module.getConfiguratorType().getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { LightsaberTypes.INJECTOR_CONFIGURATOR_TYPE.getInternalName() });

        generateConstructor(classVisitor);
        generateConfigureInjectorMethod(classVisitor);

        classVisitor.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateConstructor(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator =
                new GeneratorAdapter(classVisitor, ACC_PUBLIC, MethodDescriptor.forDefaultConstructor());
        generator.visitCode();
        generator.loadThis();
        generator.dup();
        generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forDefaultConstructor());
        generator.returnValue();
        generator.endMethod();
    }

    private void generateConfigureInjectorMethod(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, CONFIGURE_INJECTOR_METHOD);
        generator.visitCode();

        generator.loadArg(1);
        generator.checkCast(module.getModuleType());
        final int moduleLocal = generator.newLocal(module.getModuleType());
        generator.storeLocal(moduleLocal);

        for (final ProviderDescriptor provider : module.getProviders()) {
            generateRegisterProviderInvocation(generator, provider, moduleLocal);
        }

        generator.returnValue();
        generator.endMethod();
    }

    private void generateRegisterProviderInvocation(final GeneratorAdapter generator,
            final ProviderDescriptor provider, final int moduleLocal) {
        generator.loadArg(0);
        generateKeyConstruction(generator, provider);

        if (provider.getDelegatorType() != null) {
            generateDelegatorConstruction(generator, provider, moduleLocal);
        } else {
            generateProviderConstruction(generator, provider, moduleLocal);
        }

        generator.invokeVirtual(LightsaberTypes.LIGHTSABER_INJECTOR_TYPE, REGISTER_PROVIDER_METHOD);
    }

    private void generateKeyConstruction(final GeneratorAdapter generator, final ProviderDescriptor provider) {
        generator.newInstance(Types.KEY_TYPE);
        generator.dup();
        final QualifiedType providableType = provider.getQualifiedProvidableType();
        final PackageInvaderDescriptor packageInvader =
                processorContext.findPackageInvaderByTargetType(module.getModuleType());
        final FieldDescriptor classField = packageInvader.getClassField(Types.box(providableType.getType()));
        Validate.notNull(classField, "Cannot find class field for type: %s", providableType.getType());

        generator.getStatic(packageInvader.getType(), classField);
        final AnnotationData qualifier = providableType.getQualifier();
        if (qualifier == null) {
            generator.pushNull();
        } else {
            annotationCreator.newAnnotation(generator, qualifier);
        }
        generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR);
    }

    private void generateDelegatorConstruction(final GeneratorAdapter generator, final ProviderDescriptor provider,
            final int moduleLocal) {
        final Type delegatorType = provider.getDelegatorType();
        generator.newInstance(delegatorType);
        generator.dup();
        generateProviderConstruction(generator, provider, moduleLocal);
        generator.invokeConstructor(delegatorType, DELEGATE_PROVIDER_CONSTRUCTOR);
    }

    private void generateProviderConstruction(final GeneratorAdapter generator, final ProviderDescriptor provider,
            final int moduleLocal) {
        generator.newInstance(provider.getProviderType());
        generator.dup();
        generator.loadLocal(moduleLocal);
        generator.loadArg(0);
        final MethodDescriptor constructor =
                MethodDescriptor.forConstructor(provider.getModuleType(), Types.INJECTOR_TYPE);
        generator.invokeConstructor(provider.getProviderType(), constructor);
    }
}
