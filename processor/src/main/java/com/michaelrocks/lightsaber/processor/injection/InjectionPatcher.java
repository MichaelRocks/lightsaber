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
import com.michaelrocks.lightsaber.Lightsaber;
import com.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import com.michaelrocks.lightsaber.processor.ProcessorContext;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

class InjectionPatcher extends ProcessorClassVisitor {
    private String className;

    public InjectionPatcher(final ProcessorContext processorContext, final ClassVisitor classVisitor) {
        super(processorContext, classVisitor);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final String methodName = name;
        final String methodDesc = desc;
        final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(ASM5, methodVisitor) {
            @Override
            public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc,
                    final boolean itf) {
                if (Type.getInternalName(Injector.class).equals(owner) && "injectMembers".equals(name)) {
                    System.out.println("Injecting at: " + className + "." + methodName + methodDesc);
                    final String newOwner = getInjectorType().getInternalName();
                    final String newMethodDesc =
                            Type.getMethodDescriptor(
                                    Type.VOID_TYPE, Type.getType(Injector.class), Type.getObjectType(className));
                    super.visitMethodInsn(INVOKESTATIC, newOwner, name, newMethodDesc, false);
                } else if (Type.getInternalName(Lightsaber.class).equals(owner) && "createInjector".equals(name)) {
                    final String newOwner = getProcessorContext().getInjectorFactoryType().getInternalName();
                    super.visitMethodInsn(INVOKESTATIC, newOwner, name, desc, false);
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            }
        };
    }

    private Type getInjectorType() {
        // TODO: Injector type must be retrieved from InjectorsGenerator or ProcessorContext.
        return Type.getObjectType(className + "$$Injector");
    }
}
