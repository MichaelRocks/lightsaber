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

package io.michaelrocks.lightsaber;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Map;

public class Lightsaber {
    private static final Configurator DEFAULT_CONFIGURATOR = new DefaultConfigurator();

    private static volatile Lightsaber instance;
    private static final Object instanceLock = new Object();

    private final Map<Class<?>, MembersInjector<?>> membersInjectors;
    private final Object[] packageModules;

    Lightsaber(final Configurator configurator) {
        membersInjectors = configurator.getMembersInjectors();
        packageModules = configurator.getPackageModules();
    }

    public static Lightsaber getInstance() {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new Lightsaber(DEFAULT_CONFIGURATOR);
                }
            }
        }
        return instance;
    }

    public static Injector createInjector(final Object... modules) {
        throw new IllegalStateException(
                "This method must not be called. Seems the project hasn't been processed with Lightsaber");
    }

    public static <T> T getInstance(final Injector injector, final Class<? extends T> type) {
        return injector.getInstance(Key.of(type));
    }

    public static <T> T getInstance(final Injector injector, final Class<? extends T> type,
            final Annotation annotation) {
        return injector.getInstance(Key.of(type, annotation));
    }

    public static <T> Provider<T> getProvider(final Injector injector, final Class<? extends T> type) {
        return injector.getProvider(Key.of(type));
    }

    public static <T> Provider<T> getProvider(final Injector injector, final Class<? extends T> type,
            final Annotation annotation) {
        return injector.getProvider(Key.of(type, annotation));
    }

    interface Configurator {
        Map<Class<?>, MembersInjector<?>> getMembersInjectors();
        Object[] getPackageModules();
    }

    private static class DefaultConfigurator implements Configurator {
        @Override
        public Map<Class<?>, MembersInjector<?>> getMembersInjectors() {
            // noinspection unchecked
            return LightsaberRegistry.getMembersInjectors();
        }

        @Override
        public Object[] getPackageModules() {
            return LightsaberRegistry.getPackageModules();
        }
    }
}
