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

package com.michaelrocks.lightsaber.internal;

import com.michaelrocks.lightsaber.ConfigurationException;
import com.michaelrocks.lightsaber.Injector;

import javax.inject.Provider;
import java.util.IdentityHashMap;
import java.util.Map;

public class LightsaberInjector implements Injector {
    private final Map<Class<?>, Provider<?>> providers = new IdentityHashMap<Class<?>, Provider<?>>();

    public LightsaberInjector() {
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
            throw new ConfigurationException("Provider for " + type + " not found");
        }
        return provider;
    }

    public <T> void registerProvider(final Class<T> type, final Provider<T> provider) {
        final Provider<?> oldProvider = providers.put(type, provider);
        if (oldProvider != null) {
            throw new ConfigurationException("Provider for " + type + " already registered");
        }
    }
}
