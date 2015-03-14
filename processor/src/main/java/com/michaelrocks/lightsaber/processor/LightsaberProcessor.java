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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class LightsaberProcessor {
    private final LightsaberParameters parameters;

    private LightsaberProcessor(final LightsaberParameters parameters) {
        this.parameters = parameters;
    }

    public static void main(final String[] args) {
        final LightsaberParameters parameters = new LightsaberParameters();
        final JCommander parser = new JCommander(parameters);

        try {
            parser.parse(args);
        } catch (final ParameterException exception) {
            System.err.println("Cannot parse arguments");
            final StringBuilder builder = new StringBuilder();
            parser.usage(builder);
            System.err.println(builder.toString());
            System.exit(1);
        }

        final LightsaberProcessor processor = new LightsaberProcessor(parameters);
        if (!processor.process()) {
            System.exit(2);
        }
    }

    public boolean process() {
        try {
            for (final String jar : parameters.jars) {
                final File jarFile = new File(jar);
                processJarFileWithCopy(jarFile);
            }
            return true;
        } catch (final ProcessingException exception) {
            if (parameters.printStacktrace) {
                exception.printStackTrace();
            }
            return false;
        }
    }

    private void processJarFileWithCopy(final File file) throws ProcessingException {
        final File lightsaberCopy = composeFileCopy(file, "lightsabered");
        final File originalCopy = composeFileCopy(file, "original");
        try {
            processJarFile(file, lightsaberCopy);
            Files.move(file.toPath(), originalCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.move(lightsaberCopy.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (final Exception exception) {
            // noinspection ResultOfMethodCallIgnored
            lightsaberCopy.delete();
        }
    }

    private File composeFileCopy(final File file, final String copyName) {
        final File parentFile = file.getParentFile();
        final String fileNameWithExtension = file.getName();
        final int extensionIndex = fileNameWithExtension.lastIndexOf('.');
        final String fileName =
                extensionIndex < 0 ? fileNameWithExtension : fileNameWithExtension.substring(0, extensionIndex);
        final String extension =
                extensionIndex < 0 ? "" : fileNameWithExtension.substring(extensionIndex);
        return new File(parentFile, fileName + '.' + copyName + extension);
    }

    private void processJarFile(final File sourceFile, final File targetFile) throws ProcessingException {
        System.out.println("Processing: " + sourceFile.getAbsolutePath());

        JarFile jarFile = null;
        JarOutputStream outputStream = null;
        try {
            jarFile = new JarFile(sourceFile, true);
            outputStream = new JarOutputStream(new FileOutputStream(targetFile));
            processJarFile(jarFile, outputStream);
        } catch (final IOException exception) {
            throw new ProcessingException(sourceFile, exception);
        } finally {
            close(outputStream);
            close(jarFile);
        }
    }

    private void processJarFile(final JarFile jarFile, final JarOutputStream jarOutputStream) throws IOException {
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            jarOutputStream.putNextEntry(new JarEntry(entry.getName()));
            try (final InputStream entryStream = jarFile.getInputStream(entry)) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    processClass(entryStream, jarOutputStream);
                } else {
                    final byte[] buffer = new byte[1024];
                    while (true) {
                        final int length = entryStream.read(buffer);
                        if (length <= 0) {
                            break;
                        }
                        jarOutputStream.write(buffer, 0, length);
                    }
                }
            }
            jarOutputStream.closeEntry();
        }
    }

    private void processClass(final InputStream stream, final OutputStream outputStream) throws IOException {
        final ClassReader classReader = new ClassReader(stream);
        final ClassWriter classWriter =
                new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classReader.accept(new InjectionVisitor(classWriter), ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
        outputStream.write(classWriter.toByteArray());
    }

    private void close(final Closeable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (final IOException ignored) {
            }
        }
    }
}
