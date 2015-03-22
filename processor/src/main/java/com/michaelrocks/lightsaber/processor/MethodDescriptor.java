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

package com.michaelrocks.lightsaber.processor;

import org.objectweb.asm.Type;

public class MethodDescriptor {
    private static final String CONSTRUCTOR_NAME = "<init>";

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

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }
}
