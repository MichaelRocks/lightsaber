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
import io.michaelrocks.lightsaber.internal.TypeAgent;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.JavaVersionChanger;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.commons.SimpleRemapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.objectweb.asm.Opcodes.*;

public class InjectorFactoryClassGenerator {
    private static final String REGISTER_TYPE_AGENT_METHOD_NAME = "registerTypeAgent";

    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    public InjectorFactoryClassGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generateInjectorFactory() {
        try {
            final ClassReader classReader = new ClassReader(readClassBytes(Lightsaber$$InjectorFactory.class));
            final ClassWriter classWriter = new StandaloneClassWriter(
                    classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, processorContext);
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

    private byte[] readClassBytes(final Class<?> targetClass) throws IOException {
        final String path = targetClass.getName().replace('.', '/') + ".class";
        final URL url = targetClass.getClassLoader().getResource(path);
        if (url == null) {
            throw new FileNotFoundException("Class not found: " + targetClass);
        }

        final URLConnection connection = url.openConnection();
        if (connection instanceof JarURLConnection) {
            final JarURLConnection jarConnection = (JarURLConnection) connection;
            final URL jarFileUrl = jarConnection.getJarFileURL();
            final String entryName = jarConnection.getEntryName();
            try (final JarFile jarFile = new JarFile(jarFileUrl.getFile())) {
                final ZipEntry entry = jarFile.getEntry(entryName);
                if (entry == null) {
                    throw new FileNotFoundException("JAR entry " + entryName + " not found in " + jarFileUrl);
                }

                try (final InputStream stream = jarFile.getInputStream(entry)) {
                    return IOUtils.toByteArray(stream);
                }
            }
        } else {
            try (final InputStream stream = connection.getInputStream()) {
                return IOUtils.toByteArray(stream);
            }
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
            if ("populateTypeAgents".equals(name)) {
                patchPopulateTypeAgentsMethod(methodVisitor);
                return null;
            } else if ("getPackageModules".equals(name)) {
                patchGetPackageModulesMethod(methodVisitor);
                return null;
            } else {
                return methodVisitor;
            }
        }

        private void patchPopulateTypeAgentsMethod(final MethodVisitor methodVisitor) {
            methodVisitor.visitCode();
            final MethodDescriptor registerTypeAgentMethodDescriptor =
                    MethodDescriptor.forMethod(REGISTER_TYPE_AGENT_METHOD_NAME,
                            Type.VOID_TYPE, Type.getType(TypeAgent.class));
            for (final InjectorDescriptor injector : processorContext.getInjectors()) {
                methodVisitor.visitTypeInsn(NEW, injector.getInjectorType().getInternalName());
                methodVisitor.visitInsn(DUP);
                methodVisitor.visitMethodInsn(
                        INVOKESPECIAL,
                        injector.getInjectorType().getInternalName(),
                        MethodDescriptor.forDefaultConstructor().getName(),
                        MethodDescriptor.forDefaultConstructor().getDescriptor(),
                        false);
                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        Type.getInternalName(Lightsaber$$InjectorFactory.class),
                        registerTypeAgentMethodDescriptor.getName(),
                        registerTypeAgentMethodDescriptor.getDescriptor(),
                        false);
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
