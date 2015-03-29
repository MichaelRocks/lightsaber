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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InjectionTargetDescriptor {
    private final Type targetType;
    private final List<FieldDescriptor> injectableFields;
    private final List<MethodDescriptor> injectableConstructors;
    private final List<MethodDescriptor> injectableMethods;

    private InjectionTargetDescriptor(final Type targetType, final List<FieldDescriptor> injectableFields,
            final List<MethodDescriptor> injectableConstructors, final List<MethodDescriptor> injectableMethods) {
        this.targetType = targetType;
        this.injectableFields = Collections.unmodifiableList(injectableFields);
        this.injectableConstructors = Collections.unmodifiableList(injectableConstructors);
        this.injectableMethods = Collections.unmodifiableList(injectableMethods);
    }

    public Type getTargetType() {
        return targetType;
    }

    public List<FieldDescriptor> getInjectableFields() {
        return injectableFields;
    }

    public List<MethodDescriptor> getInjectableConstructors() {
        return injectableConstructors;
    }

    public List<MethodDescriptor> getInjectableMethods() {
        return injectableMethods;
    }

    public static class Builder {
        private final Type targetType;
        private boolean hasDefaultConstructor;
        private final List<FieldDescriptor> injectableFields = new ArrayList<>();
        private final List<MethodDescriptor> injectableConstructors = new ArrayList<>();
        private final List<MethodDescriptor> injectableMethods = new ArrayList<>();

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
            if (injectableConstructors.isEmpty() && hasDefaultConstructor) {
                injectableConstructors.add(MethodDescriptor.forConstructor());
            }

            return new InjectionTargetDescriptor(
                    targetType, injectableFields, injectableConstructors, injectableMethods);
        }
    }
}
