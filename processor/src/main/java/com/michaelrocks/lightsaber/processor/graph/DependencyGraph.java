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

package com.michaelrocks.lightsaber.processor.graph;

import com.michaelrocks.lightsaber.processor.ProcessingException;
import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyGraph {
    private final Map<Type, List<Type>> typeGraph;

    public DependencyGraph(final ProcessorContext processorContext) {
        typeGraph = new DependencyGraphBuilder(processorContext).build();
    }

    public Collection<Type> getTypes() {
        return typeGraph.keySet();
    }

    public Collection<Type> getTypeDependencies(final Type type) {
        return typeGraph.get(type);
    }

    private static final class DependencyGraphBuilder {
        private final ProcessorContext processorContext;
        private final Map<Type, List<Type>> typeGraph = new HashMap<>();

        private DependencyGraphBuilder(final ProcessorContext processorContext) {
            this.processorContext = processorContext;
        }

        Map<Type, List<Type>> build() {
            for (final ModuleDescriptor module : processorContext.getModules()) {
                final Set<Type> providableModuleTypes = new HashSet<>();
                for (final MethodDescriptor providerMethod : module.getProviderMethods()) {
                    final Type returnType = providerMethod.getType().getReturnType();
                    if (providableModuleTypes.add(returnType)) {
                        addProviderMethodToGraph(returnType, providerMethod);
                    } else {
                        final String message = String.format("Module %s provides %s multiple times",
                                module.getModuleType().getInternalName(), returnType.getInternalName());
                        processorContext.reportError(new ProcessingException(message));
                    }
                }
            }
            return typeGraph;
        }

        private void addProviderMethodToGraph(final Type type, final MethodDescriptor providerMethod) {
            if (providerMethod.isDefaultConstructor()) {
                typeGraph.put(type, Collections.<Type>emptyList());
            } else {
                final List<Type> argumentTypes = Arrays.asList(providerMethod.getType().getArgumentTypes());
                typeGraph.put(type, argumentTypes);
            }
        }
    }

}
