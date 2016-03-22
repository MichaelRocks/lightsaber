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

package io.michaelrocks.lightsaber.processor.graph

import io.michaelrocks.lightsaber.Injector
import io.michaelrocks.lightsaber.processor.ProcessingException
import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedType
import io.michaelrocks.lightsaber.processor.descriptors.dependencies
import org.objectweb.asm.Type
import java.util.*

class DependencyGraph(processorContext: ProcessorContext, modules: Collection<ModuleDescriptor>) {
  private val typeGraph = HashMap<QualifiedType, List<QualifiedType>>()

  val types: Collection<QualifiedType>
    get() = typeGraph.keys

  init {
    val rootType = QualifiedType(Type.getType(Injector::class.java))
    typeGraph.put(rootType, emptyList<QualifiedType>())
    for (module in modules) {
      val providableModuleTypes = HashSet<QualifiedType>()
      for (provider in module.providers) {
        val returnType = provider.qualifiedProvidableType
        if (providableModuleTypes.add(returnType)) {
          typeGraph.put(returnType, provider.dependencies)
        } else {
          val message = "Module %s provides %s multiple times".format(module.moduleType.internalName, returnType)
          processorContext.reportError(ProcessingException(message))
        }
      }
    }
  }

  fun getTypeDependencies(type: QualifiedType): Collection<QualifiedType>? = typeGraph[type]
}
