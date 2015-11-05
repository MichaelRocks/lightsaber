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
import io.michaelrocks.lightsaber.processor.descriptors.PackageInvaderDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PackageInvadersGenerator {
    private static final Logger logger = LoggerFactory.getLogger(PackageInvadersGenerator.class);

    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    public PackageInvadersGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generatePackageInvaders() {
        for (final PackageInvaderDescriptor packageInvader : processorContext.getPackageInvaders()) {
            generatePackageInvaders(packageInvader);
        }
    }

    private void generatePackageInvaders(final PackageInvaderDescriptor packageInvader) {
        logger.debug("Generating package invader {}", packageInvader.getType());
        final PackageInvaderClassGenerator generator =
                new PackageInvaderClassGenerator(processorContext, packageInvader);
        final byte[] classData = generator.generate();
        classProducer.produceClass(packageInvader.getType().getInternalName(), classData);
    }
}
