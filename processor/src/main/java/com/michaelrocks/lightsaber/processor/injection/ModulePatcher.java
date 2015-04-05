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

package com.michaelrocks.lightsaber.processor.injection;

import com.michaelrocks.lightsaber.Injector;
import com.michaelrocks.lightsaber.internal.InternalModule;
import com.michaelrocks.lightsaber.internal.LightsaberInjector;
import com.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Provider;

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
        System.out.println("Generating invocation for method " + provider.getProviderMethod().getName());

        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitLdcInsn(provider.getProvidableType());
        methodVisitor.visitTypeInsn(NEW, provider.getProviderType().getInternalName());
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitMethodInsn(INVOKESPECIAL,
                provider.getProviderType().getInternalName(),
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, provider.getModuleType(), Type.getType(Injector.class)),
                false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                Type.getInternalName(LightsaberInjector.class),
                "registerProvider",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Class.class), Type.getType(Provider.class)),
                false);
    }
}
