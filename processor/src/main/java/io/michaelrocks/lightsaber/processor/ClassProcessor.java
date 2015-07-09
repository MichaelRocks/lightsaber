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

package io.michaelrocks.lightsaber.processor;

import io.michaelrocks.lightsaber.processor.analysis.AnalysisClassFileVisitor;
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ScopeDescriptor;
import io.michaelrocks.lightsaber.processor.generation.ClassProducer;
import io.michaelrocks.lightsaber.processor.generation.InjectorFactoryClassGenerator;
import io.michaelrocks.lightsaber.processor.generation.PackageModuleClassGenerator;
import io.michaelrocks.lightsaber.processor.generation.ProcessorClassProducer;
import io.michaelrocks.lightsaber.processor.generation.ProvidersGenerator;
import io.michaelrocks.lightsaber.processor.generation.TypeAgentsGenerator;
import io.michaelrocks.lightsaber.processor.graph.CycleSearcher;
import io.michaelrocks.lightsaber.processor.graph.DependencyGraph;
import io.michaelrocks.lightsaber.processor.graph.TypeGraphBuilder;
import io.michaelrocks.lightsaber.processor.graph.UnresolvedDependenciesSearcher;
import io.michaelrocks.lightsaber.processor.injection.InjectionClassFileVisitor;
import io.michaelrocks.lightsaber.processor.io.ClassFileReader;
import io.michaelrocks.lightsaber.processor.io.ClassFileWriter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassProcessor {
    private final ClassFileReader classFileReader;
    private final ClassFileWriter classFileWriter;
    private final List<File> libraries;

    private final ProcessorContext processorContext = new ProcessorContext();

    public ClassProcessor(final ClassFileReader classFileReader, final ClassFileWriter classFileWriter,
            final List<File> libraries) {
        this.classFileWriter = classFileWriter;
        this.classFileReader = classFileReader;
        this.libraries = new ArrayList<>(libraries);
    }

    public void processClasses() throws IOException {
        buildTypeGraph();
        performAnalysis();
        composePackageModules();
        composeInjectors();
        processorContext.dump();
        validateDependencyGraph();
        generateGlobalModule();
        generateProviders();
        generateInjectorFactory();
        generateInjectors();
        copyAndPatchClasses();
    }

    private void buildTypeGraph() throws IOException {
        final TypeGraphBuilder typeGraphBuilder = new TypeGraphBuilder();
        for (final File library : libraries) {
            if (library.isDirectory()) {
                typeGraphBuilder.addClassesFromClasses(library);
            } else {
                typeGraphBuilder.addClassesFromJar(library);
            }
        }
        typeGraphBuilder.addClassesFromReader(classFileReader);
        processorContext.setTypeGraph(typeGraphBuilder.build());
    }

    private void performAnalysis() throws IOException {
        final AnalysisClassFileVisitor analysisVisitor = new AnalysisClassFileVisitor(processorContext);
        classFileReader.accept(analysisVisitor);
        checkErrors();
    }

    private void composePackageModules() {
        final Map<String, ModuleDescriptor.Builder> moduleBuilders = new HashMap<>();
        for (final InjectionTargetDescriptor providableTarget : processorContext.getProvidableTargets()) {
            final Type providableTargetType = providableTarget.getTargetType();
            final MethodDescriptor providableTargetConstructor = providableTarget.getInjectableConstructor();

            final String packageName = FilenameUtils.getPath(providableTargetType.getInternalName());
            ModuleDescriptor.Builder moduleBuilder = moduleBuilders.get(packageName);
            if (moduleBuilder == null) {
                final Type moduleType = processorContext.getPackageModuleType(packageName);
                moduleBuilder = new ModuleDescriptor.Builder(moduleType);
                moduleBuilders.put(packageName, moduleBuilder);
            }

            final Type providerType = Type.getObjectType(
                    providableTargetType.getInternalName() + "$$Provider");
            final ScopeDescriptor scope = providableTarget.getScope();
            final Type delegatorType = scope != null ? scope.getProviderType() : null;
            final ProviderDescriptor provider =
                    new ProviderDescriptor(providerType, providableTarget.getTargetType(), providableTargetConstructor,
                            moduleBuilder.getModuleType(), delegatorType);

            moduleBuilder.addProvider(provider);
        }

        for (final ModuleDescriptor.Builder moduleBuilder : moduleBuilders.values()) {
            processorContext.addPackageModule(moduleBuilder.build());
        }
    }

    private void composeInjectors() {
        for (final InjectionTargetDescriptor injectableTarget : processorContext.getInjectableTargets()) {
            final Type injectorType =
                    Type.getObjectType(injectableTarget.getTargetType().getInternalName() + "$$Agent");
            final InjectorDescriptor injector = new InjectorDescriptor(injectorType, injectableTarget);
            processorContext.addInjector(injector);
        }
    }

    private void validateDependencyGraph() throws ProcessingException {
        final DependencyGraph dependencyGraph = new DependencyGraph(processorContext, processorContext.getModules());

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
        final PackageModuleClassGenerator packageModuleClassGenerator =
                new PackageModuleClassGenerator(classProducer, processorContext);
        for (final ModuleDescriptor packageModule : processorContext.getPackageModules()) {
            packageModuleClassGenerator.generatePackageModule(packageModule);
        }
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
        final TypeAgentsGenerator typeAgentsGenerator = new TypeAgentsGenerator(classProducer, processorContext);
        typeAgentsGenerator.generateInjectors();
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
                        .append(error.getMessage())
                        .append(System.lineSeparator())
                        .append(ExceptionUtils.getStackTrace(error));
            }
        }
        return builder.toString();
    }
}
