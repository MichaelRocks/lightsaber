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
import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.Key;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.Map;

public class LightsaberInjector implements Injector {
    private static final Key<Injector> INJECTOR_KEY = new Key<Injector>(Injector.class);

    private final Injector parentInjector;
    private final Map<Key<?>, Provider<?>> providers = new HashMap<Key<?>, Provider<?>>();

    public LightsaberInjector() {
        this(null);
    }

    public LightsaberInjector(final Injector parentInjector) {
        this.parentInjector = parentInjector;
        registerProvider(INJECTOR_KEY, new InstanceProvider<Injector>(this));
        if (parentInjector != null) {
            copyParentProviders();
        }
    }

    private void copyParentProviders() {
        for (final Map.Entry<Key<?>, Provider<?>> entry : parentInjector.getAllProviders().entrySet()) {
            if (!INJECTOR_KEY.equals(entry.getKey()) && entry.getValue() instanceof CopyableProvider) {
                // noinspection unchecked
                registerProvider(
                        (Key<Object>) entry.getKey(),
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
    public <T> T getInstance(final Key<? extends T> key) {
        return getProvider(key).get();
    }

    @Override
    public <T> Provider<T> getProvider(final Key<? extends T> key) {
        // noinspection unchecked
        final Provider<T> provider = (Provider<T>) providers.get(key);
        if (provider == null) {
            if (parentInjector == null) {
                throw new ConfigurationException("Provider for " + key + " not found");
            } else {
                return parentInjector.getProvider(key);
            }
        }
        return provider;
    }

    @Override
    public Map<Key<?>, Provider<?>> getAllProviders() {
        return new HashMap<Key<?>, Provider<?>>(providers);
    }

    public <T> void registerProvider(final Key<T> key, final CopyableProvider<T> provider) {
        final Provider<?> oldProvider = providers.put(key, provider);
        if (oldProvider != null) {
            throw new ConfigurationException("Provider for " + key + " already registered");
        }
    }
}
