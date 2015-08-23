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

package io.michaelrocks.lightsaber.processor.descriptors;

import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class InjectionTargetDescriptor {
    private final Type targetType;
    private final Set<FieldDescriptor> injectableFields;
    private final Map<String, FieldDescriptor> injectableFieldsByName;
    private final MethodDescriptor injectableConstructor;
    private final Set<MethodDescriptor> injectableConstructors;
    private final Set<MethodDescriptor> injectableMethods;
    private final ScopeDescriptor scopeDescriptor;

    private InjectionTargetDescriptor(final Type targetType, final Set<FieldDescriptor> injectableFields,
            final Set<MethodDescriptor> injectableConstructors, final Set<MethodDescriptor> injectableMethods,
            final ScopeDescriptor scopeDescriptor) {
        this.targetType = targetType;
        this.injectableFields = Collections.unmodifiableSet(injectableFields);
        this.injectableFieldsByName = new HashMap<>();
        for (final FieldDescriptor injectableField : injectableFields) {
            this.injectableFieldsByName.put(injectableField.getName(), injectableField);
        }
        final Iterator<MethodDescriptor> constructorIterator = injectableConstructors.iterator();
        this.injectableConstructor = constructorIterator.hasNext() ? constructorIterator.next() : null;
        this.injectableConstructors = Collections.unmodifiableSet(injectableConstructors);
        this.injectableMethods = Collections.unmodifiableSet(injectableMethods);
        this.scopeDescriptor = scopeDescriptor;
    }

    public Type getTargetType() {
        return targetType;
    }

    public boolean isInjectableField(final String name) {
        return injectableFieldsByName.containsKey(name);
    }

    public Set<FieldDescriptor> getInjectableFields() {
        return injectableFields;
    }

    public boolean isInjectableConstructor(final MethodDescriptor constructor) {
        return injectableConstructor.equals(constructor);
    }

    public MethodDescriptor getInjectableConstructor() {
        return injectableConstructor;
    }

    public Set<MethodDescriptor> getInjectableConstructors() {
        return injectableConstructors;
    }

    public boolean isInjectableMethod(final MethodDescriptor method) {
        return injectableMethods.contains(method);
    }

    public Set<MethodDescriptor> getInjectableMethods() {
        return injectableMethods;
    }

    public ScopeDescriptor getScope() {
        return scopeDescriptor;
    }

    public static class Builder {
        private final Type targetType;
        private boolean hasDefaultConstructor;
        private final Set<FieldDescriptor> injectableFields = new LinkedHashSet<>();
        private final Set<MethodDescriptor> injectableConstructors = new LinkedHashSet<>();
        private final Set<MethodDescriptor> injectableMethods = new LinkedHashSet<>();
        private ScopeDescriptor scopeDescriptor;

        public Builder(final Type targetType) {
            this.targetType = targetType;
        }

        public void setHasDefaultConstructor(final boolean hasDefaultConstructor) {
            this.hasDefaultConstructor = hasDefaultConstructor;
        }

        public Builder addInjectableField(final FieldDescriptor injectableField) {
            injectableFields.add(injectableField);
            return this;
        }

        public Builder addInjectableConstructor(final MethodDescriptor injectableConstructor) {
            injectableConstructors.add(injectableConstructor);
            return this;
        }

        public Builder addInjectableMethod(final MethodDescriptor injectableMethod) {
            injectableMethods.add(injectableMethod);
            return this;
        }

        public Builder setScope(final ScopeDescriptor scopeDescriptor) {
            this.scopeDescriptor = scopeDescriptor;
            return this;
        }

        public InjectionTargetDescriptor build() {
            // TODO: Allow to inject objects with default constructors when we can ensure they will be used.
            // if (injectableConstructors.isEmpty() && hasDefaultConstructor) {
            //     injectableConstructors.add(MethodDescriptor.forConstructor());
            // }

            return new InjectionTargetDescriptor(
                    targetType, injectableFields, injectableConstructors, injectableMethods, scopeDescriptor);
        }
    }
}
