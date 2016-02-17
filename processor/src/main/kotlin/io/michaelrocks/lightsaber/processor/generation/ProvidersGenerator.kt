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

package io.michaelrocks.lightsaber.processor.generation

import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor
import io.michaelrocks.lightsaber.processor.logging.getLogger

class ProvidersGenerator(
    private val classProducer: ClassProducer,
    private val processorContext: ProcessorContext,
    private val annotationCreator: AnnotationCreator
) {
  private val logger = getLogger()

  fun generateProviders() = processorContext.allModules.forEach { generateModuleProviders(it) }

  private fun generateModuleProviders(module: ModuleDescriptor) = module.providers.forEach { generateProvider(it) }

  private fun generateProvider(provider: ProviderDescriptor) {
    logger.debug("Generating provider {}", provider.providerType.internalName)
    val generator = ProviderClassGenerator(processorContext.classRegistry, annotationCreator, provider)
    val providerClassData = generator.generate()
    classProducer.produceClass(provider.providerType.internalName, providerClassData)
  }
}
