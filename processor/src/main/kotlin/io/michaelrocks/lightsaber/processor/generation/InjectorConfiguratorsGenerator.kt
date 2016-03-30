/*
 * Copyright 2016 Michael Rozumyanskiy
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
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.Module

class InjectorConfiguratorsGenerator(
    private val classProducer: ClassProducer,
    private val processorContext: ProcessorContext,
    private val annotationCreator: AnnotationCreator
) {
  private val logger = getLogger()

  fun generateInjectorConfigurators() = processorContext.allModules.forEach { generateInjectorConfigurator(it) }

  private fun generateInjectorConfigurator(module: Module) {
    logger.debug("Generating injector configurator {}", module.configuratorType.internalName)
    val generator = InjectorConfiguratorClassGenerator(processorContext, annotationCreator, module)
    val classData = generator.generate()
    classProducer.produceClass(module.configuratorType.internalName, classData)
  }
}
