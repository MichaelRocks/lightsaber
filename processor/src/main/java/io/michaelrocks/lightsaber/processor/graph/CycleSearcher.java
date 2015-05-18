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

import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CycleSearcher {
    private final DependencyGraph graph;
    private final Map<Type, VertexColor> colors = new HashMap<>();
    private final Set<Type> cycles = new HashSet<>();

    public CycleSearcher(final DependencyGraph graph) {
        this.graph = graph;
    }

    public Collection<Type> findCycles() {
        for (final Type type : graph.getTypes()) {
            traverse(type);
        }
        return Collections.unmodifiableSet(cycles);
    }

    private void traverse(final Type type) {
        final VertexColor color = colors.get(type);
        if (color == VertexColor.BLACK) {
            return;
        }

        if (color == VertexColor.GRAY) {
            cycles.add(type);
            return;
        }

        colors.put(type, VertexColor.GRAY);
        final Collection<Type> dependencies = graph.getTypeDependencies(type);
        if (dependencies != null) {
            for (final Type dependency : dependencies) {
                traverse(dependency);
            }
        }
        colors.put(type, VertexColor.BLACK);
    }

    private enum VertexColor {
        GRAY, BLACK
    }
}
