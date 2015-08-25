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

import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import io.michaelrocks.lightsaber.processor.signature.MethodSignature;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class QualifiedMethodDescriptor {
    private final MethodDescriptor method;
    private final List<AnnotationData> parameterQualifiers;
    private final AnnotationData resultQualifier;

    private QualifiedMethodDescriptor(final MethodDescriptor method,
            final List<AnnotationData> parameterQualifiers, final AnnotationData resultQualifier) {
        this.method = method;
        this.parameterQualifiers = parameterQualifiers;
        this.resultQualifier = resultQualifier;
    }

    public static QualifiedMethodDescriptor from(final MethodDescriptor method,
            final Map<Integer, AnnotationData> parameterQualifiers) {
        return QualifiedMethodDescriptor.from(method, parameterQualifiers, null);
    }

    public static QualifiedMethodDescriptor from(final MethodDescriptor method,
            final Map<Integer, AnnotationData> parameterQualifiers, final AnnotationData resultQualifier) {
        final int parameterCount = method.getArgumentTypes().size();
        return new QualifiedMethodDescriptor(method, toQualifierList(parameterQualifiers, parameterCount),
                resultQualifier
        );
    }

    private static List<AnnotationData> toQualifierList(
            final Map<Integer, AnnotationData> parameterQualifiers, final int parameterCount) {
        final List<AnnotationData> qualifiers = new ArrayList<>(parameterCount);
        for (int i = 0; i < parameterCount; ++i) {
            qualifiers.add(parameterQualifiers.get(i));
        }
        return Collections.unmodifiableList(qualifiers);
    }

    public MethodDescriptor getMethod() {
        return method;
    }

    public List<AnnotationData> getParameterQualifiers() {
        return parameterQualifiers;
    }

    public AnnotationData getResultQualifier() {
        return resultQualifier;
    }

    public String getName() {
        return method.getName();
    }

    public String getDescriptor() {
        return method.getDescriptor();
    }

    public List<TypeSignature> getArgumentTypes() {
        return method.getArgumentTypes();
    }

    public TypeSignature getReturnType() {
        return method.getReturnType();
    }

    public Type getType() {
        return method.getType();
    }

    public MethodSignature getSignature() {
        return method.getSignature();
    }

    public boolean isConstructor() {
        return method.isConstructor();
    }

    public boolean isDefaultConstructor() {
        return method.isDefaultConstructor();
    }

    public boolean isStaticInitializer() {
        return method.isStaticInitializer();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final QualifiedMethodDescriptor that = (QualifiedMethodDescriptor) object;
        return new EqualsBuilder()
                .append(method, that.method)
                .append(parameterQualifiers, that.parameterQualifiers)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(method)
                .append(parameterQualifiers)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("method", method)
                .append("parameterQualifiers", parameterQualifiers)
                .toString();
    }
}
