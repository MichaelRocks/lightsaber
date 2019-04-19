/*
 * Copyright 2019 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.internal;

import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import io.michaelrocks.lightsaber.ConfigurationException;
import io.michaelrocks.lightsaber.DependencyResolver;
import io.michaelrocks.lightsaber.Key;

public class ConfigurableDependencyResolver implements DependencyResolver {
  private final ConfigurableDependencyResolver parent;
  private final IterableMap<Object, Provider<?>> providers = new PolymorphicKeyHashMap<Provider<?>>();

  ConfigurableDependencyResolver(final ConfigurableDependencyResolver parent) {
    this.parent = parent;
  }

  @Nonnull
  @Override
  public <T> T getInstance(@Nonnull final Class<T> type) {
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
  public <T> T getInstance(@Nonnull final Key<T> key) {
    return getProvider(key).get();
  }

  @Nonnull
  @Override
  public <T> Provider<? extends T> getProvider(@Nonnull final Class<T> type) {
    return getProvider((Type) type);
  }

  @Nonnull
  @Override
  public <T> Provider<? extends T> getProvider(@Nonnull final Type type) {
    return getProviderInternal(type);
  }

  @Nonnull
  @Override
  public <T> Provider<? extends T> getProvider(@Nonnull final Key<T> key) {
    if (key.getQualifier() != null) {
      return getProviderInternal(key);
    } else {
      return getProviderInternal(key.getType());
    }
  }

  public <T> void registerProvider(final Class<T> type, final Provider<? extends T> provider) {
    registerProviderInternal(type, provider);
  }

  public <T> void registerProvider(final Type type, final Provider<? extends T> provider) {
    registerProviderInternal(type, provider);
  }

  public <T> void registerProvider(final Key<T> key, final Provider<? extends T> provider) {
    if (key.getQualifier() != null) {
      registerProviderInternal(key, provider);
    } else {
      registerProviderInternal(key.getType(), provider);
    }
  }

  private <T> void registerProviderInternal(final Object key, final Provider<? extends T> provider) {
    final Provider<?> oldProvider = providers.put(key, provider);
    if (oldProvider != null) {
      throw new ConfigurationException("Provider for " + key + " already registered in " + this);
    }
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  private <T> Provider<T> getProviderInternal(@Nonnull final Object key) {
    final Provider<T> provider = (Provider<T>) providers.get(key);
    if (provider == null) {
      if (parent != null) {
        try {
          return parent.getProviderInternal(key);
        } catch (final ConfigurationException exception) {
          throwConfigurationException(key, exception);
        }
      } else {
        throwConfigurationException(key, null);
      }
    }
    // noinspection ConstantConditions
    return provider;
  }

  private void throwConfigurationException(@Nonnull final Object key, final Throwable cause) {
    final ConfigurationException exception = new ConfigurationException("Provider for " + key + " not found in " + this);
    if (cause != null) {
      exception.initCause(cause);
    }
    throw exception;
  }
}
