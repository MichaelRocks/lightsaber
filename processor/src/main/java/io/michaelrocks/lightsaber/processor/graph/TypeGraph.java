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
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TypeGraph {
    private Map<Type, ClassDescriptor> typeGraph = new HashMap<>();

    TypeGraph(final Map<Type, ClassDescriptor> typeGraph) {
        this.typeGraph = typeGraph;
    }

    public Set<Type> getTypes() {
        return Collections.unmodifiableSet(typeGraph.keySet());
    }

    public ClassDescriptor findClassDescriptor(final Type type) {
        return typeGraph.get(type);
    }

    public Type findSuperType(final Type type) {
        final ClassDescriptor classDescriptor = typeGraph.get(type);
        return classDescriptor == null ? null : classDescriptor.getSuperType();
    }
}
