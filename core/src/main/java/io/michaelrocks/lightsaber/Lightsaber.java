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

import io.michaelrocks.lightsaber.internal.InternalModule;
import io.michaelrocks.lightsaber.internal.LightsaberInjector;

import javax.inject.Provider;
import java.lang.annotation.Annotation;

public class Lightsaber {
    public static Injector createInjector(final Module... modules) {
        return createChildInjectorInternal(null, modules);
    }

    public static Injector createChildInjector(final Injector parentInjector, final Module... modules) {
        if (parentInjector == null) {
            throw new NullPointerException("Parent injector cannot be null");
        }
        return createChildInjectorInternal(parentInjector, modules);
    }

    private static Injector createChildInjectorInternal(final Injector parentInjector, final Module... modules) {
        final LightsaberInjector injector = new LightsaberInjector(parentInjector);
        if (modules != null) {
            for (final Module module : modules) {
                if (module == null) {
                    throw new NullPointerException("Trying to create injector with a null module");
                }

                if (!(module instanceof InternalModule)) {
                    throw new ConfigurationException("Module " + module + " hasn't been processed");
                }

                ((InternalModule) module).configureInjector(injector);
            }
        }
        return injector;
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
}
