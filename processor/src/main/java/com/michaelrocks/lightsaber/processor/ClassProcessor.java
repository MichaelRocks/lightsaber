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

import com.michaelrocks.lightsaber.internal.Lightsaber$$PackageModule;
import com.michaelrocks.lightsaber.processor.analysis.AnalysisClassFileVisitor;
import com.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ScopeDescriptor;
import com.michaelrocks.lightsaber.processor.generation.ClassProducer;
import com.michaelrocks.lightsaber.processor.generation.InjectorFactoryClassGenerator;
import com.michaelrocks.lightsaber.processor.generation.InjectorsGenerator;
import com.michaelrocks.lightsaber.processor.generation.PackageModuleClassGenerator;
import com.michaelrocks.lightsaber.processor.generation.ProcessorClassProducer;
import com.michaelrocks.lightsaber.processor.generation.ProvidersGenerator;
import com.michaelrocks.lightsaber.processor.graph.CycleSearcher;
import com.michaelrocks.lightsaber.processor.graph.DependencyGraph;
import com.michaelrocks.lightsaber.processor.graph.UnresolvedDependenciesSearcher;
import com.michaelrocks.lightsaber.processor.injection.InjectionClassFileVisitor;
import com.michaelrocks.lightsaber.processor.io.ClassFileReader;
import com.michaelrocks.lightsaber.processor.io.ClassFileWriter;
import org.apache.commons.io.FilenameUtils;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
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
        composePackageModules();
        composeInjectors();
        processorContext.dump();
        validateRootDependencyGraph();
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

    private void composePackageModules() {
        final Map<String, ModuleDescriptor.Builder> moduleBuilders = new HashMap<>();
        for (final InjectionTargetDescriptor providableTarget : processorContext.getProvidableTargets()) {
            final Type providableTargetType = providableTarget.getTargetType();
            final MethodDescriptor providableTargetConstructor = providableTarget.getInjectableConstructor();

            final String packageName = FilenameUtils.getPath(providableTargetType.getInternalName());
            ModuleDescriptor.Builder moduleBuilder = moduleBuilders.get(packageName);
            if (moduleBuilder == null) {
                final Type moduleType =
                        Type.getObjectType(packageName + Lightsaber$$PackageModule.class.getSimpleName());
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
                    Type.getObjectType(injectableTarget.getTargetType().getInternalName() + "$$Injector");
            final InjectorDescriptor injector = new InjectorDescriptor(injectorType, injectableTarget);
            processorContext.addInjector(injector);
        }
    }

    private void validateRootDependencyGraph() throws ProcessingException {
        final DependencyGraph dependencyGraph =
                new DependencyGraph(processorContext, processorContext.getPackageModules());

        final UnresolvedDependenciesSearcher unresolvedDependenciesSearcher =
                new UnresolvedDependenciesSearcher(dependencyGraph);
        final Collection<Type> unresolvedDependencies = unresolvedDependenciesSearcher.findUnresolvedDependencies();
        for (final Type unresolvedDependency : unresolvedDependencies) {
            processorContext.reportError(
                    new ProcessingException("Unresolved root dependency: " + unresolvedDependency));
        }

        checkErrors();
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
