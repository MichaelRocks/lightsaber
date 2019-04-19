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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import io.michaelrocks.lightsaber.internal.LightsaberInjector;

public class Lightsaber {
  private final List<DependencyResolverInterceptor> generalDependencyResolverInterceptors;

  Lightsaber(final Builder builder) {
    generalDependencyResolverInterceptors = builder.generalDependencyResolverInterceptors == null
        ? null : new ArrayList<DependencyResolverInterceptor>(builder.generalDependencyResolverInterceptors);
  }

  /**
   * @return A {@link Lightsaber} instance.
   * @deprecated Use {@link Lightsaber.Builder} instead.
   */
  @Nonnull
  @Deprecated
  public static Lightsaber get() {
    return Holder.INSTANCE;
  }

  @Nonnull
  public Builder newBuilder() {
    return new Builder(this);
  }

  @Nonnull
  public Injector createInjector(@Nonnull final Object component) {
    return new LightsaberInjector(component, null, generalDependencyResolverInterceptors);
  }

  /** @deprecated Use {@link Injector#createChildInjector(Object)} instead. */
  @Nonnull
  public Injector createChildInjector(@Nonnull final Injector parentInjector, @Nonnull final Object component) {
    // noinspection ConstantConditions
    if (parentInjector == null) {
      throw new NullPointerException("Parent injector cannot be null");
    }

    if (!(parentInjector instanceof LightsaberInjector)) {
      throw new IllegalArgumentException("Cannot create a child injector for a non-Lightsaber injector");
    }

    return new LightsaberInjector(component, (LightsaberInjector) parentInjector, generalDependencyResolverInterceptors);
  }

  @Nonnull
  public static <T> T getInstance(@Nonnull final Injector injector, @Nonnull final Class<? extends T> type) {
    return injector.getGeneralDependencyResolver().getInstance(type);
  }

  @Nonnull
  public static <T> T getInstance(@Nonnull final Injector injector, @Nonnull final Class<? extends T> type,
      @Nullable final Annotation annotation) {
    return injector.getGeneralDependencyResolver().getInstance(Key.<T>of(type, annotation));
  }

  @Nonnull
  public static <T> Provider<? extends T> getProvider(@Nonnull final Injector injector, @Nonnull final Class<? extends T> type) {
    return injector.getGeneralDependencyResolver().getProvider(type);
  }

  @Nonnull
  public static <T> Provider<? extends T> getProvider(@Nonnull final Injector injector, @Nonnull final Class<? extends T> type,
      @Nullable final Annotation annotation) {
    return injector.getGeneralDependencyResolver().getProvider(Key.of(type, annotation));
  }

  public static class Builder {
    private List<DependencyResolverInterceptor> generalDependencyResolverInterceptors;

    public Builder() {
    }

    Builder(@Nonnull final Lightsaber lightsaber) {
      generalDependencyResolverInterceptors = lightsaber.generalDependencyResolverInterceptors == null
          ? null : new ArrayList<DependencyResolverInterceptor>(lightsaber.generalDependencyResolverInterceptors);
    }

    /**
     * Adds a {@link DependencyResolverInterceptor} to the interceptor chain. This interceptor may be invoked when the {@link Injector} creates a
     * {@link DependencyResolver} for general dependencies. Added interceptors will be invoked in the reverse order.
     *
     * @param interceptor
     *     The {@link DependencyResolverInterceptor} to add to the interceptor chain.
     * @return The current {@link Builder} instance.
     */
    @Nonnull
    public Builder addGeneralDependencyResolverInterceptor(@Nonnull final DependencyResolverInterceptor interceptor) {
      // noinspection ConstantConditions
      if (interceptor == null) {
        throw new NullPointerException("Interceptor is null");
      }

      if (generalDependencyResolverInterceptors == null) {
        generalDependencyResolverInterceptors = new ArrayList<DependencyResolverInterceptor>();
      }

      generalDependencyResolverInterceptors.add(interceptor);
      return this;
    }

    /**
     * Adds a {@link ProviderInterceptor} to the interceptor chain. Added interceptors will be invoked in the reverse order.
     * <p>
     * <strong>WARNING!</strong> Provider interception affects performance negatively. If a single interceptor is added each dependency resolution
     * produces at least two additional allocations even if it's not affected by the interceptor.
     * </p>
     *
     * @param interceptor
     *     The {@link ProviderInterceptor} to add to the interceptor chain.
     * @return The current {@link Builder} instance.
     * @deprecated Use {@link DependencyResolverInterceptor} instead.
     */
    @Nonnull
    public Builder addProviderInterceptor(@Nonnull final ProviderInterceptor interceptor) {
      // noinspection ConstantConditions
      if (interceptor == null) {
        throw new NullPointerException("Interceptor is null");
      }

      addGeneralDependencyResolverInterceptor(
          new DependencyResolverInterceptor() {
            @Nonnull
            @Override
            public DependencyResolver intercept(@Nonnull final Injector injector, @Nonnull final DependencyResolver resolver) {
              return new ProviderInterceptorDependencyResolver(injector, resolver, interceptor);
            }
          }
      );
      return this;
    }

    @Nonnull
    public Lightsaber build() {
      return new Lightsaber(this);
    }
  }

  private static final class Holder {
    static final Lightsaber INSTANCE = new Lightsaber.Builder().build();
  }

  @SuppressWarnings("deprecation")
  private static class ProviderInterceptorDependencyResolver implements DependencyResolver, ProviderInterceptor.Chain {
    private final Injector injector;
    private final DependencyResolver resolver;
    private final ProviderInterceptor interceptor;

    ProviderInterceptorDependencyResolver(final Injector injector, final DependencyResolver resolver, final ProviderInterceptor interceptor) {
      this.injector = injector;
      this.resolver = resolver;
      this.interceptor = interceptor;
    }

    @Nonnull
    @Override
    public <T> T getInstance(@Nonnull final Class<T> type) {
      return getProviderInternal(Key.of(type)).get();
    }

    @Nonnull
    @Override
    public <T> T getInstance(@Nonnull final Type type) {
      return getProviderInternal(Key.<T>of(type)).get();
    }

    @Nonnull
    @Override
    public <T> T getInstance(@Nonnull final Key<T> key) {
      return getProviderInternal(key).get();
    }

    @Nonnull
    @Override
    public <T> Provider<? extends T> getProvider(@Nonnull final Class<T> type) {
      return getProviderInternal(Key.of(type));
    }

    @Nonnull
    @Override
    public <T> Provider<? extends T> getProvider(@Nonnull final Type type) {
      return getProviderInternal(Key.<T>of(type));
    }

    @Nonnull
    @Override
    public <T> Provider<? extends T> getProvider(@Nonnull final Key<T> key) {
      return getProviderInternal(key);
    }

    @Nonnull
    @Override
    public Injector injector() {
      return injector;
    }

    @Nonnull
    @Override
    public Provider<?> proceed(@Nonnull final Key<?> key) {
      return resolver.getProvider(key);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    private <T> Provider<T> getProviderInternal(@Nonnull final Key<? extends T> key) {
      return (Provider<T>) interceptor.intercept(this, key);
    }
  }
}
