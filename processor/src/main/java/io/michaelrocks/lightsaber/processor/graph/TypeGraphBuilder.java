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

package io.michaelrocks.lightsaber.processor.graph;

import io.michaelrocks.lightsaber.processor.descriptors.ClassDescriptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ASM5;

public class TypeGraphBuilder extends ClassVisitor {
    private final Map<Type, ClassDescriptor> classDescriptors = new HashMap<>();

    public TypeGraph build() {
        return new TypeGraph(classDescriptors);
    }

    public TypeGraphBuilder() {
        super(ASM5);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        final ClassDescriptor classDescriptor = new ClassDescriptor(name, superName, interfaces);
        classDescriptors.put(classDescriptor.getClassType(), classDescriptor);
    }
}
