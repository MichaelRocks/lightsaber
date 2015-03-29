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

import com.michaelrocks.lightsaber.processor.analysis.AnalysisClassFileVisitor;
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import com.michaelrocks.lightsaber.processor.generation.ClassProducer;
import com.michaelrocks.lightsaber.processor.generation.GlobalModuleGenerator;
import com.michaelrocks.lightsaber.processor.generation.ProcessorClassProducer;
import com.michaelrocks.lightsaber.processor.generation.ProviderGenerator;
import com.michaelrocks.lightsaber.processor.graph.CycleSearcher;
import com.michaelrocks.lightsaber.processor.graph.DependencyGraph;
import com.michaelrocks.lightsaber.processor.graph.UnresolvedDependenciesSearcher;
import com.michaelrocks.lightsaber.processor.injection.InjectionClassFileVisitor;
import com.michaelrocks.lightsaber.processor.io.ClassFileReader;
import com.michaelrocks.lightsaber.processor.io.ClassFileWriter;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ClassProcessor {
    private final ClassFileReader classFileReader;
    private final ClassFileWriter classFileWriter;

    private final ProcessorContext processorContext = new ProcessorContext();

    public ClassProcessor(final ClassFileReader classFileReader, final ClassFileWriter classFileWriter) {
        this.classFileWriter = classFileWriter;
        this.classFileReader = classFileReader;
    }

    public void processClasses() throws IOException {
        performAnalysis();
        processorContext.dump();
        validateDependencyGraph();
        generateGlobalModule();
        generateProviders();
        patchInjectorCreation();
        patchInjection();
    }

    private void performAnalysis() throws IOException {
        final AnalysisClassFileVisitor analysisVisitor = new AnalysisClassFileVisitor(processorContext);
        classFileReader.accept(analysisVisitor);
        checkErrors();
    }

    private void validateDependencyGraph() throws ProcessingException {
        final DependencyGraph dependencyGraph = new DependencyGraph(processorContext);

        final UnresolvedDependenciesSearcher unresolvedDependenciesSearcher =
                new UnresolvedDependenciesSearcher(dependencyGraph);
        final Collection<Type> unresolvedDependencies = unresolvedDependenciesSearcher.findUnresolvedDependencies();
        for (final Type unresolvedDependency : unresolvedDependencies) {
            processorContext.reportError(
                    new ProcessingException("Unresolved dependency: " + unresolvedDependency));
        }

        final CycleSearcher cycleSearcher = new CycleSearcher(dependencyGraph);
        final Collection<Type> cycles = cycleSearcher.findCycles();
        for (final Type cycle : cycles) {
            processorContext.reportError(
                    new ProcessingException("Cycled dependency: " + cycle));
        }

        checkErrors();
    }

    private void generateGlobalModule() throws ProcessingException {
        final ClassProducer classProducer = new ProcessorClassProducer(classFileWriter, processorContext);
        final GlobalModuleGenerator globalModuleGenerator = new GlobalModuleGenerator(classProducer, processorContext);
        globalModuleGenerator.generateGlobalModule();
        final ModuleDescriptor globalModule = globalModuleGenerator.getGlobalModuleDescriptor();
        processorContext.addModule(globalModule);
        checkErrors();
    }

    private void generateProviders() throws ProcessingException {
        final ClassProducer classProducer = new ProcessorClassProducer(classFileWriter, processorContext);
        final ProviderGenerator providerGenerator = new ProviderGenerator(classProducer, processorContext);
        providerGenerator.generateProviders();
        checkErrors();
    }

    private void patchInjectorCreation() {
        // TODO: Implement.
    }

    private void patchInjection() throws IOException {
        // TODO: Implement.

        final InjectionClassFileVisitor injectionVisitor = new InjectionClassFileVisitor(classFileWriter);
        classFileReader.accept(injectionVisitor);
    }

    private void checkErrors() throws ProcessingException {
        if (processorContext.hasErrors()) {
            throw new ProcessingException(composeErrorMessage());
        }
    }

    private String composeErrorMessage() {
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
