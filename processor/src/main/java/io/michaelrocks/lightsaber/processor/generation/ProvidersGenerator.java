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

import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;

public class ProvidersGenerator {
    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    public ProvidersGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generateProviders() {
        for (final ModuleDescriptor module : processorContext.getModules()) {
            generateModuleProviders(module);
        }
    }

    private void generateModuleProviders(final ModuleDescriptor module) {
        for (final ProviderDescriptor provider : module.getProviders()) {
            // TODO: That's not the best way to ensure that the provider should be generated.
            if (provider.getProviderMethod() != null) {
                generateProvider(provider);
            }
        }
    }

    private void generateProvider(final ProviderDescriptor provider) {
        System.out.println("Generating provider " + provider.getProviderType().getInternalName());
        final ProviderClassGenerator generator = new ProviderClassGenerator(processorContext, provider);
        final byte[] providerClassData = generator.generate();
        classProducer.produceClass(provider.getProviderType().getInternalName(), providerClassData);
    }
}
