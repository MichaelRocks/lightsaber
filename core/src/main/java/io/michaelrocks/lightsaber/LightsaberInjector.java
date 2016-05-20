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

import io.michaelrocks.lightsaber.internal.InjectingProvider;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

class LightsaberInjector implements Injector {
  static final Key<Injector> INJECTOR_KEY = new Key<Injector>(Injector.class);

  private final Lightsaber lightsaber;
  private final Map<Key<?>, InjectingProvider<?>> providers = new HashMap<Key<?>, InjectingProvider<?>>();

  public LightsaberInjector(final Lightsaber lightsaber) {
    this.lightsaber = lightsaber;
    registerProvider(INJECTOR_KEY, new InjectingProvider<Injector>() {
      @Override
      public Injector get() {
        return LightsaberInjector.this;
      }

      @Override
      public Injector getWithInjector(final Injector injector) {
        return LightsaberInjector.this;
      }
    });
  }

  @Override
  public void injectMembers(final Object target) {
    lightsaber.injectMembers(this, target);
  }

  @Override
  public <T> T getInstance(final Key<? extends T> key) {
    return getProvider(key).get();
  }

  @Override
  public <T> Provider<T> getProvider(final Key<? extends T> key) {
    // noinspection unchecked
    final Provider<T> provider = (Provider<T>) providers.get(key);
    if (provider == null) {
      throw new ConfigurationException("Provider for " + key + " not found in " + this);
    }
    return provider;
  }

  public Map<Key<?>, InjectingProvider<?>> getProviders() {
    return providers;
  }

  public <T> void registerProvider(final Key<T> key, final InjectingProvider<? extends T> provider) {
    final Provider<?> oldProvider = providers.put(key, provider);
    if (oldProvider != null) {
      throw new ConfigurationException("Provider for " + key + " already registered in " + this);
    }
  }
}
