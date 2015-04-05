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

package com.michaelrocks.lightsaber.processor.descriptors;

import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class InjectionTargetDescriptor {
    private final Type targetType;
    private final Set<FieldDescriptor> injectableFields;
    private final MethodDescriptor injectableConstructor;
    private final Set<MethodDescriptor> injectableConstructors;
    private final Set<MethodDescriptor> injectableMethods;

    private InjectionTargetDescriptor(final Type targetType, final Set<FieldDescriptor> injectableFields,
            final Set<MethodDescriptor> injectableConstructors, final Set<MethodDescriptor> injectableMethods) {
        this.targetType = targetType;
        this.injectableFields = Collections.unmodifiableSet(injectableFields);
        final Iterator<MethodDescriptor> constructorIterator = injectableConstructors.iterator();
        this.injectableConstructor = constructorIterator.hasNext() ? constructorIterator.next() : null;
        this.injectableConstructors = Collections.unmodifiableSet(injectableConstructors);
        this.injectableMethods = Collections.unmodifiableSet(injectableMethods);
    }

    public Type getTargetType() {
        return targetType;
    }

    public Set<FieldDescriptor> getInjectableFields() {
        return injectableFields;
    }

    public MethodDescriptor getInjectableConstructor() {
        return injectableConstructor;
    }

    public Set<MethodDescriptor> getInjectableConstructors() {
        return injectableConstructors;
    }

    public Set<MethodDescriptor> getInjectableMethods() {
        return injectableMethods;
    }

    public static class Builder {
        private final Type targetType;
        private boolean hasDefaultConstructor;
        private final Set<FieldDescriptor> injectableFields = new LinkedHashSet<>();
        private final Set<MethodDescriptor> injectableConstructors = new LinkedHashSet<>();
        private final Set<MethodDescriptor> injectableMethods = new LinkedHashSet<>();

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

        public InjectionTargetDescriptor build() {
            // TODO: Allow to inject objects with default constructors when we can ensure they will be used.
            // if (injectableConstructors.isEmpty() && hasDefaultConstructor) {
            //     injectableConstructors.add(MethodDescriptor.forConstructor());
            // }

            return new InjectionTargetDescriptor(
                    targetType, injectableFields, injectableConstructors, injectableMethods);
        }
    }
}
