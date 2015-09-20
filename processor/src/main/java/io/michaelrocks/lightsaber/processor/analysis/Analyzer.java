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

package io.michaelrocks.lightsaber.processor.analysis;

import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.CompositeClassVisitor;
import io.michaelrocks.lightsaber.processor.graph.TypeGraphBuilder;
import io.michaelrocks.lightsaber.processor.io.ClassFileReader;
import io.michaelrocks.lightsaber.processor.io.ClassFileVisitor;
import io.michaelrocks.lightsaber.processor.io.DirectoryClassFileTraverser;
import io.michaelrocks.lightsaber.processor.io.JarClassFileTraverser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Analyzer {
    private final ProcessorContext processorContext;
    private final TypeGraphBuilder typeGraphBuilder = new TypeGraphBuilder();

    public Analyzer(final ProcessorContext processorContext) {
        this.processorContext = processorContext;
    }

    public void analyze(final ClassFileReader classFileReader, final List<File> libraries) throws IOException {
        analyzeTypes(classFileReader, libraries);
        analyzeInjectionTargets(classFileReader);
    }

    private void analyzeTypes(final ClassFileReader classFileReader, final List<File> libraries) throws IOException {
        final CompositeClassVisitor compositeClassVisitor = new CompositeClassVisitor();
        compositeClassVisitor.addVisitor(typeGraphBuilder);
        compositeClassVisitor.addVisitor(new AnnotationAnalysisDispatcher(processorContext));
        analyzeLibraries(libraries, compositeClassVisitor);
        analyzeClassesFromReader(classFileReader, compositeClassVisitor);
        processorContext.setTypeGraph(typeGraphBuilder.build());
    }

    private void analyzeInjectionTargets(final ClassFileReader classFileReader) throws IOException {
        analyzeClassesFromReader(classFileReader, new InjectionAnalysisDispatcher(processorContext));
    }

    private void analyzeLibraries(final List<File> libraries, final ClassVisitor classVisitor) throws IOException {
        for (final File library : libraries) {
            if (library.isDirectory()) {
                analyzeClassesFromDirectory(library, classVisitor);
            } else {
                analyzeClassesFromJar(library, classVisitor);
            }
        }
    }

    private void analyzeClassesFromJar(final File jarFile, final ClassVisitor classVisitor) throws IOException {
        try (final ClassFileReader classFileReader = new ClassFileReader(new JarClassFileTraverser(jarFile))) {
            analyzeClassesFromReader(classFileReader, classVisitor);
        } catch (final Exception exception) {
            throw new IOException(exception);
        }
    }

    private void analyzeClassesFromDirectory(final File classesDir, final ClassVisitor classVisitor) throws IOException {
        try (final ClassFileReader classFileReader = new ClassFileReader(new DirectoryClassFileTraverser(classesDir))) {
            analyzeClassesFromReader(classFileReader, classVisitor);
        } catch (final Exception exception) {
            throw new IOException(exception);
        }
    }

    private void analyzeClassesFromReader(final ClassFileReader classFileReader, final ClassVisitor classVisitor)
            throws IOException {
        classFileReader.accept(new ClassFileVisitor(null) {
            @Override
            public void visitClassFile(final String path, final byte[] classData) throws IOException {
                processorContext.setClassFilePath(path);
                try {
                    final ClassReader classReader = new ClassReader(classData);
                    classReader.accept(classVisitor,
                            ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
                    super.visitClassFile(path, classData);
                } finally {
                    processorContext.setClassFilePath(null);
                }
            }
        });
    }
}
