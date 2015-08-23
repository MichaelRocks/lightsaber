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

import io.michaelrocks.lightsaber.processor.annotations.AnnotationDescriptor;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProviderDescriptor {
    private final Type providerType;
    private final QualifiedType providableType;
    private final FieldDescriptor providerField;
    private final MethodDescriptor providerMethod;
    private final Type moduleType;
    private final Type delegatorType;

    public ProviderDescriptor(final Type providerType, final QualifiedType providableType,
            final FieldDescriptor providerField, final Type moduleType) {
        this(providerType, providableType, providerField, null, moduleType, null);
    }

    public ProviderDescriptor(final Type providerType, final QualifiedType providableType,
            final MethodDescriptor providerMethod, final Type moduleType, final Type delegatorType) {
        this(providerType, providableType, null, providerMethod, moduleType, delegatorType);
    }

    private ProviderDescriptor(final Type providerType, final QualifiedType providableType,
            final FieldDescriptor providerField, final MethodDescriptor providerMethod, final Type moduleType,
            final Type delegatorType) {
        this.providerType = providerType;
        this.providableType = providableType;
        this.providerField = providerField;
        this.providerMethod = providerMethod;
        this.moduleType = moduleType;
        this.delegatorType = delegatorType;
    }

    public Type getProviderType() {
        return providerType;
    }

    public QualifiedType getQualifiedProvidableType() {
        return providableType;
    }

    public AnnotationDescriptor getQualifier() {
        return providableType.getQualifier();
    }

    public Type getProvidableType() {
        return providableType.getType();
    }

    public FieldDescriptor getProviderField() {
        return providerField;
    }

    public MethodDescriptor getProviderMethod() {
        return providerMethod;
    }

    public Type getModuleType() {
        return moduleType;
    }

    public Type getDelegatorType() {
        return delegatorType;
    }

    public List<QualifiedType> getDependencies() {
        if (providerMethod == null) {
            return Collections.emptyList();
        }

        final List<QualifiedType> dependencies = new ArrayList<>(providerMethod.getArgumentTypes().size());
        for (final TypeSignature argumentType : providerMethod.getArgumentTypes()) {
            final Type dependencyType = argumentType.getParameterType() != null
                    ? argumentType.getParameterType() : argumentType.getRawType();
            dependencies.add(new QualifiedType(dependencyType));
        }
        return dependencies;
    }
}
