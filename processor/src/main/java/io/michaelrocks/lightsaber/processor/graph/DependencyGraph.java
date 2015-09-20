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

import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.processor.ProcessingException;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedType;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyGraph {
    private final Map<QualifiedType, List<QualifiedType>> typeGraph = new HashMap<>();

    public DependencyGraph(final ProcessorContext processorContext, final Collection<ModuleDescriptor> modules) {
        final QualifiedType rootType = new QualifiedType(Type.getType(Injector.class));
        typeGraph.put(rootType, Collections.<QualifiedType>emptyList());
        for (final ModuleDescriptor module : modules) {
            final Set<QualifiedType> providableModuleTypes = new HashSet<>();
            for (final ProviderDescriptor provider : module.getProviders()) {
                final QualifiedType returnType = provider.getQualifiedProvidableType();
                if (providableModuleTypes.add(returnType)) {
                    typeGraph.put(returnType, provider.getDependencies());
                } else {
                    final String message = String.format("Module %s provides %s multiple times",
                            module.getModuleType().getInternalName(), returnType);
                    processorContext.reportError(new ProcessingException(message));
                }
            }
        }
    }

    public Collection<QualifiedType> getTypes() {
        return typeGraph.keySet();
    }

    public Collection<QualifiedType> getTypeDependencies(final QualifiedType type) {
        return typeGraph.get(type);
    }
}
