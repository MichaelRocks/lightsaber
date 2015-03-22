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
import com.michaelrocks.lightsaber.processor.analysis.AnalysisClassFileVisitor;
import com.michaelrocks.lightsaber.processor.analysis.InjectionTargetDescriptor;
import com.michaelrocks.lightsaber.processor.analysis.ModuleDescriptor;
import com.michaelrocks.lightsaber.processor.io.ClassFileReader;
import com.michaelrocks.lightsaber.processor.io.ClassFileWriter;
import com.michaelrocks.lightsaber.processor.io.DirectoryClassFileReader;
import com.michaelrocks.lightsaber.processor.io.DirectoryClassFileWriter;
import com.michaelrocks.lightsaber.processor.io.JarClassFileReader;
import com.michaelrocks.lightsaber.processor.io.JarClassFileWriter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

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
            if (parameters.jar != null) {
                final File jarFile = new File(parameters.jar);
                processJarFileWithCopy(jarFile);
            } else if (parameters.classes != null) {
                final File classesDirectory = new File(parameters.classes);
                processClassesWithCopy(classesDirectory);
            }
            System.out.println("DONE");
            return true;
        } catch (final Exception exception) {
            System.err.println(exception.getMessage());
            if (parameters.printStacktrace) {
                exception.printStackTrace();
            }
        }
        return false;
    }

    private void processJarFileWithCopy(final File file) throws Exception {
        final File lightsaberCopy = composeCopyName(file, "lightsabered");
        final File originalCopy = composeCopyName(file, "original");
        try (
            final ClassFileReader<?> classFileReader = new JarClassFileReader(file);
            final ClassFileWriter classFileWriter = new JarClassFileWriter(lightsaberCopy)
        ) {
            processClassFiles(classFileReader, classFileWriter);
            Files.move(file.toPath(), originalCopy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.move(lightsaberCopy.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            // noinspection ResultOfMethodCallIgnored
            lightsaberCopy.delete();
        }
    }

    private void processClassesWithCopy(final File directory) throws Exception {
        final File lightsaberCopy = composeCopyName(directory, "lightsabered");
        final File originalCopy = composeCopyName(directory, "original");

        FileUtils.deleteQuietly(lightsaberCopy);
        if (!lightsaberCopy.mkdirs()) {
            throw new ProcessingException("Failed to create output directory " + lightsaberCopy);
        }

        try (
            final ClassFileReader<?> classFileReader = new DirectoryClassFileReader(directory);
            final ClassFileWriter classFileWriter = new DirectoryClassFileWriter(lightsaberCopy)
        ) {
            processClassFiles(classFileReader, classFileWriter);
            FileUtils.deleteQuietly(originalCopy);
            Files.move(directory.toPath(), originalCopy.toPath());
            Files.move(lightsaberCopy.toPath(), directory.toPath());
        } finally {
            // noinspection ResultOfMethodCallIgnored
            FileUtils.deleteQuietly(lightsaberCopy);
        }
    }

    private File composeCopyName(final File file, final String copyName) {
        final File parentFile = file.getParentFile();
        final String fileNameWithExtension = file.getName();
        final String fileName = FilenameUtils.getBaseName(fileNameWithExtension);
        final String extension = FilenameUtils.getExtension(fileNameWithExtension);
        return new File(parentFile, fileName + '-' + copyName + extension);
    }

    private void processClassFiles(final ClassFileReader classFileReader, final ClassFileWriter classFileWriter)
            throws IOException {
        final ProcessorContext processorContext = new ProcessorContext();
        final AnalysisClassFileVisitor analysisVisitor = new AnalysisClassFileVisitor(processorContext);
        classFileReader.accept(analysisVisitor);

        if (parameters.verbose) {
            dumpProcessorContext(processorContext);
        }

        if (processorContext.hasErrors()) {
            throw new ProcessingException(composeErrorMessage(processorContext));
        }

        final InjectionClassFileVisitor injectionVisitor = new InjectionClassFileVisitor(classFileWriter);
        classFileReader.accept(injectionVisitor);
    }

    private void dumpProcessorContext(final ProcessorContext processorContext) {
        for (final ModuleDescriptor module : processorContext.getModules()) {
            System.out.println("Module: " + module.getModuleType());
            for (final MethodDescriptor providerMethod : module.getProviderMethods()) {
                System.out.println("\tProvides: " + providerMethod);
            }
        }
        for (final InjectionTargetDescriptor injectableTarget : processorContext.getInjectableTargets()) {
            System.out.println("Injectable: " + injectableTarget.getTargetType());
            for (final FieldDescriptor injectableField : injectableTarget.getInjectableFields()) {
                System.out.println("\tField: " + injectableField);
            }
            for (final MethodDescriptor injectableMethod : injectableTarget.getInjectableMethods()) {
                System.out.println("\tMethod: " + injectableMethod);
            }
        }
        for (final InjectionTargetDescriptor providableTarget : processorContext.getProvidableTargets()) {
            System.out.println("Providable: " + providableTarget.getTargetType());
            for (final MethodDescriptor injectableConstructor : providableTarget.getInjectableConstructors()) {
                System.out.println("\tConstructor: " + injectableConstructor);
            }
        }
    }

    private String composeErrorMessage(final ProcessorContext processorContext) {
        final StringBuilder builder = new StringBuilder();
        for (final Map.Entry<String, List<Exception>> entry : processorContext.getErrors().entrySet()) {
            final String path = entry.getKey();
            for (final Exception error : entry.getValue()) {
                builder
                        .append(System.lineSeparator())
                        .append(path)
                        .append(": ")
                        .append(error.getMessage());
            }
        }
        return builder.toString();
    }
}
