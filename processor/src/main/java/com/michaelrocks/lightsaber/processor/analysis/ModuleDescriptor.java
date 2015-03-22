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

package com.michaelrocks.lightsaber.processor.analysis;

import com.michaelrocks.lightsaber.processor.MethodDescriptor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleDescriptor {
    private final Type moduleType;
    private final List<MethodDescriptor> providerMethods;

    public ModuleDescriptor(final Type moduleType, final List<MethodDescriptor> providerMethods) {
        this.moduleType = moduleType;
        this.providerMethods = Collections.unmodifiableList(providerMethods);
    }

    public Type getModuleType() {
        return moduleType;
    }

    public List<MethodDescriptor> getProviderMethods() {
        return providerMethods;
    }

    public static class Builder {
        private final Type moduleType;
        private final List<MethodDescriptor> providerMethods = new ArrayList<>();

        public Builder(final Type moduleType) {
            this.moduleType = moduleType;
        }

        public Builder addProviderMethod(final MethodDescriptor providerMethod) {
            providerMethods.add(providerMethod);
            return this;
        }

        public ModuleDescriptor build() {
            return new ModuleDescriptor(moduleType, providerMethods);
        }
    }
}
