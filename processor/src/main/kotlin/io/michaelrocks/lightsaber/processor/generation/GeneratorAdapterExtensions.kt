/*
 * Copyright 2018 Michael Rozumyanskiy
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

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.Key
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.model.Converter
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.Injectee
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.Scope

private val PROVIDER_GET_METHOD = MethodDescriptor.forMethod("get", Types.OBJECT_TYPE)
private val ADAPTER_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.PROVIDER_TYPE)

private val GET_PROVIDER_FOR_CLASS_METHOD =
    MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.CLASS_TYPE)
private val GET_PROVIDER_FOR_TYPE_METHOD =
    MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.TYPE_TYPE)
private val GET_PROVIDER_FOR_KEY_METHOD =
    MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.KEY_TYPE)

private val GET_INSTANCE_FOR_CLASS_METHOD =
    MethodDescriptor.forMethod("getInstance", Types.OBJECT_TYPE, Types.CLASS_TYPE)
private val GET_INSTANCE_FOR_TYPE_METHOD =
    MethodDescriptor.forMethod("getInstance", Types.OBJECT_TYPE, Types.TYPE_TYPE)
private val GET_INSTANCE_FOR_KEY_METHOD =
    MethodDescriptor.forMethod("getInstance", Types.OBJECT_TYPE, Types.KEY_TYPE)

private val REGISTER_PROVIDER_FOR_CLASS_METHOD =
    MethodDescriptor.forMethod("registerProvider", Type.Primitive.Void, Types.CLASS_TYPE, Types.PROVIDER_TYPE)
private val REGISTER_PROVIDER_FOR_TYPE_METHOD =
    MethodDescriptor.forMethod("registerProvider", Type.Primitive.Void, Types.TYPE_TYPE, Types.PROVIDER_TYPE)
private val REGISTER_PROVIDER_FOR_KEY_METHOD =
    MethodDescriptor.forMethod("registerProvider", Type.Primitive.Void, Types.KEY_TYPE, Types.PROVIDER_TYPE)

private val DELEGATE_PROVIDER_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.PROVIDER_TYPE)

fun GeneratorAdapter.getDependency(keyRegistry: KeyRegistry, injectee: Injectee) {
  when (injectee.converter) {
    is Converter.Identity -> {
      getProvider(keyRegistry, injectee.dependency)
    }
    is Converter.Instance -> {
      getInstance(keyRegistry, injectee.dependency)
      unbox(injectee.dependency.type.rawType)
    }
    is Converter.Adapter -> {
      getProvider(keyRegistry, injectee.dependency)
      newInstance(injectee.converter.adapterType)
      dupX1()
      swap()
      invokeConstructor(injectee.converter.adapterType, ADAPTER_CONSTRUCTOR)
    }
  }
}

fun GeneratorAdapter.getProvider(keyRegistry: KeyRegistry, dependency: Dependency) {
  val key = getKey(keyRegistry, dependency)
  getStatic(keyRegistry.type, key.field)

  when (key) {
    is Key.Class -> invokeInterface(Types.INJECTOR_TYPE, GET_PROVIDER_FOR_CLASS_METHOD)
    is Key.Type -> invokeInterface(Types.INJECTOR_TYPE, GET_PROVIDER_FOR_TYPE_METHOD)
    is Key.QualifiedType -> invokeInterface(Types.INJECTOR_TYPE, GET_PROVIDER_FOR_KEY_METHOD)
  }
}

fun GeneratorAdapter.getInstance(keyRegistry: KeyRegistry, dependency: Dependency) {
  val key = getKey(keyRegistry, dependency)
  getStatic(keyRegistry.type, key.field)

  when (key) {
    is Key.Class -> invokeInterface(Types.INJECTOR_TYPE, GET_INSTANCE_FOR_CLASS_METHOD)
    is Key.Type -> invokeInterface(Types.INJECTOR_TYPE, GET_INSTANCE_FOR_TYPE_METHOD)
    is Key.QualifiedType -> invokeInterface(Types.INJECTOR_TYPE, GET_INSTANCE_FOR_KEY_METHOD)
  }
}

fun GeneratorAdapter.registerProvider(keyRegistry: KeyRegistry, provider: Provider, providerCreator: () -> Unit) {
  val key = getKey(keyRegistry, provider.dependency)
  getStatic(keyRegistry.type, key.field)

  when (provider.scope) {
    is Scope.Class -> newDelegator(provider.scope.scopeType, providerCreator)
    is Scope.None -> providerCreator()
  }

  when (key) {
    is Key.Class -> invokeVirtual(LightsaberTypes.LIGHTSABER_INJECTOR_TYPE, REGISTER_PROVIDER_FOR_CLASS_METHOD)
    is Key.Type -> invokeVirtual(LightsaberTypes.LIGHTSABER_INJECTOR_TYPE, REGISTER_PROVIDER_FOR_TYPE_METHOD)
    is Key.QualifiedType -> invokeVirtual(LightsaberTypes.LIGHTSABER_INJECTOR_TYPE, REGISTER_PROVIDER_FOR_KEY_METHOD)
  }
}

private fun GeneratorAdapter.newDelegator(scopeType: Type, providerCreator: () -> Unit) {
  newInstance(scopeType)
  dup()
  providerCreator()
  invokeConstructor(scopeType, DELEGATE_PROVIDER_CONSTRUCTOR)
}

private fun getKey(keyRegistry: KeyRegistry, dependency: Dependency): Key {
  return keyRegistry.keys[dependency.box()] ?: error("Key for $dependency not found")
}
