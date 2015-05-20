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

import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.objectweb.asm.Type;

public class FieldDescriptor {
    private final String name;
    private final TypeSignature signature;

    public FieldDescriptor(final String name, final String desc) {
        this(name, Type.getType(desc));
    }

    public FieldDescriptor(final String name, final Type signature) {
        this.name = name;
        this.signature = new TypeSignature(signature);
    }

    public FieldDescriptor(final String name, final TypeSignature signature) {
        this.name = name;
        this.signature = signature;
    }

    public String getName() {
        return name;
    }

    public TypeSignature getSignature() {
        return signature;
    }

    public boolean isParameterized() {
        return signature.getParameterType() != null;
    }

    public Type getRawType() {
        return signature.getRawType();
    }

    public Type getParameterType() {
        return signature.getParameterType();
    }

    @Override
    public String toString() {
        return signature + " " + name;
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
                .append(signature, that.signature)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .append(signature)
                .toHashCode();
    }
}
