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
import io.michaelrocks.lightsaber.LazyAdapter;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import org.objectweb.asm.Type;

class GenerationHelper {
    private static final Type LAZY_TYPE = Type.getType(Lazy.class);
    private static final Type LAZY_ADAPTER_TYPE = Type.getType(LazyAdapter.class);

    private static final MethodDescriptor LAZY_ADAPTER_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.PROVIDER_TYPE);

    private GenerationHelper() {
    }

    static void convertDependencyToTargetType(final GeneratorAdapter generator, final TypeSignature type) {
        if (type.isParameterized()) {
            if (LAZY_TYPE.equals(type.getRawType())) {
                generator.newInstance(LAZY_ADAPTER_TYPE);
                generator.dupX1();
                generator.swap();
                generator.invokeConstructor(LAZY_ADAPTER_TYPE, LAZY_ADAPTER_CONSTRUCTOR);
            }
        } else {
            generator.unbox(type.getRawType());
        }
    }
}
