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

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class InjectionTargetDescriptor {
    private final Type targetType;
    private final Map<String, QualifiedFieldDescriptor> injectableFields;
    private final QualifiedMethodDescriptor injectableConstructor;
    private final Map<MethodDescriptor, QualifiedMethodDescriptor> injectableConstructors;
    private final Map<MethodDescriptor, QualifiedMethodDescriptor> injectableMethods;
    private final Map<String, QualifiedFieldDescriptor> injectableStaticFields;
    private final Map<MethodDescriptor, QualifiedMethodDescriptor> injectableStaticMethods;
    private final ScopeDescriptor scopeDescriptor;

    private InjectionTargetDescriptor(final Builder builder) {
        this.targetType = builder.targetType;
        this.injectableFields = Collections.unmodifiableMap(builder.injectableFields);
        final Iterator<QualifiedMethodDescriptor> constructorIterator =
                builder.injectableConstructors.values().iterator();
        this.injectableConstructor = constructorIterator.hasNext() ? constructorIterator.next() : null;
        this.injectableConstructors = Collections.unmodifiableMap(builder.injectableConstructors);
        this.injectableMethods = Collections.unmodifiableMap(builder.injectableMethods);
        this.injectableStaticFields = builder.injectableStaticFields;
        this.injectableStaticMethods = builder.injectableStaticMethods;
        this.scopeDescriptor = builder.scopeDescriptor;
    }

    public Type getTargetType() {
        return targetType;
    }

    public boolean isInjectableField(final String fieldName) {
        return injectableFields.containsKey(fieldName);
    }

    public Collection<QualifiedFieldDescriptor> getInjectableFields() {
        return injectableFields.values();
    }

    public boolean isInjectableConstructor(final MethodDescriptor constructor) {
        return injectableConstructor.getMethod().equals(constructor);
    }

    public QualifiedMethodDescriptor getInjectableConstructor() {
        return injectableConstructor;
    }

    public Collection<QualifiedMethodDescriptor> getInjectableConstructors() {
        return injectableConstructors.values();
    }

    public boolean isInjectableMethod(final MethodDescriptor method) {
        return injectableMethods.containsKey(method);
    }

    public Collection<QualifiedMethodDescriptor> getInjectableMethods() {
        return injectableMethods.values();
    }

    public boolean isInjectableStaticField(final String fieldName) {
        return injectableStaticFields.containsKey(fieldName);
    }

    public Map<String, QualifiedFieldDescriptor> getInjectableStaticFields() {
        return injectableStaticFields;
    }

    public boolean isInjectableStaticMethod(final MethodDescriptor method) {
        return injectableStaticMethods.containsKey(method);
    }

    public Map<MethodDescriptor, QualifiedMethodDescriptor> getInjectableStaticMethods() {
        return injectableStaticMethods;
    }

    public ScopeDescriptor getScope() {
        return scopeDescriptor;
    }

    public static class Builder {
        private final Type targetType;
        private boolean hasDefaultConstructor;
        private final Map<String, QualifiedFieldDescriptor> injectableFields = new LinkedHashMap<>();
        private final Map<MethodDescriptor, QualifiedMethodDescriptor> injectableConstructors = new LinkedHashMap<>();
        private final Map<MethodDescriptor, QualifiedMethodDescriptor> injectableMethods = new LinkedHashMap<>();
        private final Map<String, QualifiedFieldDescriptor> injectableStaticFields = new LinkedHashMap<>();
        private final Map<MethodDescriptor, QualifiedMethodDescriptor> injectableStaticMethods = new LinkedHashMap<>();
        private ScopeDescriptor scopeDescriptor;

        public Builder(final Type targetType) {
            this.targetType = targetType;
        }

        public Type getTargetType() {
            return targetType;
        }

        public void setHasDefaultConstructor(final boolean hasDefaultConstructor) {
            this.hasDefaultConstructor = hasDefaultConstructor;
        }

        public Builder addInjectableField(final QualifiedFieldDescriptor injectableField) {
            injectableFields.put(injectableField.getName(), injectableField);
            return this;
        }

        public Builder addInjectableConstructor(final QualifiedMethodDescriptor injectableConstructor) {
            injectableConstructors.put(injectableConstructor.getMethod(), injectableConstructor);
            return this;
        }

        public Builder addInjectableMethod(final QualifiedMethodDescriptor injectableMethod) {
            injectableMethods.put(injectableMethod.getMethod(), injectableMethod);
            return this;
        }

        public Builder addInjectableStaticField(final QualifiedFieldDescriptor injectableField) {
            injectableStaticFields.put(injectableField.getName(), injectableField);
            return this;
        }

        public Builder addInjectableStaticMethod(final QualifiedMethodDescriptor injectableMethod) {
            injectableStaticMethods.put(injectableMethod.getMethod(), injectableMethod);
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

            return new InjectionTargetDescriptor(this);
        }
    }
}
