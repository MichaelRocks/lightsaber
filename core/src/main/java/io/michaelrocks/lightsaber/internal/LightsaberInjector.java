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

package io.michaelrocks.lightsaber.internal;

import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import io.michaelrocks.lightsaber.ConfigurationException;
import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.Key;

public class LightsaberInjector implements Injector {
  private final Injector parent;
  private final IterableMap<Object, Provider<?>> providers = new PolymorphicKeyHashMap<Provider<?>>();

  public LightsaberInjector(final Injector parent) {
    this.parent = parent;
    registerProvider(Injector.class, new Provider<Injector>() {
      @Override
      public Injector get() {
        return LightsaberInjector.this;
      }
    });
  }

  @Override
  public void injectMembers(@Nonnull final Object target) {
    if (target instanceof MembersInjector) {
      final MembersInjector membersInjector = (MembersInjector) target;
      membersInjector.injectFields(this);
      membersInjector.injectMethods(this);
    }
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
    return getProvider((Type) type);
  }

  @Nonnull
  @Override
  public <T> Provider<T> getProvider(@Nonnull final Type type) {
    // noinspection unchecked
    final Provider<T> provider = (Provider<T>) providers.get(type);
    if (provider == null) {
      if (parent != null) {
        try {
          return parent.getProvider(type);
        } catch (final ConfigurationException exception) {
          throwConfigurationException(type, exception);
        }
      } else {
        throwConfigurationException(type, null);
      }
    }
    // noinspection ConstantConditions
    return provider;
  }

  @Nonnull
  @Override
  public <T> Provider<T> getProvider(@Nonnull final Key<? extends T> key) {
    if (key.getQualifier() == null) {
      return getProvider(key.getType());
    }

    // noinspection unchecked
    final Provider<T> provider = (Provider<T>) providers.get(key);
    if (provider == null) {
      if (parent != null) {
        try {
          return parent.getProvider(key);
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

  @Nonnull
  public IterableMap<Object, Provider<?>> getProviders() {
    return providers;
  }

  public <T> void registerProvider(final Class<? extends T> type, final Provider<? extends T> provider) {
    registerProviderInternal(type, provider);
  }

  public <T> void registerProvider(final Type type, final Provider<? extends T> provider) {
    registerProviderInternal(type, provider);
  }

  public <T> void registerProvider(final Key<T> key, final Provider<? extends T> provider) {
    if (key.getQualifier() == null) {
      registerProviderInternal(key.getType(), provider);
    } else {
      registerProviderInternal(key, provider);
    }
  }

  private <T> void registerProviderInternal(final Object key, final Provider<? extends T> provider) {
    final Provider<?> oldProvider = providers.put(key, provider);
    if (oldProvider != null) {
      throw new ConfigurationException("Provider for " + key + " already registered in " + this);
    }
  }

  private void throwConfigurationException(@Nonnull final Object key, final Throwable cause) {
    final ConfigurationException exception =
        new ConfigurationException("Provider for " + key + " not found in " + this);
    if (cause != null) {
      exception.initCause(cause);
    }
    throw exception;
  }
}
