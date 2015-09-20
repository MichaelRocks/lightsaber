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

import io.michaelrocks.lightsaber.processor.descriptors.QualifiedType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnresolvedDependenciesSearcher {
    private final DependencyGraph graph;
    private final Set<QualifiedType> visitedTypes = new HashSet<>();
    private final List<QualifiedType> unresolvedTypes = new ArrayList<>();

    public UnresolvedDependenciesSearcher(final DependencyGraph graph) {
        this.graph = graph;
    }

    public Collection<QualifiedType> findUnresolvedDependencies() {
        for (final QualifiedType type : graph.getTypes()) {
            traverse(type);
        }
        return Collections.unmodifiableList(unresolvedTypes);
    }

    private void traverse(final QualifiedType type) {
        if (visitedTypes.add(type)) {
            final Collection<QualifiedType> dependencies = graph.getTypeDependencies(type);
            if (dependencies == null) {
                unresolvedTypes.add(type);
            } else {
                for (final QualifiedType dependency : dependencies) {
                    traverse(dependency);
                }
            }
        }
    }
}
