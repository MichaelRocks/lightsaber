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

package io.michaelrocks.lightsaber;

import io.michaelrocks.lightsaber.internal.AbstractInjectingProvider;
import io.michaelrocks.lightsaber.internal.InjectingProvider;

class InjectorOverridingProvider<T> extends AbstractInjectingProvider<T> {
  private final InjectingProvider<T> provider;

  InjectorOverridingProvider(final InjectingProvider<T> provider, final Injector injector) {
    super(injector);
    this.provider = provider;
  }

  @Override
  public T getWithInjector(final Injector injector) {
    return provider.getWithInjector(injector);
  }
}
