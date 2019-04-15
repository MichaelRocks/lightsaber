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
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import io.michaelrocks.lightsaber.ConfigurationException;
import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.Key;
import io.michaelrocks.lightsaber.ProviderInterceptor;

public class LightsaberInjector implements Injector {
  private final LightsaberInjector parent;
  private final List<ProviderInterceptor> interceptors;
  private final IterableMap<Object, Provider<?>> providers = new PolymorphicKeyHashMap<Provider<?>>();

  public LightsaberInjector(@Nonnull final Object component, final LightsaberInjector parent, final List<ProviderInterceptor> interceptors) {
    this.parent = parent;
    this.interceptors = interceptors;
    registerProvider(Injector.class, new Provider<Injector>() {
      @Override
      public Injector get() {
        return LightsaberInjector.this;
      }
    });

    final InjectorConfigurator configurator = (InjectorConfigurator) component;
    configurator.configureInjector(this);
  }

  @Nonnull
  @Override
  public Injector createChildInjector(@Nonnull final Object component) {
    // noinspection ConstantConditions
    if (component == null) {
      throw new NullPointerException("Trying to create an injector with a null component");
    }

    return new LightsaberInjector(component, this, interceptors);
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
    if (interceptors == null) {
      return getProviderInternal(type);
    }

    // noinspection unchecked
    return (Provider<T>) new ProviderResolutionChain().proceed(Key.of(type));
  }

  @Nonnull
  @Override
  public <T> Provider<T> getProvider(@Nonnull final Key<? extends T> key) {
    if (interceptors == null) {
      if (key.getQualifier() != null) {
        return getProviderInternal(key);
      } else {
        return getProvider(key.getType());
      }
    }

    // noinspection unchecked
    return (Provider<T>) new ProviderResolutionChain().proceed(key);
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
    if (key.getQualifier() != null) {
      registerProviderInternal(key, provider);
    } else {
      registerProviderInternal(key.getType(), provider);
    }
  }

  @Nonnull
  private <T> Provider<T> getProviderInternal(@Nonnull final Object key) {
    // noinspection unchecked
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

  private class ProviderResolutionChain implements ProviderInterceptor.Chain {
    private int index = interceptors.size();

    @Nonnull
    @Override
    public Injector injector() {
      return LightsaberInjector.this;
    }

    @Nonnull
    @Override
    public Provider<?> proceed(@Nonnull final Key<?> key) {
      // noinspection ConstantConditions
      if (key == null) {
        throw new NullPointerException("Key is null");
      }

      index -= 1;
      if (index >= 0) {
        final ProviderInterceptor interceptor = interceptors.get(index);
        return interceptor.intercept(this, key);
      } else {
        if (key.getQualifier() != null) {
          return getProviderInternal(key);
        } else {
          return getProviderInternal(key.getType());
        }
      }
    }
  }
}
