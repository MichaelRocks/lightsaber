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
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator;
import io.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;

public class TypeAgentsGenerator {
    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;
    private final AnnotationCreator annotationCreator;

    public TypeAgentsGenerator(final ClassProducer classProducer, final ProcessorContext processorContext,
            final AnnotationCreator annotationCreator) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
        this.annotationCreator = annotationCreator;
    }

    public void generateInjectors() {
        for (final InjectorDescriptor injector : processorContext.getInjectors()) {
            generateTypeAgent(injector);
        }
    }

    private void generateTypeAgent(final InjectorDescriptor injectorDescriptor) {
        final TypeAgentClassGenerator generator = new TypeAgentClassGenerator(processorContext, annotationCreator,
                injectorDescriptor);
        final byte[] injectorClassData = generator.generate();
        classProducer.produceClass(injectorDescriptor.getInjectorType().getInternalName(), injectorClassData);
    }
}
