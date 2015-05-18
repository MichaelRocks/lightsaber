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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.objectweb.asm.Type;

public class FieldDescriptor {
    private final String name;
    private final ParameterizedType type;

    public FieldDescriptor(final String name, final String desc) {
        this(name, Type.getType(desc));
    }

    public FieldDescriptor(final String name, final Type type) {
        this.name = name;
        this.type = new ParameterizedType(type);
    }

    public FieldDescriptor(final String name, final ParameterizedType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public ParameterizedType getParameterizedType() {
        return type;
    }

    public boolean isParameterized() {
        return type.getParameterType() != null;
    }

    public Type getRawType() {
        return type.getRawType();
    }

    public Type getArgumentType() {
        return type.getParameterType();
    }

    @Override
    public String toString() {
        return type + " " + name;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final FieldDescriptor that = (FieldDescriptor) object;
        return new EqualsBuilder()
                .append(name, that.name)
                .append(type, that.type)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(type)
                .toHashCode();
    }
}
