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

import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.internal.ConfigurableModule;
import io.michaelrocks.lightsaber.internal.CopyableProvider;
import io.michaelrocks.lightsaber.internal.LightsaberInjector;
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

import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

public class ModulePatcher extends BaseInjectionClassVisitor {
    private static final MethodDescriptor KEY_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE);

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
        newInterfaces[interfaces.length] = Type.getInternalName(ConfigurableModule.class);
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
        System.out.println("Generating configureInjector");
        final MethodDescriptor method =
                MethodDescriptor.forMethod("configureInjector", Type.VOID_TYPE, Type.getType(LightsaberInjector.class));
        final GeneratorAdapter methodVisitor = new GeneratorAdapter(this, ACC_PUBLIC, method, null, null);
        methodVisitor.visitCode();
        for (final ProviderDescriptor provider : module.getProviders()) {
            generateRegisterProviderInvocation(methodVisitor, provider);
        }
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateRegisterProviderInvocation(final GeneratorAdapter methodVisitor,
            final ProviderDescriptor provider) {
        methodVisitor.visitVarInsn(ALOAD, 1);
        generateKeyConstruction(methodVisitor, provider);

        if (provider.getProviderField() != null) {
            generateProviderConstructionForField(methodVisitor, provider);
        } else {
            generateProviderConstructionForMethod(methodVisitor, provider);
        }

        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                Type.getInternalName(LightsaberInjector.class),
                "registerProvider",
                Type.getMethodDescriptor(Type.VOID_TYPE, Types.KEY_TYPE, Type.getType(CopyableProvider.class)),
                false);
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

    private void generateProviderConstructionForField(final MethodVisitor methodVisitor,
            final ProviderDescriptor provider) {
        System.out.println("Generating invocation for field " + provider.getProviderField().getName());

        methodVisitor.visitTypeInsn(NEW, provider.getProviderType().getInternalName());
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        final QualifiedFieldDescriptor fieldDescriptor = provider.getProviderField();
        methodVisitor.visitFieldInsn(
                GETFIELD,
                module.getModuleType().getInternalName(),
                fieldDescriptor.getName(),
                fieldDescriptor.getDescriptor());
        final MethodDescriptor constructorDescriptor = MethodDescriptor.forConstructor(Type.getType(Object.class));
        methodVisitor.visitMethodInsn(INVOKESPECIAL,
                provider.getProviderType().getInternalName(),
                constructorDescriptor.getName(),
                constructorDescriptor.getDescriptor(),
                false);
    }

    private void generateProviderConstructionForMethod(final MethodVisitor methodVisitor,
            final ProviderDescriptor provider) {
        System.out.println("Generating invocation for method " + provider.getProviderMethod().getName());

        if (provider.getDelegatorType() != null) {
            generateDelegatorConstruction(methodVisitor, provider);
        } else {
            generateProviderConstruction(methodVisitor, provider);
        }
    }

    private void generateDelegatorConstruction(final MethodVisitor methodVisitor, final ProviderDescriptor provider) {
        final Type delegatorType = provider.getDelegatorType();
        methodVisitor.visitTypeInsn(NEW, delegatorType.getInternalName());
        methodVisitor.visitInsn(DUP);
        generateProviderConstruction(methodVisitor, provider);
        final MethodDescriptor constructorDescriptor =
                MethodDescriptor.forConstructor(Type.getType(CopyableProvider.class));
        methodVisitor.visitMethodInsn(INVOKESPECIAL,
                delegatorType.getInternalName(),
                constructorDescriptor.getName(),
                constructorDescriptor.getDescriptor(),
                false);
    }

    private void generateProviderConstruction(final MethodVisitor methodVisitor, final ProviderDescriptor provider) {
        methodVisitor.visitTypeInsn(NEW, provider.getProviderType().getInternalName());
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        final MethodDescriptor constructorDescriptor =
                MethodDescriptor.forConstructor(provider.getModuleType(), Type.getType(Injector.class));
        methodVisitor.visitMethodInsn(INVOKESPECIAL,
                provider.getProviderType().getInternalName(),
                constructorDescriptor.getName(),
                constructorDescriptor.getDescriptor(),
                false);
    }
}
