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
import com.michaelrocks.lightsaber.processor.FieldDescriptor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

class InjectionVisitor extends ProducingClassVisitor {
    private String className;
    private final List<FieldDescriptor> injectableFields = new ArrayList<>();
    private boolean shouldGenerateInjector;

    public InjectionVisitor(final ClassVisitor classVisitor, final ClassProducer classProducer) {
        super(classVisitor, classProducer);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
            final Object value) {
        final String fieldName = name;
        final String fieldDesc = desc;
        final FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature, value);
        return new FieldVisitor(ASM5, fieldVisitor) {
            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                if (Type.getDescriptor(Inject.class).equals(desc)) {
                    injectableFields.add(new FieldDescriptor(fieldName, fieldDesc));
                }
                return super.visitAnnotation(desc, visible);
            }
        };
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
                if (Type.getInternalName(Injector.class).equals(owner)  && "injectMembers".equals(name)) {
                    System.out.println("Injecting at: " + className + "." + methodName + methodDesc);
                    final String newMethodDesc =
                            Type.getMethodDescriptor(
                                    Type.VOID_TYPE, Type.getType(Injector.class), Type.getObjectType(className));
                    super.visitMethodInsn(
                            INVOKESTATIC, getInjectorType().getInternalName(), name, newMethodDesc, false);
                    shouldGenerateInjector = true;
                } else {
                    super.visitMethodInsn(opcode, owner, name, desc, itf);
                }
            }
        };
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        if (shouldGenerateInjector) {
            final Type injectorType = getInjectorType();
            final InjectorClassGenerator generator =
                    new InjectorClassGenerator(injectorType, Type.getObjectType(className), injectableFields);
            final byte[] injectorClassData = generator.generate();
            produceClass(injectorType.getInternalName(), injectorClassData);
        }
    }

    private Type getInjectorType() {
        return Type.getObjectType(className + "$$Injector");
    }
}
