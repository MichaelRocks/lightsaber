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

package io.michaelrocks.lightsaber;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.lang.annotation.Annotation;

public class Lightsaber {
  private static final Configurator DEFAULT_CONFIGURATOR = new DefaultConfigurator();

  private final Configurator configurator;

  Lightsaber() {
    this(DEFAULT_CONFIGURATOR);
  }

  Lightsaber(final Configurator configurator) {
    this.configurator = configurator;
  }

  @Nonnull
  public static Lightsaber get() {
    return Holder.INSTANCE;
  }

  @Nonnull
  public Injector createInjector(@Nonnull final Object component) {
    return createInjectorInternal(null, component);
  }

  @Nonnull
  public Injector createChildInjector(@Nonnull final Injector parentInjector, @Nonnull final Object component) {
    // noinspection ConstantConditions
    if (parentInjector == null) {
      throw new NullPointerException("Parent injector cannot be null");
    }

    if (!(parentInjector instanceof LightsaberInjector)) {
      throw new IllegalArgumentException("Cannot create a child injector for a non-Lightsaber injector");
    }

    return createInjectorInternal(parentInjector, component);
  }

  private LightsaberInjector createInjectorInternal(final Injector parentInjector, final Object component) {
    if (component == null) {
      throw new NullPointerException("Trying to create an injector with a null component");
    }

    final LightsaberInjector injector = new LightsaberInjector(this, parentInjector);
    final InjectorConfigurator configurator = (InjectorConfigurator) component;
    configurator.configureInjector(injector);
    return injector;
  }

  void injectMembers(final Injector injector, final Object object) {
    configurator.injectMembers(injector, object);
  }

  @Nonnull
  public static <T> T getInstance(@Nonnull final Injector injector, @Nonnull final Class<? extends T> type) {
    return injector.getInstance(type);
  }

  @Nonnull
  public static <T> T getInstance(@Nonnull final Injector injector, @Nonnull final Class<? extends T> type,
      @Nullable final Annotation annotation) {
    return injector.getInstance(Key.of(type, annotation));
  }

  @Nonnull
  public static <T> Provider<T> getProvider(@Nonnull final Injector injector, @Nonnull final Class<? extends T> type) {
    return injector.getProvider(type);
  }

  @Nonnull
  public static <T> Provider<T> getProvider(@Nonnull final Injector injector, @Nonnull final Class<? extends T> type,
      @Nullable final Annotation annotation) {
    return injector.getProvider(Key.of(type, annotation));
  }

  interface Configurator {
    void injectMembers(Injector injector, Object object);
  }

  private static class DefaultConfigurator implements Configurator {
    @Override
    public void injectMembers(final Injector injector, final Object object) {
      InjectionDispatcher.injectMembers(injector, object);
    }
  }

  private static final class Holder {
    static final Lightsaber INSTANCE = new Lightsaber();
  }
}
