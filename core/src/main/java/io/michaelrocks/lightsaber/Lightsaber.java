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

package io.michaelrocks.lightsaber;

import java.lang.annotation.Annotation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import io.michaelrocks.lightsaber.internal.InjectorConfigurator;
import io.michaelrocks.lightsaber.internal.LightsaberInjector;

public class Lightsaber {
  Lightsaber(final Builder builder) {
  }

  @Nonnull
  public static Lightsaber get() {
    return Holder.INSTANCE;
  }

  @Nonnull
  public Builder newBuilder() {
    return new Builder(this);
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

    final LightsaberInjector injector = new LightsaberInjector(parentInjector);
    final InjectorConfigurator configurator = (InjectorConfigurator) component;
    configurator.configureInjector(injector);
    return injector;
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

  public static class Builder {
    public Builder() {
    }

    Builder(final Lightsaber lightsaber) {
    }

    public Lightsaber build() {
      return new Lightsaber(this);
    }
  }

  private static final class Holder {
    static final Lightsaber INSTANCE = new Lightsaber.Builder().build();
  }
}
