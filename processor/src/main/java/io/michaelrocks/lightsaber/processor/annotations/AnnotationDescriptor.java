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

package io.michaelrocks.lightsaber.processor.annotations;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.objectweb.asm.Type;

import java.util.Map;

public class AnnotationDescriptor {
    private final Type type;
    private final Map<String, Object> values;
    private final boolean resolved;

    AnnotationDescriptor(final Type type, final Map<String, Object> values, final boolean resolved) {
        this.type = type;
        this.values = values;
        this.resolved = resolved;
    }

    public Type getType() {
        return type;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public boolean isResolved() {
        return resolved;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final AnnotationDescriptor that = (AnnotationDescriptor) object;
        final EqualsBuilder equalsBuilder = new EqualsBuilder()
                .append(type, that.type)
                .append(values.size(), that.values.size());
        if (!equalsBuilder.isEquals()) {
            return false;
        }

        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            equalsBuilder
                    .append(true, that.getValues().containsKey(entry.getKey()))
                    .append(entry.getValue(), that.getValues().get(entry.getKey()));
            if (!equalsBuilder.isEquals()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        final HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(17, 37)
                .append(type);
        final HashCodeBuilder mapHashCodeBuilder = new HashCodeBuilder(1, 1);
        for (final Map.Entry<String, Object> entry : values.entrySet()) {
            mapHashCodeBuilder.append(entry.getValue());
        }
        return hashCodeBuilder
                .append(mapHashCodeBuilder.build())
                .build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("values", values)
                .toString();
    }
}
