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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Lightsaber {
    private static final Key<?> INJECTOR_KEY = Key.of(Injector.class);
    private static final Configurator DEFAULT_CONFIGURATOR = new DefaultConfigurator();

    private static volatile Lightsaber instance;
    private static final Object instanceLock = new Object();

    private final Map<Class<?>, InjectorConfigurator> injectorConfigurators;
    private final Map<Class<?>, MembersInjector<?>> membersInjectors;
    private final Object[] packageModules;

    Lightsaber(final Configurator configurator) {
        injectorConfigurators = configurator.getInjectorConfigurators();
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

    public Injector createInjector(final Object... modules) {
        final LightsaberInjector injector = createChildInjectorInternal(null, modules);
        configureInjector(injector, packageModules);
        return injector;
    }

    public Injector createChildInjector(final Injector parentInjector, final Object... modules) {
        if (parentInjector == null) {
            throw new NullPointerException("Parent injector cannot be null");
        }
        return createChildInjectorInternal(parentInjector, modules);
    }

    private LightsaberInjector createChildInjectorInternal(final Injector parentInjector,
            final Object... modules) {
        final LightsaberInjector injector = new LightsaberInjector(this, parentInjector);
        configureInjector(injector, modules);
        checkProvidersNotOverlap(injector, parentInjector);
        return injector;
    }

    private void configureInjector(final LightsaberInjector injector, final Object[] modules) {
        if (modules != null) {
            for (final Object module : modules) {
                if (module == null) {
                    throw new NullPointerException("Trying to create injector with a null module");
                }

                if (!configureInjectorWithModule(injector, module, module.getClass())) {
                    throw new IllegalArgumentException("Module hasn't been processed with Lightsaber: " + module);
                }
            }
        }
    }

    private boolean configureInjectorWithModule(final LightsaberInjector injector, final Object module,
            final Class moduleClass) {
        if (moduleClass == Object.class) {
            return false;
        }

        final InjectorConfigurator configurator = injectorConfigurators.get(moduleClass);
        if (configurator == null) {
            return configureInjectorWithModule(injector, module, moduleClass.getSuperclass());
        }

        configurator.configureInjector(injector, module);
        configureInjectorWithModule(injector, module, moduleClass.getSuperclass());
        return true;
    }

    private static void checkProvidersNotOverlap(final LightsaberInjector injector, final Injector parentInjector) {
        if (parentInjector == null) {
            return;
        }

        final Set<Key<?>> overlappingKeys = new HashSet<Key<?>>(injector.getProviders().keySet());
        overlappingKeys.retainAll(parentInjector.getAllProviders().keySet());
        overlappingKeys.remove(INJECTOR_KEY);
        if (!overlappingKeys.isEmpty()) {
            throw new ConfigurationException(composeOverlappingKeysMessage(overlappingKeys));
        }
    }

    private static String composeOverlappingKeysMessage(final Set<Key<?>> overlappingKeys) {
        final StringBuilder builder = new StringBuilder("Injector has overlapping keys with its parent:");
        for (final Key<?> key : overlappingKeys) {
            builder.append("\n  ").append(key);
        }
        return builder.toString();
    }

    void injectMembers(final Injector injector, final Object object) {
        injectFieldsIntoObject(injector, object, object.getClass());
        injectMethodsIntoObject(injector, object, object.getClass());
    }

    private void injectFieldsIntoObject(final Injector injector, final Object object, final Class type) {
        if (type == Object.class) {
            return;
        }

        injectFieldsIntoObject(injector, object, type.getSuperclass());
        final MembersInjector membersInjector = membersInjectors.get(type);
        if (membersInjector != null) {
            // noinspection unchecked
            membersInjector.injectFields(injector, object);
        }
    }

    private void injectMethodsIntoObject(final Injector injector, final Object object, final Class type) {
        if (type == Object.class) {
            return;
        }

        injectMethodsIntoObject(injector, object, type.getSuperclass());
        final MembersInjector membersInjector = membersInjectors.get(type);
        if (membersInjector != null) {
            // noinspection unchecked
            membersInjector.injectMethods(injector, object);
        }
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
        Map<Class<?>, InjectorConfigurator> getInjectorConfigurators();
        Map<Class<?>, MembersInjector<?>> getMembersInjectors();
        Object[] getPackageModules();
    }

    private static class DefaultConfigurator implements Configurator {
        @Override
        public Map<Class<?>, InjectorConfigurator> getInjectorConfigurators() {
            // noinspection unchecked
            return LightsaberRegistry.getInjectorConfigurators();
        }

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
