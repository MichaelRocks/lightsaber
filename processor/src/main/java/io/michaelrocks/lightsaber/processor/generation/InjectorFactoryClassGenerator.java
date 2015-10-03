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

import io.michaelrocks.lightsaber.internal.Lightsaber$$InjectorFactory;
import io.michaelrocks.lightsaber.internal.TypeAgent;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.commons.JavaVersionChanger;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor;
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

import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.V1_6;

public class InjectorFactoryClassGenerator {
    private static final MethodDescriptor REGISTER_TYPE_AGENT_METHOD =
            MethodDescriptor.forMethod("registerTypeAgent", Type.VOID_TYPE, Type.getType(TypeAgent.class));

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
            final ClassVisitor watermarkClassVisitor = new WatermarkClassVisitor(classWriter, true);
            final ClassVisitor generator = new InjectorFactoryClassVisitor(watermarkClassVisitor);
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
                patchPopulateTypeAgentsMethod(new GeneratorAdapter(methodVisitor, access, name, desc));
                return null;
            } else if ("getPackageModules".equals(name)) {
                patchGetPackageModulesMethod(new GeneratorAdapter(methodVisitor, access, name, desc));
                return null;
            } else {
                return methodVisitor;
            }
        }

        private void patchPopulateTypeAgentsMethod(final GeneratorAdapter generator) {
            generator.visitCode();
            for (final InjectorDescriptor injector : processorContext.getInjectors()) {
                generator.newInstance(injector.getInjectorType());
                generator.dup();
                generator.invokeConstructor(injector.getInjectorType(), MethodDescriptor.forDefaultConstructor());
                generator.invokeStatic(Type.getType(Lightsaber$$InjectorFactory.class), REGISTER_TYPE_AGENT_METHOD);
            }
            generator.returnValue();
            generator.endMethod();
        }

        private void patchGetPackageModulesMethod(final GeneratorAdapter generator) {
            generator.visitCode();
            final Collection<ModuleDescriptor> packageModules = processorContext.getPackageModules();
            generator.newArray(Types.OBJECT_TYPE, packageModules.size());

            int index = 0;
            for (final ModuleDescriptor packageModule : packageModules) {
                generator.dup();
                generator.push(index);
                generator.newInstance(packageModule.getModuleType());
                generator.dup();
                generator.invokeConstructor(packageModule.getModuleType(), MethodDescriptor.forDefaultConstructor());
                generator.arrayStore(packageModule.getModuleType());
                index += 1;
            }

            generator.returnValue();
            generator.endMethod();
        }
    }
}
