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

package io.michaelrocks.lightsaber.processor.generation;

import io.michaelrocks.lightsaber.Lazy;
import io.michaelrocks.lightsaber.internal.LazyAdapter;
import io.michaelrocks.lightsaber.processor.commons.Boxer;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

class GenerationHelper {
    private static final String LAZY_TYPE_NAME = Type.getInternalName(Lazy.class);
    private static final String LAZY_ADAPTER_TYPE_NAME = Type.getInternalName(LazyAdapter.class);

    private static final MethodDescriptor LAZY_ADAPTER_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.PROVIDER_TYPE);

    private GenerationHelper() {
    }

    static void convertDependencyToTargetType(final MethodVisitor methodVisitor, final TypeSignature type) {
        if (type.isParameterized()) {
            if (LAZY_TYPE_NAME.equals(type.getRawType().getInternalName())) {
                methodVisitor.visitTypeInsn(NEW, LAZY_ADAPTER_TYPE_NAME);
                methodVisitor.visitInsn(DUP_X1);
                methodVisitor.visitInsn(SWAP);
                methodVisitor.visitMethodInsn(
                        INVOKESPECIAL,
                        LAZY_ADAPTER_TYPE_NAME,
                        LAZY_ADAPTER_CONSTRUCTOR.getName(),
                        LAZY_ADAPTER_CONSTRUCTOR.getDescriptor(),
                        false);
            }
        } else {
            GenerationHelper.generateTypeCast(methodVisitor, type);
        }
    }

    private static void generateTypeCast(final MethodVisitor methodVisitor, final TypeSignature type) {
        if (!type.isParameterized()) {
            final Type boxedType = Types.box(type.getRawType());
            methodVisitor.visitTypeInsn(CHECKCAST, boxedType.getInternalName());
            if (!type.getRawType().equals(boxedType)) {
                Boxer.unbox(methodVisitor, boxedType);
            }
        }
    }
}
