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

package io.michaelrocks.lightsaber

import io.michaelrocks.lightsaber.processor.commons.getType

object LightsaberTypes {
  val INJECTOR_CONFIGURATOR_TYPE = getType<InjectorConfigurator>()
  val LIGHTSABER_INJECTOR_TYPE = getType<LightsaberInjector>()
  val SINGLETON_PROVIDER_TYPE = getType<SingletonProvider<*>>()
  val LAZY_ADAPTER_TYPE = getType<LazyAdapter<*>>()
}
