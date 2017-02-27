/*
 * Copyright 2017 Michael Rozumyanskiy
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

import io.michaelrocks.lightsaber.internal.FastHashMap;
import io.michaelrocks.lightsaber.internal.InjectingProvider;
import io.michaelrocks.lightsaber.internal.IterableMap;
import io.michaelrocks.lightsaber.internal.TypeUtils;

import javax.annotation.Nonnull;
import javax.inject.Provider;
import java.lang.reflect.Type;

class LightsaberInjector implements Injector {
  private final Lightsaber lightsaber;
  private final IterableMap<Object, InjectingProvider<?>> providers = new FastHashMap<Object, InjectingProvider<?>>() {
    @Override
    protected int hashCode(final Object key) {
      if (key instanceof Class<?>) {
        return key.hashCode();
      } if (key instanceof Type) {
        return TypeUtils.hashCode((Type) key);
      } else {
        return key.hashCode();
      }
    }

    @Override
    protected boolean areKeysEqual(final Object key1, final Object key2) {
      if (key1 == key2) {
        return true;
      }

      if (key1 instanceof Class<?> && key2 instanceof Class<?>) {
        return key1.equals(key2);
      } if (key1 instanceof Type && key2 instanceof Type) {
        return TypeUtils.equals((Type) key1, (Type) key2);
      } else {
        return key1.equals(key2);
      }
    }
  };

  LightsaberInjector(@Nonnull final Lightsaber lightsaber) {
    this.lightsaber = lightsaber;
    registerProvider(Injector.class, new InjectingProvider<Injector>() {
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
  public void injectMembers(@Nonnull final Object target) {
    lightsaber.injectMembers(this, target);
  }

  @Nonnull
  @Override
  public <T> T getInstance(@Nonnull final Class<? extends T> type) {
    return getProvider(type).get();
  }

  @Nonnull
  @Override
  public <T> T getInstance(@Nonnull final Type type) {
    // noinspection unchecked
    return (T) getProvider(type).get();
  }

  @Nonnull
  @Override
  public <T> T getInstance(@Nonnull final Key<? extends T> key) {
    return getProvider(key).get();
  }

  @Nonnull
  @Override
  public <T> Provider<T> getProvider(@Nonnull final Class<? extends T> type) {
    return getProviderInternal(type);
  }

  @Nonnull
  @Override
  public <T> Provider<T> getProvider(@Nonnull final Type type) {
    return getProviderInternal(type);
  }

  @Nonnull
  @Override
  public <T> Provider<T> getProvider(@Nonnull final Key<? extends T> key) {
    if (key.getQualifier() == null) {
      return getProviderInternal(key.getType());
    } else {
      return getProviderInternal(key);
    }
  }

  private <T> Provider<T> getProviderInternal(final Object key) {
    // noinspection unchecked
    final Provider<T> provider = (Provider<T>) providers.get(key);
    if (provider == null) {
      throw new ConfigurationException("Provider for " + key + " not found in " + this);
    }
    return provider;
  }

  @Nonnull
  public IterableMap<Object, InjectingProvider<?>> getProviders() {
    return providers;
  }

  <T> void registerProvider(final Class<? extends T> type, final InjectingProvider<? extends T> provider) {
    registerProviderInternal(type, provider);
  }

  <T> void registerProvider(final Type type, final InjectingProvider<? extends T> provider) {
    registerProviderInternal(type, provider);
  }

  <T> void registerProvider(final Key<T> key, final InjectingProvider<? extends T> provider) {
    if (key.getQualifier() == null) {
      registerProviderInternal(key.getType(), provider);
    } else {
      registerProviderInternal(key, provider);
    }
  }

  <T> void registerProviderInternal(final Object key, final InjectingProvider<? extends T> provider) {
    final Provider<?> oldProvider = providers.put(key, provider);
    if (oldProvider != null) {
      throw new ConfigurationException("Provider for " + key + " already registered in " + this);
    }
  }
}
