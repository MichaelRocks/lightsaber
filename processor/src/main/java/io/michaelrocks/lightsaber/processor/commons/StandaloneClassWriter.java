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

import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.graph.TypeGraph;
import org.apache.commons.collections4.iterators.IteratorIterable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public class StandaloneClassWriter extends ClassWriter {
    private static final Type OBJECT_TYPE = Type.getType(Object.class);

    private final ProcessorContext processorContext;

    public StandaloneClassWriter(final int flags, final ProcessorContext processorContext) {
        super(flags);
        this.processorContext = processorContext;
    }

    public StandaloneClassWriter(final ClassReader classReader, final int flags,
            final ProcessorContext processorContext) {
        super(classReader, flags);
        this.processorContext = processorContext;
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        final Set<Type> hierarchy = new HashSet<>();
        for (final Type type : traverseTypeHierarchy(Type.getObjectType(type1))) {
            hierarchy.add(type);
        }

        for (final Type type : traverseTypeHierarchy(Type.getObjectType(type2))) {
            if (hierarchy.contains(type)) {
                System.out.println("[getCommonSuperClass]: " + type1 + " & " + type2 + " = " + type);
                return type.getInternalName();
            }
        }

        System.out.println("[getCommonSuperClass]: " + type1 + " & " + type2 + " = NOT FOUND");
        return OBJECT_TYPE.getInternalName();
    }

    private Iterable<Type> traverseTypeHierarchy(final Type type) {
        return new IteratorIterable<>(new TypeHierarchyIterator(processorContext.getTypeGraph(), type));
    }

    private static class TypeHierarchyIterator implements Iterator<Type> {
        private final TypeGraph typeGraph;
        private Type type;

        public TypeHierarchyIterator(final TypeGraph typeGraph, final Type type) {
            this.typeGraph = typeGraph;
            this.type = type;
        }

        @Override
        public boolean hasNext() {
            return type != null;
        }

        @Override
        public Type next() {
            if (type == null) {
                throw new NoSuchElementException();
            }

            final Type result = type;
            if (OBJECT_TYPE.equals(type)) {
                type = null;
            } else {
                type = typeGraph.findSuperType(type);
            }
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
