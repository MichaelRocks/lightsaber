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

import io.michaelrocks.lightsaber.processor.signature.MethodSignature;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.objectweb.asm.Type;

import java.util.List;

public class MethodDescriptor {
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String STATIC_INITIALIZER_NAME = "<clinit>";

    private static final MethodDescriptor DEFAULT_CONSTRUCTOR_DESCRIPTOR = forConstructor();
    private static final MethodDescriptor STATIC_INITIALIZER_DESCRIPTOR = forMethod("<clinit>", Type.VOID_TYPE);

    private final String name;
    private final MethodSignature signature;

    public MethodDescriptor(final String name, final String desc) {
        this(name, Type.getType(desc));
    }

    public MethodDescriptor(final String name, final Type type) {
        this(name, new MethodSignature(type));
    }

    public MethodDescriptor(final String name, final MethodSignature signature) {
        this.name = name;
        this.signature = signature;
    }

    public static MethodDescriptor forMethod(final String name, final Type returnType, final Type... argumentTypes) {
        return new MethodDescriptor(name, Type.getMethodType(returnType, argumentTypes));
    }

    public static MethodDescriptor forConstructor(final Type... argumentTypes) {
        return new MethodDescriptor(CONSTRUCTOR_NAME, Type.getMethodType(Type.VOID_TYPE, argumentTypes));
    }

    public static MethodDescriptor forDefaultConstructor() {
        return DEFAULT_CONSTRUCTOR_DESCRIPTOR;
    }

    public static MethodDescriptor forStaticInitializer() {
        return STATIC_INITIALIZER_DESCRIPTOR;
    }

    public static boolean isConstructor(final String methodName) {
        return CONSTRUCTOR_NAME.equals(methodName);
    }

    public static boolean isDefaultConstructor(final String methodName, final String methodDesc) {
        return isConstructor(methodName) && Type.getMethodDescriptor(Type.VOID_TYPE).equals(methodDesc);
    }

    public static boolean isStaticInitializer(final String methodName) {
        return STATIC_INITIALIZER_NAME.equals(methodName);
    }

    public String getName() {
        return name;
    }

    public MethodSignature getSignature() {
        return signature;
    }

    public Type getType() {
        return signature.getMethodType();
    }

    public String getDescriptor() { return signature.getMethodType().getDescriptor(); }

    public TypeSignature getReturnType() {
        return signature.getReturnType();
    }

    public List<TypeSignature> getArgumentTypes() {
        return signature.getArgumentTypes();
    }

    public boolean isConstructor() {
        return isConstructor(name);
    }

    public boolean isDefaultConstructor() {
        return isDefaultConstructor(name, signature.getMethodType().getDescriptor());
    }

    public boolean isStaticInitializer() { return STATIC_INITIALIZER_DESCRIPTOR.equals(this); }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final MethodDescriptor that = (MethodDescriptor) object;
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

    @Override
    public String toString() {
        return name + " " + signature;
    }
}
