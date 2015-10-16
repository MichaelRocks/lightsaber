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

package io.michaelrocks.lightsaber.processor.injection;

import io.michaelrocks.lightsaber.InjectorConfigurator;
import io.michaelrocks.lightsaber.LightsaberInjector;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedFieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedType;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class ModulePatcher extends BaseInjectionClassVisitor {
    private static final Type LIGHTSABER_INJECTOR_TYPE = Type.getType(LightsaberInjector.class);

    private static final MethodDescriptor KEY_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE);
    private static final MethodDescriptor CONFIGURE_INJECTOR_METHOD =
            MethodDescriptor.forMethod("configureInjector", Type.VOID_TYPE, LIGHTSABER_INJECTOR_TYPE);
    private static final MethodDescriptor REGISTER_PROVIDER_METHOD =
            MethodDescriptor.forMethod("registerProvider", Type.VOID_TYPE, Types.KEY_TYPE, Types.PROVIDER_TYPE);

    private static final MethodDescriptor FIELD_PROVIDER_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.OBJECT_TYPE);
    private static final MethodDescriptor DELEGATE_PROVIDER_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.PROVIDER_TYPE);

    private static final Logger logger = LoggerFactory.getLogger(ModulePatcher.class);

    private final AnnotationCreator annotationCreator;
    private final ModuleDescriptor module;

    private final Set<String> providableFields;
    private final Set<MethodDescriptor> providableMethods;

    public ModulePatcher(final ProcessorContext processorContext, final ClassVisitor classVisitor,
            final AnnotationCreator annotationCreator, final ModuleDescriptor module) {
        super(processorContext, classVisitor);
        this.annotationCreator = annotationCreator;
        this.module = module;

        providableFields = new HashSet<>(module.getProviders().size());
        providableMethods = new HashSet<>(module.getProviders().size());
        for (final ProviderDescriptor provider : module.getProviders()) {
            if (provider.getProviderField() != null) {
                providableFields.add(provider.getProviderField().getName());
            }
            if (provider.getProviderMethod() != null) {
                providableMethods.add(provider.getProviderMethod().getMethod());
            }
        }
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        final String[] newInterfaces = new String[interfaces.length + 1];
        System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
        newInterfaces[interfaces.length] = Type.getInternalName(InjectorConfigurator.class);
        super.visit(version, access, name, signature, superName, newInterfaces);
        setDirty(true);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
            final Object value) {
        if (providableFields.contains(name)) {
            final int newAccess = access & ~ACC_PRIVATE;
            return super.visitField(newAccess, name, desc, signature, value);
        } else {
            return super.visitField(access, name, desc, signature, value);
        }
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final MethodDescriptor method = new MethodDescriptor(name, desc);
        if (providableMethods.contains(method)) {
            final int newAccess = access & ~ACC_PRIVATE;
            return super.visitMethod(newAccess, name, desc, signature, exceptions);
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }

    @Override
    public void visitEnd() {
        generateConfigureInjectorMethod();
        super.visitEnd();
    }

    private void generateConfigureInjectorMethod() {
        logger.debug("Generating configureInjector for module {}", module.getModuleType().getInternalName());
        final GeneratorAdapter generator = new GeneratorAdapter(this, ACC_PUBLIC, CONFIGURE_INJECTOR_METHOD);
        generator.visitCode();
        for (final ProviderDescriptor provider : module.getProviders()) {
            generateRegisterProviderInvocation(generator, provider);
        }
        generator.returnValue();
        generator.endMethod();
    }

    private void generateRegisterProviderInvocation(final GeneratorAdapter generator,
            final ProviderDescriptor provider) {
        generator.loadArg(0);
        generateKeyConstruction(generator, provider);

        if (provider.getProviderField() != null) {
            generateProviderConstructionForField(generator, provider);
        } else {
            generateProviderConstructionForMethod(generator, provider);
        }

        generator.invokeVirtual(LIGHTSABER_INJECTOR_TYPE, REGISTER_PROVIDER_METHOD);
    }

    private void generateKeyConstruction(final GeneratorAdapter generator, final ProviderDescriptor provider) {
        generator.newInstance(Types.KEY_TYPE);
        generator.dup();
        final QualifiedType providableType = provider.getQualifiedProvidableType();
        generator.push(Types.box(providableType.getType()));
        final AnnotationData qualifier = providableType.getQualifier();
        if (qualifier == null) {
            generator.pushNull();
        } else {
            annotationCreator.newAnnotation(generator, qualifier);
        }
        generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR);
    }

    private void generateProviderConstructionForField(final GeneratorAdapter generator,
            final ProviderDescriptor provider) {
        logger.debug("Generating invocation for field {}", provider.getProviderField().getName());

        generator.newInstance(provider.getProviderType());
        generator.dup();
        generator.loadThis();
        final QualifiedFieldDescriptor fieldDescriptor = provider.getProviderField();
        generator.getField(module.getModuleType(), fieldDescriptor.getField());
        generator.invokeConstructor(provider.getProviderType(), FIELD_PROVIDER_CONSTRUCTOR);
    }

    private void generateProviderConstructionForMethod(final GeneratorAdapter generator,
            final ProviderDescriptor provider) {
        logger.debug("Generating invocation for method {}", provider.getProviderMethod().getName());

        if (provider.getDelegatorType() != null) {
            generateDelegatorConstruction(generator, provider);
        } else {
            generateProviderConstruction(generator, provider);
        }
    }

    private void generateDelegatorConstruction(final GeneratorAdapter generator, final ProviderDescriptor provider) {
        final Type delegatorType = provider.getDelegatorType();
        generator.newInstance(delegatorType);
        generator.dup();
        generateProviderConstruction(generator, provider);
        generator.invokeConstructor(delegatorType, DELEGATE_PROVIDER_CONSTRUCTOR);
    }

    private void generateProviderConstruction(final GeneratorAdapter generator, final ProviderDescriptor provider) {
        generator.newInstance(provider.getProviderType());
        generator.dup();
        generator.loadThis();
        generator.loadArg(0);
        final MethodDescriptor constructor =
                MethodDescriptor.forConstructor(provider.getModuleType(), Types.INJECTOR_TYPE);
        generator.invokeConstructor(provider.getProviderType(), constructor);
    }
}
