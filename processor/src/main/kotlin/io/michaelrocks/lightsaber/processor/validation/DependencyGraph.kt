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

package io.michaelrocks.lightsaber.processor.validation

import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.Injector
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.ProcessingException
import io.michaelrocks.lightsaber.processor.graph.DirectedGraph
import io.michaelrocks.lightsaber.processor.graph.HashDirectedGraph
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint
import org.objectweb.asm.Type
import java.util.*

fun buildDependencyGraph(errorReporter: ErrorReporter, modules: Collection<Module>): DirectedGraph<Dependency> {
  return HashDirectedGraph<Dependency>().apply {
    val rootType = Dependency(GenericType.RawType(Type.getType(Injector::class.java)))
    put(rootType, emptyList<Dependency>())
    for (module in modules) {
      val providableModuleTypes = HashSet<Dependency>()
      for (provider in module.providers) {
        val returnType = provider.dependency
        if (providableModuleTypes.add(returnType)) {
          val injectees =
              (provider.provisionPoint as? ProvisionPoint.AbstractMethod)?.injectionPoint?.injectees.orEmpty()
          val dependencies = injectees.map { it.dependency }
          put(returnType, dependencies)
        } else {
          val message = "Module %s provides %s multiple times".format(module.type.internalName, returnType)
          errorReporter.reportError(ProcessingException(message))
        }
      }
    }
  }
}
