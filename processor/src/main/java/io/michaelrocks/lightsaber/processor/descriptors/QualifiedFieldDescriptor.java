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
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.objectweb.asm.Type;

public class QualifiedFieldDescriptor {
    private final FieldDescriptor field;
    private final AnnotationData qualifier;

    public QualifiedFieldDescriptor(final FieldDescriptor field, final AnnotationData qualifier) {
        this.field = field;
        this.qualifier = qualifier;
    }

    public FieldDescriptor getField() {
        return field;
    }

    public AnnotationData getQualifier() {
        return qualifier;
    }

    public String getName() {
        return field.getName();
    }

    public Type getParameterType() {
        return field.getParameterType();
    }

    public boolean isParameterized() {
        return field.isParameterized();
    }

    public TypeSignature getSignature() {
        return field.getSignature();
    }

    public Type getRawType() {
        return field.getRawType();
    }

    public String getDescriptor() {
        return field.getDescriptor();
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final QualifiedFieldDescriptor that = (QualifiedFieldDescriptor) object;
        return new EqualsBuilder()
                .append(field, that.field)
                .append(qualifier, that.qualifier)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(field)
                .append(qualifier)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("field", field)
                .append("qualifier", qualifier)
                .toString();
    }
}
