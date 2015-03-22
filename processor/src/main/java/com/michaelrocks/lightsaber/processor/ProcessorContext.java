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

import com.michaelrocks.lightsaber.processor.analysis.InjectionTargetDescriptor;
import com.michaelrocks.lightsaber.processor.analysis.ModuleDescriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ProcessorContext {
    private String classFilePath;
    private final Map<String, List<Exception>> errorsByPath = new LinkedHashMap<>();
    private final List<ModuleDescriptor> modules = new ArrayList<>();
    private final List<InjectionTargetDescriptor> injectableTargets = new ArrayList<>();
    private final List<InjectionTargetDescriptor> providableTargets = new ArrayList<>();

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

    public List<ModuleDescriptor> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public void addModule(final ModuleDescriptor module) {
        modules.add(module);
    }

    public List<InjectionTargetDescriptor> getInjectableTargets() {
        return Collections.unmodifiableList(injectableTargets);
    }

    public void addInjectableTarget(final InjectionTargetDescriptor injectableTarget) {
        injectableTargets.add(injectableTarget);
    }

    public List<InjectionTargetDescriptor> getProvidableTargets() {
        return Collections.unmodifiableList(providableTargets);
    }

    public void addProvidableTarget(final InjectionTargetDescriptor providableTarget) {
        providableTargets.add(providableTarget);
    }
}
