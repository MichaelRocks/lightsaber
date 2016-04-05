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

import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.model.Converter
import io.michaelrocks.lightsaber.processor.model.Injectee

private val PROVIDER_GET_METHOD = MethodDescriptor.forMethod("get", Types.OBJECT_TYPE)
private val ADAPTER_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.PROVIDER_TYPE)

fun GeneratorAdapter.convertDependencyToTargetType(injectee: Injectee) {
  when (injectee.converter) {
    is Converter.Identity -> {} // Do nothing.
    is Converter.Instance -> {
      invokeInterface(Types.PROVIDER_TYPE, PROVIDER_GET_METHOD)
      unbox(injectee.dependency.type.rawType)
    }
    is Converter.Adapter -> {
      newInstance(injectee.converter.adapterType)
      dupX1()
      swap()
      invokeConstructor(injectee.converter.adapterType, ADAPTER_CONSTRUCTOR)
    }
  }
}
