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

package com.michaelrocks.lightsaber.processor.generation;

import com.michaelrocks.lightsaber.internal.Lightsaber$$InjectorFactory;
import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class InjectorFactoryClassGenerator {
    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    public InjectorFactoryClassGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generateInjectorFactory() {
        final String path = Lightsaber$$InjectorFactory.class.getSimpleName() + ".class";
        try (final InputStream stream = Lightsaber$$InjectorFactory.class.getResourceAsStream(path)) {
            final ClassReader classReader = new ClassReader(stream);
            final ClassWriter classWriter =
                    new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            classReader.accept(
                    new InjectorFactoryClassVisitor(classWriter), ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            final byte[] classData = classWriter.toByteArray();
            classProducer.produceClass(processorContext.getInjectorFactoryType().getInternalName(), classData);
        } catch (final IOException exception) {
            processorContext.reportError(exception);
        }
    }

    private class InjectorFactoryClassVisitor extends ClassVisitor {
        public InjectorFactoryClassVisitor(final ClassVisitor classVisitor) {
            super(ASM5, classVisitor);
        }

        @Override
        public void visit(final int version, final int access, final String name, final String signature,
                final String superName, final String[] interfaces) {
            final String newName = processorContext.getInjectorFactoryType().getInternalName();
            super.visit(V1_6, access, newName, signature, superName, interfaces);
        }

        @Override
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                final String[] exceptions) {
            final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            if ("populateTypeInjectors".equals(name)) {
                patchPopulateTypeInjectorsMethod(methodVisitor);
                return null;
            } else {
                return methodVisitor;
            }
        }

        private void patchPopulateTypeInjectorsMethod(final MethodVisitor methodVisitor) {
            methodVisitor.visitCode();
            final Type objectType = Type.getType(Object.class);
            final MethodDescriptor putMethodDescriptor =
                    MethodDescriptor.forMethod("put", objectType, objectType, objectType);
            for (final InjectorDescriptor injector : processorContext.getInjectors()) {
                methodVisitor.visitFieldInsn(
                        GETSTATIC,
                        Type.getInternalName(Lightsaber$$InjectorFactory.class),
                        "typeInjectors",
                        Type.getDescriptor(Map.class));
                methodVisitor.visitLdcInsn(injector.getInjectableTarget().getTargetType());
                methodVisitor.visitTypeInsn(NEW, injector.getInjectorType().getInternalName());
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitMethodInsn(
                        INVOKESPECIAL,
                        injector.getInjectorType().getInternalName(),
                        MethodDescriptor.forDefaultConstructor().getName(),
                        MethodDescriptor.forDefaultConstructor().getDescriptor(),
                        false);
                methodVisitor.visitMethodInsn(
                        INVOKEINTERFACE,
                        Type.getInternalName(Map.class),
                        putMethodDescriptor.getName(),
                        putMethodDescriptor.getDescriptor(),
                        true);
                methodVisitor.visitInsn(POP);
            }

            methodVisitor.visitInsn(RETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }
    }
}
