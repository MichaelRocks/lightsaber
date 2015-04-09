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
import com.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import com.michaelrocks.lightsaber.processor.generation.ClassProducer;
import com.michaelrocks.lightsaber.processor.generation.GlobalModuleGenerator;
import com.michaelrocks.lightsaber.processor.generation.InjectorFactoryClassGenerator;
import com.michaelrocks.lightsaber.processor.generation.InjectorsGenerator;
import com.michaelrocks.lightsaber.processor.generation.ProcessorClassProducer;
import com.michaelrocks.lightsaber.processor.generation.ProvidersGenerator;
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
        composeGlobalModule();
        composeInjectors();
        processorContext.dump();
        validateDependencyGraph();
        generateGlobalModule();
        generateProviders();
        generateInjectorFactory();
        generateInjectors();
        copyAndPatchClasses();
    }

    private void performAnalysis() throws IOException {
        final AnalysisClassFileVisitor analysisVisitor = new AnalysisClassFileVisitor(processorContext);
        classFileReader.accept(analysisVisitor);
        checkErrors();
    }

    private void composeGlobalModule() {
        final ModuleDescriptor.Builder globalModuleBuilder =
                new ModuleDescriptor.Builder(processorContext.getGlobalModuleType());
        for (final InjectionTargetDescriptor providableTarget : processorContext.getProvidableTargets()) {
            final Type providableTargetType = providableTarget.getTargetType();
            final MethodDescriptor providableTargetConstructor = providableTarget.getInjectableConstructor();

            final String providerMethodName = "provide$" + providableTargetType.getInternalName().replace('/', '$');
            final Type[] providerMethodArgumentTypes = providableTargetConstructor.getType().getArgumentTypes();
            final MethodDescriptor providerMethod =
                    MethodDescriptor.forMethod(providerMethodName, providableTargetType, providerMethodArgumentTypes);

            globalModuleBuilder.addProviderMethod(providerMethod, providableTarget.getScope());
        }
        processorContext.setGlobalModule(globalModuleBuilder.build());
    }

    private void composeInjectors() {
        for (final InjectionTargetDescriptor injectableTarget : processorContext.getInjectableTargets()) {
            final Type injectorType =
                    Type.getObjectType(injectableTarget.getTargetType().getInternalName() + "$$Injector");
            final InjectorDescriptor injector = new InjectorDescriptor(injectorType, injectableTarget);
            processorContext.addInjector(injector);
        }
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
        checkErrors();
    }

    private void generateProviders() throws ProcessingException {
        final ClassProducer classProducer = new ProcessorClassProducer(classFileWriter, processorContext);
        final ProvidersGenerator providersGenerator = new ProvidersGenerator(classProducer, processorContext);
        providersGenerator.generateProviders();
        checkErrors();
    }

    private void generateInjectorFactory() throws ProcessingException {
        final ClassProducer classProducer = new ProcessorClassProducer(classFileWriter, processorContext);
        final InjectorFactoryClassGenerator injectorFactoryClassGenerator =
                new InjectorFactoryClassGenerator(classProducer, processorContext);
        injectorFactoryClassGenerator.generateInjectorFactory();
        checkErrors();
    }

    private void generateInjectors() throws ProcessingException {
        final ClassProducer classProducer = new ProcessorClassProducer(classFileWriter, processorContext);
        final InjectorsGenerator injectorsGenerator = new InjectorsGenerator(classProducer, processorContext);
        injectorsGenerator.generateInjectors();
        checkErrors();
    }

    private void copyAndPatchClasses() throws IOException {
        final InjectionClassFileVisitor injectionVisitor =
                new InjectionClassFileVisitor(classFileWriter, processorContext);
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
