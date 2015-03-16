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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class ModuleVisitor extends ClassVisitor {
    private final List<String> providerMethods = new ArrayList<>();

    public ModuleVisitor(final ClassVisitor classVisitor) {
        super(ASM5, classVisitor);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        final String[] newInterfaces = new String[interfaces.length + 1];
        System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);
        newInterfaces[interfaces.length] = InternalNames.CLASS_INTERNAL_MODULE;
        super.visit(version, access, name, signature, superName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final String methodName = name;
        final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(ASM5, methodVisitor) {
            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                if (Descriptors.CLASS_PROVIDES.equals(desc)) {
                    providerMethods.add(methodName);
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
        final MethodVisitor methodVisitor = cv.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "configureInjector",
                "(Lcom/michaelrocks/lightsaber/internal/LightsaberInjector;)V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitLdcInsn(Type.getType("Lcom/michaelrocks/lightsaber/sample/Wookiee;"));
        methodVisitor.visitTypeInsn(NEW, "com/michaelrocks/lightsaber/sample/LightsaberModule$1");
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(INVOKESPECIAL,
                "com/michaelrocks/lightsaber/sample/LightsaberModule$1",
                "<init>",
                "(Lcom/michaelrocks/lightsaber/sample/LightsaberModule;)V",
                false);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL,
                "com/michaelrocks/lightsaber/internal/LightsaberInjector",
                "registerProvider",
                "(Ljava/lang/Class;Ljavax/inject/Provider;)V",
                false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }
}
