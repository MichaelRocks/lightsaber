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

package com.michaelrocks.lightsaber.processor;

import com.michaelrocks.lightsaber.Provides;
import com.michaelrocks.lightsaber.internal.InternalModule;
import com.michaelrocks.lightsaber.internal.LightsaberInjector;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class ModuleVisitor extends ClassVisitor {
    private String className;
    private final List<ProviderMethodDescriptor> providerMethods = new ArrayList<>();

    public ModuleVisitor(final ClassVisitor classVisitor) {
        super(ASM5, classVisitor);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        className = name;
        final String[] newInterfaces = new String[interfaces.length + 1];
        System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
        newInterfaces[interfaces.length] = Type.getInternalName(InternalModule.class);
        super.visit(version, access, name, signature, superName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final String methodName = name;
        final String methodDesc = desc;
        final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(ASM5, methodVisitor) {
            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                if (Type.getDescriptor(Provides.class).equals(desc)) {
                    final Type methodType = Type.getMethodType(methodDesc);
                    final ProviderMethodDescriptor descriptor = new ProviderMethodDescriptor(methodName, methodType);
                    providerMethods.add(descriptor);
                }

                return super.visitAnnotation(desc, visible);
            }
        };
    }

    @Override
    public void visitEnd() {
        generateConfigureInjectorMethod();
        super.visitEnd();
    }

    private void generateConfigureInjectorMethod() {
        System.out.println("Generating configureInjector");
        final MethodVisitor methodVisitor = cv.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC,
                "configureInjector",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(LightsaberInjector.class)),
                null,
                null);
        methodVisitor.visitCode();
        for (int i = 0; i < providerMethods.size(); ++i) {
            final ProviderMethodDescriptor descriptor = providerMethods.get(i);
            generateRegisterProviderInvocation(methodVisitor, descriptor, i);
        }
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateRegisterProviderInvocation(final MethodVisitor methodVisitor,
            final ProviderMethodDescriptor descriptor, final int invocationIndex) {
        System.out.println("Generating invocation for method " + descriptor.name);
        final String providerClass = className + "$$Provider$$" + (invocationIndex + 1);
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitLdcInsn(descriptor.type.getReturnType());
        methodVisitor.visitTypeInsn(NEW, providerClass);
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL,
                providerClass,
                "<init>",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getObjectType(className)),
                false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                Type.getInternalName(LightsaberInjector.class),
                "registerProvider",
                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Class.class), Type.getType(Provider.class)),
                false);
    }

    private class ProviderMethodDescriptor {
        public final String name;
        public final Type type;

        public ProviderMethodDescriptor(final String name, final Type type) {
            this.name = name;
            this.type = type;
        }
    }
}
