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

package io.michaelrocks.lightsaber.processor.signature;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodSignature {
    private final Type methodType;
    private final TypeSignature returnType;
    private final List<TypeSignature> argumentTypes;

    public MethodSignature(final Type methodType) {
        this.methodType = methodType;
        this.returnType = createReturnTypeSignature(methodType);
        this.argumentTypes = Collections.unmodifiableList(createArgumentTypesSignatures(methodType));
    }

    public MethodSignature(final TypeSignature returnType, final List<TypeSignature> argumentTypes) {
        this.methodType = createRawType(returnType, argumentTypes);
        this.returnType = returnType;
        this.argumentTypes = Collections.unmodifiableList(new ArrayList<>(argumentTypes));
    }

    public Type getMethodType() {
        return methodType;
    }

    public TypeSignature getReturnType() {
        return returnType;
    }

    public List<TypeSignature> getArgumentTypes() {
        return argumentTypes;
    }

    private static TypeSignature createReturnTypeSignature(final Type methodType) {
        return new TypeSignature(methodType.getReturnType());
    }

    private static List<TypeSignature> createArgumentTypesSignatures(final Type methodType) {
        final ArrayList<TypeSignature> argumentTypes = new ArrayList<>();
        for (final Type argumentType : methodType.getArgumentTypes()) {
            argumentTypes.add(new TypeSignature(argumentType));
        }
        return argumentTypes;
    }

    private static Type createRawType(final TypeSignature returnTypeSignature,
            final List<TypeSignature> argumentTypesSignatures) {
        final Type[] argumentTypes = new Type[argumentTypesSignatures.size()];
        for (int i = 0; i < argumentTypesSignatures.size(); ++i) {
            argumentTypes[i] = argumentTypesSignatures.get(i).getRawType();
        }
        return Type.getMethodType(returnTypeSignature.getRawType(), argumentTypes);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final MethodSignature that = (MethodSignature) object;
        return new EqualsBuilder()
                .append(returnType, that.returnType)
                .append(argumentTypes, that.argumentTypes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(returnType)
                .append(argumentTypes)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "(" + StringUtils.join(argumentTypes, ", ") + "): " + returnType;
    }
}
