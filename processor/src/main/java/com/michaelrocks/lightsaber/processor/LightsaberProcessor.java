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
            validateParameters(parameters);
        } catch (final ParameterException exception) {
            System.err.println(exception.getMessage());
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

    private static void validateParameters(final LightsaberParameters parameters) {
        if (parameters.jar == null && parameters.classes == null) {
            throw new ParameterException("Either --jar or --classes must be specified");
        }
        if (parameters.jar != null && parameters.classes != null) {
            throw new ParameterException("Either --jar or --classes can be specified but not both");
        }
    }

    public boolean process() {
        try {
            if (parameters.jar != null) {
                final File jarFile = new File(parameters.jar);
                processJarFile(jarFile);
            } else if (parameters.classes != null) {
                final File classesDirectory = new File(parameters.classes);
                processClasses(classesDirectory);
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

    private void processJarFile(final File file) throws Exception {
        final File processedFile = composeCopyName(file, "lightsaber");
        try (
            final ClassFileReader<?> classFileReader = new JarClassFileReader(file);
            final ClassFileWriter classFileWriter = new JarClassFileWriter(processedFile)
        ) {
            processClassFiles(classFileReader, classFileWriter);
        }
    }

    private void processClasses(final File directory) throws Exception {
        final File processedDirectory = composeCopyName(directory, "lightsaber");
        FileUtils.deleteQuietly(processedDirectory);
        if (!processedDirectory.mkdirs()) {
            throw new ProcessingException("Failed to create output directory " + processedDirectory);
        }

        try (
            final ClassFileReader<?> classFileReader = new DirectoryClassFileReader(directory);
            final ClassFileWriter classFileWriter = new DirectoryClassFileWriter(processedDirectory)
        ) {
            processClassFiles(classFileReader, classFileWriter);
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
        final ClassProcessor classProcessor = new ClassProcessor(classFileReader, classFileWriter);
        classProcessor.processClasses();
    }
}
