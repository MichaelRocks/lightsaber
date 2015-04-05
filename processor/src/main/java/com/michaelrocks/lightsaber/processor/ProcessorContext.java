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

import com.michaelrocks.lightsaber.internal.Lightsaber$$GlobalModule;
import com.michaelrocks.lightsaber.internal.Lightsaber$$InjectorFactory;
import com.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProcessorContext {
    private static final Type GLOBAL_MODULE_TYPE = Type.getType(Lightsaber$$GlobalModule.class);
    private static final Type INJECTOR_FACTORY_TYPE = Type.getType(Lightsaber$$InjectorFactory.class);

    private String classFilePath;
    private final Map<String, List<Exception>> errorsByPath = new LinkedHashMap<>();
    private final Map<Type, ModuleDescriptor> modules = new HashMap<>();
    private ModuleDescriptor globalModule;
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

    public void reportError(final Exception error) {
        List<Exception> errors = errorsByPath.get(classFilePath);
        if (errors == null) {
            errors = new ArrayList<>();
            errorsByPath.put(classFilePath, errors);
        }
        errors.add(error);
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

    public ModuleDescriptor getGlobalModule() {
        return globalModule;
    }

    public void setGlobalModule(final ModuleDescriptor globalModule) {
        Validate.isTrue(this.globalModule == null, "Global module cannot be set multiple times");
        Validate.notNull(globalModule, "Global module cannot be set to null");
        this.globalModule = globalModule;
        addModule(globalModule);
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

    public Type getGlobalModuleType() {
        return GLOBAL_MODULE_TYPE;
    }

    public Type getInjectorFactoryType() {
        return INJECTOR_FACTORY_TYPE;
    }

    public void dump() {
        for (final ModuleDescriptor module : getModules()) {
            System.out.println("Module: " + module.getModuleType());
            for (final ProviderDescriptor provider : module.getProviders()) {
                System.out.println("\tProvides: " + provider.getProviderMethod());
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
