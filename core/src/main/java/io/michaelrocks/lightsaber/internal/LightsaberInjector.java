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

package io.michaelrocks.lightsaber.internal;

import io.michaelrocks.lightsaber.ConfigurationException;
import io.michaelrocks.lightsaber.CopyableProvider;
import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.InstanceProvider;

import javax.inject.Provider;
import java.util.IdentityHashMap;
import java.util.Map;

public class LightsaberInjector implements Injector {
    private final Injector parentInjector;
    private final Map<Class<?>, Provider<?>> providers = new IdentityHashMap<Class<?>, Provider<?>>();

    public LightsaberInjector() {
        this(null);
    }

    public LightsaberInjector(final Injector parentInjector) {
        this.parentInjector = parentInjector;
        registerProvider(Injector.class, new InstanceProvider<Injector>(this));
        if (parentInjector != null) {
            copyParentProviders();
        }
    }

    private void copyParentProviders() {
        for (final Map.Entry<Class<?>, Provider<?>> entry : parentInjector.getAllProviders().entrySet()) {
            if (entry.getKey() != Injector.class && entry.getValue() instanceof CopyableProvider) {
                // noinspection unchecked
                registerProvider(
                        (Class<Object>) entry.getKey(),
                        ((CopyableProvider<Object>) entry.getValue()).copyWithInjector(this));
            }
        }
    }

    @Override
    public void injectMembers(final Object target) {
        throw new IllegalStateException(
                "This method must not be called. It must be substituted with a valid injector instead.");
    }

    @Override
    public <T> T getInstance(final Class<T> type) {
        return getProvider(type).get();
    }

    @Override
    public <T> Provider<T> getProvider(final Class<T> type) {
        // noinspection unchecked
        final Provider<T> provider = (Provider<T>) providers.get(type);
        if (provider == null) {
            if (parentInjector == null) {
                throw new ConfigurationException("Provider for " + type + " not found");
            } else {
                return parentInjector.getProvider(type);
            }
        }
        return provider;
    }

    @Override
    public Map<Class<?>, Provider<?>> getAllProviders() {
        return new IdentityHashMap<Class<?>, Provider<?>>(providers);
    }

    public <T> void registerProvider(final Class<T> type, final CopyableProvider<T> provider) {
        final Provider<?> oldProvider = providers.put(type, provider);
        if (oldProvider != null) {
            throw new ConfigurationException("Provider for " + type + " already registered");
        }
    }
}
