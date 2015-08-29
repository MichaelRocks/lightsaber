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

package io.michaelrocks.lightsaber.processor.commons;

import io.michaelrocks.lightsaber.Provides;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.objectweb.asm.Type;

import javax.inject.Inject;

public class Types {
    public static final Type OBJECT_TYPE = Type.getType(Object.class);
    public static final Type STRING_TYPE = Type.getType(String.class);
    public static final Type INJECT_TYPE = Type.getType(Inject.class);
    public static final Type PROVIDES_TYPE = Type.getType(Provides.class);

    public static final Type BOXED_VOID_TYPE = Type.getType(Byte.class);
    public static final Type BOXED_BOOLEAN_TYPE = Type.getType(Boolean.class);
    public static final Type BOXED_BYTE_TYPE = Type.getType(Byte.class);
    public static final Type BOXED_CHAR_TYPE = Type.getType(Character.class);
    public static final Type BOXED_DOUBLE_TYPE = Type.getType(Double.class);
    public static final Type BOXED_FLOAT_TYPE = Type.getType(Float.class);
    public static final Type BOXED_INT_TYPE = Type.getType(Integer.class);
    public static final Type BOXED_LONG_TYPE = Type.getType(Long.class);
    public static final Type BOXED_SHORT_TYPE = Type.getType(Short.class);

    private static final BidiMap<Type, Type> primitiveToBoxedMap;

    static {
        primitiveToBoxedMap = new DualHashBidiMap<>();
        primitiveToBoxedMap.put(Type.VOID_TYPE, BOXED_VOID_TYPE);
        primitiveToBoxedMap.put(Type.BOOLEAN_TYPE, BOXED_BOOLEAN_TYPE);
        primitiveToBoxedMap.put(Type.BYTE_TYPE, BOXED_BYTE_TYPE);
        primitiveToBoxedMap.put(Type.CHAR_TYPE, BOXED_CHAR_TYPE);
        primitiveToBoxedMap.put(Type.DOUBLE_TYPE, BOXED_DOUBLE_TYPE);
        primitiveToBoxedMap.put(Type.FLOAT_TYPE, BOXED_FLOAT_TYPE);
        primitiveToBoxedMap.put(Type.INT_TYPE, BOXED_INT_TYPE);
        primitiveToBoxedMap.put(Type.LONG_TYPE, BOXED_LONG_TYPE);
        primitiveToBoxedMap.put(Type.SHORT_TYPE, BOXED_SHORT_TYPE);
    }

    private Types() {
    }

    public static Type box(final Type type) {
        final Type boxedType = primitiveToBoxedMap.get(type);
        return boxedType != null ? boxedType : type;
    }

    public static Type unbox(final Type type) {
        final Type unboxedType = primitiveToBoxedMap.getKey(type);
        return unboxedType != null ? unboxedType : type;
    }

    public static boolean isPrimitive(final Type type) {
        return primitiveToBoxedMap.containsKey(type);
    }
}
