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

import io.michaelrocks.lightsaber.CopyableProvider;
import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.internal.InternalModule;
import io.michaelrocks.lightsaber.internal.LightsaberInjector;
import io.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedFieldDescriptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class ModulePatcher extends ProcessorClassVisitor {
    private final ModuleDescriptor module;

    public ModulePatcher(final ProcessorContext processorContext, final ClassVisitor classVisitor,
            final ModuleDescriptor module) {
        super(processorContext, classVisitor);
        this.module = module;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        final String[] newInterfaces = new String[interfaces.length + 1];
        System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
        newInterfaces[interfaces.length] = Type.getInternalName(InternalModule.class);
        super.visit(version, access, name, signature, superName, newInterfaces);
    }

    @Override
    public void visitEnd() {
        generateConfigureInjectorMethod();
        super.visitEnd();
    }

    private void generateConfigureInjectorMethod() {
        System.out.println("Generating configureInjector");
        final MethodVisitor methodVisitor = cv.visitMethod(ACC_PUBLIC,
                "configureInjector",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(LightsaberInjector.class)),
                null,
                null);
        methodVisitor.visitCode();
        for (final ProviderDescriptor provider : module.getProviders()) {
            generateRegisterProviderInvocation(methodVisitor, provider);
        }
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateRegisterProviderInvocation(final MethodVisitor methodVisitor,
            final ProviderDescriptor provider) {
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitLdcInsn(Types.box(provider.getProvidableType()));

        if (provider.getProviderField() != null) {
            generateProviderConstructionForField(methodVisitor, provider);
        } else {
            generateProviderConstructionForMethod(methodVisitor, provider);
        }

        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                Type.getInternalName(LightsaberInjector.class),
                "registerProvider",
                Type.getMethodDescriptor(Type.VOID_TYPE,
                        Type.getType(Class.class), Type.getType(CopyableProvider.class)),
                false);
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
