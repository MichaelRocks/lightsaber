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

import io.michaelrocks.lightsaber.Module;
import io.michaelrocks.lightsaber.internal.Lightsaber$$InjectorFactory;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.JavaVersionChanger;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
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
                    new StandaloneClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
            final ClassVisitor generator = new InjectorFactoryClassVisitor(classWriter);
            final Remapper remapper =
                    new SimpleRemapper(
                            Type.getInternalName(Lightsaber$$InjectorFactory.class),
                            processorContext.getInjectorFactoryType().getInternalName());
            final ClassVisitor remappingAdapter = new RemappingClassAdapter(generator, remapper);
            final ClassVisitor javaVersionChanger = new JavaVersionChanger(remappingAdapter, V1_6);
            classReader.accept(javaVersionChanger, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
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
        public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
                final String[] exceptions) {
            final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
            if ("populateTypeInjectors".equals(name)) {
                patchPopulateTypeInjectorsMethod(methodVisitor);
                return null;
            } else if ("getPackageModules".equals(name)) {
                patchGetPackageModulesMethod(methodVisitor);
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

        private void patchGetPackageModulesMethod(final MethodVisitor methodVisitor) {
            methodVisitor.visitCode();
            final Collection<ModuleDescriptor> packageModules = processorContext.getPackageModules();
            methodVisitor.visitIntInsn(BIPUSH, packageModules.size());
            methodVisitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(Module.class));

            int index = 0;
            for (final ModuleDescriptor packageModule : packageModules) {
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitIntInsn(BIPUSH, index);
                methodVisitor.visitTypeInsn(NEW, packageModule.getModuleType().getInternalName());
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitMethodInsn(
                        INVOKESPECIAL,
                        packageModule.getModuleType().getInternalName(),
                        MethodDescriptor.forDefaultConstructor().getName(),
                        MethodDescriptor.forDefaultConstructor().getDescriptor(),
                        false);
                methodVisitor.visitInsn(AASTORE);
                index += 1;
            }

            methodVisitor.visitInsn(ARETURN);
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
        }
    }
}
