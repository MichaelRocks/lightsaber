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

package com.michaelrocks.lightsaber.processor.generation;

import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProviderGenerator {
    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    private final Map<Type, ProviderDescriptor> generatedProviders = new HashMap<>();

    public ProviderGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generateProviders() {
        for (final ModuleDescriptor module : processorContext.getModules()) {
            generateModuleProviders(module);
        }
    }

    public ProviderDescriptor getProviderForType(final Type type) {
        return generatedProviders.get(type);
    }

    private void generateModuleProviders(final ModuleDescriptor module) {
        final List<ProviderDescriptor> providers = createProviderDescriptorsForModule(module);
        for (final ProviderDescriptor provider : providers) {
            generateProvider(provider);
        }
    }

    private static List<ProviderDescriptor> createProviderDescriptorsForModule(final ModuleDescriptor module) {
        final List<ProviderDescriptor> providers = new ArrayList<>();

        final Type moduleType = module.getModuleType();
        final String moduleName = moduleType.getInternalName();
        final List<MethodDescriptor> providerMethods = module.getProviderMethods();
        for (int i = 0; i < providerMethods.size(); ++i) {
            final Type providerType = Type.getObjectType(moduleName + "$$Provider$$" + (i + 1));
            final MethodDescriptor providerMethod = providerMethods.get(i);
            final ProviderDescriptor provider =
                    new ProviderDescriptor(providerType, providerMethod.getReturnType(), providerMethod, moduleType);
            providers.add(provider);
        }

        return providers;
    }

    private void generateProvider(final ProviderDescriptor provider) {
        System.out.println("Generating provider " + provider.getProviderType().getInternalName());
        final ProviderClassGenerator generator = new ProviderClassGenerator(
                provider.getProviderType(), provider.getModuleType(), provider.getProviderMethod());
        final byte[] providerClassData = generator.generate();
        classProducer.produceClass(provider.getProviderType().getInternalName(), providerClassData);
        registerProvider(provider);
    }

    private void registerProvider(final ProviderDescriptor provider) {
        generatedProviders.put(provider.getProvidableType(), provider);
    }
}
