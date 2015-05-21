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

import io.michaelrocks.lightsaber.SingletonProvider;
import io.michaelrocks.lightsaber.internal.Lightsaber$$InjectorFactory;
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ScopeDescriptor;
import io.michaelrocks.lightsaber.processor.graph.TypeGraph;
import org.objectweb.asm.Type;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProcessorContext {
    private static final String PACKAGE_MODULE_CLASS_NAME = "Lightsaber$$PackageModule";
    private static final Type INJECTOR_FACTORY_TYPE = Type.getType(Lightsaber$$InjectorFactory.class);
    private static final ScopeDescriptor SINGLETON_SCOPE_DESCRIPTOR =
            new ScopeDescriptor(Type.getType(Singleton.class), Type.getType(SingletonProvider.class));

    private String classFilePath;
    private final Map<String, List<Exception>> errorsByPath = new LinkedHashMap<>();

    private TypeGraph typeGraph;
    private final Map<Type, ModuleDescriptor> modules = new HashMap<>();
    private final Map<Type, ModuleDescriptor> packageModules = new HashMap<>();
    private final Map<Type, InjectionTargetDescriptor> injectableTargets = new HashMap<>();
    private final Map<Type, InjectionTargetDescriptor> providableTargets = new HashMap<>();
    private final Map<Type, InjectorDescriptor> injectors = new HashMap<>();

    public String getClassFilePath() {
        return classFilePath;
    }

    public void setClassFilePath(final String path) {
        classFilePath = path;
    }

    public boolean hasErrors() {
        return !errorsByPath.isEmpty();
    }

    public Map<String, List<Exception>> getErrors() {
        return Collections.unmodifiableMap(errorsByPath);
    }

    public void reportError(final String errorMessage) {
        reportError(new ProcessingException(errorMessage, classFilePath));
    }

    public void reportError(final Exception error) {
        List<Exception> errors = errorsByPath.get(classFilePath);
        if (errors == null) {
            errors = new ArrayList<>();
            errorsByPath.put(classFilePath, errors);
        }
        errors.add(error);
    }

    public TypeGraph getTypeGraph() {
        return typeGraph;
    }

    public void setTypeGraph(final TypeGraph typeGraph) {
        this.typeGraph = typeGraph;
    }

    public ModuleDescriptor findModuleByType(final Type moduleType) {
        return modules.get(moduleType);
    }

    public Collection<ModuleDescriptor> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public void addModule(final ModuleDescriptor module) {
        modules.put(module.getModuleType(), module);
    }

    public Collection<ModuleDescriptor> getPackageModules() {
        return Collections.unmodifiableCollection(packageModules.values());
    }

    public void addPackageModule(final ModuleDescriptor packageModule) {
        packageModules.put(packageModule.getModuleType(), packageModule);
        addModule(packageModule);
    }

    public InjectionTargetDescriptor findInjectableTargetByType(final Type injectableTargetType) {
        return injectableTargets.get(injectableTargetType);
    }

    public Collection<InjectionTargetDescriptor> getInjectableTargets() {
        return Collections.unmodifiableCollection(injectableTargets.values());
    }

    public void addInjectableTarget(final InjectionTargetDescriptor injectableTarget) {
        injectableTargets.put(injectableTarget.getTargetType(), injectableTarget);
    }

    public InjectionTargetDescriptor findProvidableTargetByType(final Type providableTargetType) {
        return providableTargets.get(providableTargetType);
    }

    public Collection<InjectionTargetDescriptor> getProvidableTargets() {
        return Collections.unmodifiableCollection(providableTargets.values());
    }

    public void addProvidableTarget(final InjectionTargetDescriptor providableTarget) {
        providableTargets.put(providableTarget.getTargetType(), providableTarget);
    }

    public InjectorDescriptor findInjectorByTargetType(final Type targetType) {
        return injectors.get(targetType);
    }

    public Collection<InjectorDescriptor> getInjectors() {
        return Collections.unmodifiableCollection(injectors.values());
    }

    public void addInjector(final InjectorDescriptor injector) {
        injectors.put(injector.getInjectableTarget().getTargetType(), injector);
    }

    public ScopeDescriptor findScopeByAnnotationType(final Type annotationType) {
        if (SINGLETON_SCOPE_DESCRIPTOR.getScopeAnnotationType().equals(annotationType)) {
            return SINGLETON_SCOPE_DESCRIPTOR;
        }
        return null;
    }

    public Collection<ScopeDescriptor> getScopes() {
        return Collections.singleton(SINGLETON_SCOPE_DESCRIPTOR);
    }

    public Type getPackageModuleType(final String packageName) {
        return Type.getObjectType(packageName + PACKAGE_MODULE_CLASS_NAME);
    }

    public Type getInjectorFactoryType() {
        return INJECTOR_FACTORY_TYPE;
    }

    public void dump() {
        for (final ModuleDescriptor module : getModules()) {
            System.out.println("Module: " + module.getModuleType());
            for (final ProviderDescriptor provider : module.getProviders()) {
                if (provider.getProviderMethod() != null) {
                    System.out.println("\tProvides: " + provider.getProviderMethod());
                } else {
                    System.out.println("\tProvides: " + provider.getProviderField());
                }
            }
        }
        for (final InjectionTargetDescriptor injectableTarget : getInjectableTargets()) {
            System.out.println("Injectable: " + injectableTarget.getTargetType());
            for (final FieldDescriptor injectableField : injectableTarget.getInjectableFields()) {
                System.out.println("\tField: " + injectableField);
            }
            for (final MethodDescriptor injectableMethod : injectableTarget.getInjectableMethods()) {
                System.out.println("\tMethod: " + injectableMethod);
            }
        }
        for (final InjectionTargetDescriptor providableTarget : getProvidableTargets()) {
            System.out.println("Providable: " + providableTarget.getTargetType());
            for (final MethodDescriptor injectableConstructor : providableTarget.getInjectableConstructors()) {
                System.out.println("\tConstructor: " + injectableConstructor);
            }
        }
    }
}
