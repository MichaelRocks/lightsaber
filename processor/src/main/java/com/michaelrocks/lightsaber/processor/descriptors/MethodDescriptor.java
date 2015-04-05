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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MethodDescriptor {
    private static final String CONSTRUCTOR_NAME = "<init>";
    private static final String STATIC_INITIALIZER_NAME = "<clinit>";

    private static final MethodDescriptor DEFAULT_CONSTRUCTOR_DESCRIPTOR = forConstructor();
    private static final MethodDescriptor STATIC_INITIALIZER_DESCRIPTOR = forMethod("<clinit>", Type.VOID_TYPE);

    private final String name;
    private final Type type;

    public MethodDescriptor(final String name, final Type type) {
        this.name = name;
        this.type = type;
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

    public Type getType() {
        return type;
    }

    public String getDescriptor() { return type.getDescriptor(); }

    public Type getReturnType() {
        return type.getReturnType();
    }

    public List<Type> getArgumentTypes() {
        return Collections.unmodifiableList(Arrays.asList(type.getArgumentTypes()));
    }

    public boolean isConstructior() {
        return isConstructor(name);
    }

    public boolean isDefaultConstructor() {
        return isDefaultConstructor(name, type.getDescriptor());
    }

    public boolean isStaticInitializer() { return STATIC_INITIALIZER_DESCRIPTOR.equals(this); }

    @Override
    public String toString() {
        return name + type;
    }
}
