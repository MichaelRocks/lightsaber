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
import com.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

public class InjectorsGenerator {
    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    private final Map<Type, InjectorDescriptor> generatedInjectors = new HashMap<>();

    public InjectorsGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generateInjectors() {
        for (final InjectionTargetDescriptor injectableTarget : processorContext.getInjectableTargets()) {
            final Type injectorType =
                    Type.getObjectType(injectableTarget.getTargetType().getInternalName() + "$$Injector");
            final InjectorDescriptor injector = new InjectorDescriptor(injectorType, injectableTarget);
            generateInjector(injector);
        }
    }

    public InjectorDescriptor getInjectorForType(final Type type) {
        return generatedInjectors.get(type);
    }

    private void generateInjector(final InjectorDescriptor injectorDescriptor) {
        final InjectorClassGenerator generator = new InjectorClassGenerator(injectorDescriptor);
        final byte[] injectorClassData = generator.generate();
        classProducer.produceClass(injectorDescriptor.getInjectorType().getInternalName(), injectorClassData);
        registerInjector(injectorDescriptor);
    }

    private void registerInjector(final InjectorDescriptor injector) {
        generatedInjectors.put(injector.getInjectableTarget().getTargetType(), injector);
    }
}
