/*
 * Copyright 2016 Michael Rozumyanskiy
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
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Lightsaber {
  private static final Key<?> INJECTOR_KEY = Key.of(Injector.class);
  private static final Configurator DEFAULT_CONFIGURATOR = new DefaultConfigurator();

  private final List<InjectorConfigurator> packageInjectorConfigurators;
  private final Map<Class<?>, InjectorConfigurator> injectorConfigurators;
  private final Map<Class<?>, MembersInjector<?>> membersInjectors;

  Lightsaber() {
    this(DEFAULT_CONFIGURATOR);
  }

  Lightsaber(final Configurator configurator) {
    packageInjectorConfigurators = configurator.getPackageInjectorConfigurators();
    injectorConfigurators = configurator.getInjectorConfigurators();
    membersInjectors = configurator.getMembersInjectors();
  }

  public static Lightsaber get() {
    return Holder.INSTANCE;
  }

  public Injector createInjector(final Object component) {
    final LightsaberInjector injector = createChildInjectorInternal(null, component);
    for (final InjectorConfigurator injectorConfigurator : packageInjectorConfigurators) {
      injectorConfigurator.configureInjector(injector, null);
    }
    return injector;
  }

  public Injector createChildInjector(final Injector parentInjector, final Object component) {
    if (parentInjector == null) {
      throw new NullPointerException("Parent injector cannot be null");
    }
    return createChildInjectorInternal(parentInjector, component);
  }

  private LightsaberInjector createChildInjectorInternal(final Injector parentInjector, final Object component) {
    final LightsaberInjector injector = new LightsaberInjector(this, parentInjector);
    configureInjector(injector, component);
    checkProvidersNotOverlap(injector, parentInjector);
    return injector;
  }

  private void configureInjector(final LightsaberInjector injector, final Object component) {
    if (component == null) {
      throw new NullPointerException("Trying to create an injector with a null component");
    }
    if (!configureInjectorWithComponent(injector, component, component.getClass())) {
      throw new IllegalArgumentException("Component hasn't been processed with Lightsaber: " + component);
    }
  }

  private boolean configureInjectorWithComponent(final LightsaberInjector injector, final Object component,
      final Class componentClass) {
    final InjectorConfigurator configurator = injectorConfigurators.get(componentClass);
    if (configurator != null) {
      configurator.configureInjector(injector, component);
      return true;
    }
    return false;
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
    List<InjectorConfigurator> getPackageInjectorConfigurators();

    Map<Class<?>, InjectorConfigurator> getInjectorConfigurators();

    Map<Class<?>, MembersInjector<?>> getMembersInjectors();
  }

  private static class DefaultConfigurator implements Configurator {
    @Override
    public List<InjectorConfigurator> getPackageInjectorConfigurators() {
      // noinspection unchecked
      return LightsaberRegistry.getPackageInjectorConfigurators();
    }

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
  }

  private static final class Holder {
    static final Lightsaber INSTANCE = new Lightsaber();
  }
}
