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

package io.michaelrocks.lightsaber.internal;

import java.lang.reflect.Type;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import io.michaelrocks.lightsaber.DependencyResolver;
import io.michaelrocks.lightsaber.DependencyResolverInterceptor;
import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.Key;

public class LightsaberInjector implements Injector {
  private final List<DependencyResolverInterceptor> generalDependencyResolverInterceptors;

  private final ConfigurableDependencyResolver configurableGeneralDependencyResolver;
  private final DependencyResolver generalDependencyResolver;

  public LightsaberInjector(@Nonnull final Object component, final LightsaberInjector parent,
      final List<DependencyResolverInterceptor> generalDependencyResolverInterceptors) {
    this.generalDependencyResolverInterceptors = generalDependencyResolverInterceptors;

    this.configurableGeneralDependencyResolver =
        new ConfigurableDependencyResolver(parent == null ? null : parent.getConfigurableGeneralDependencyResolver());
    this.generalDependencyResolver = createWrappedDependencyResolver(configurableGeneralDependencyResolver, generalDependencyResolverInterceptors);

    registerProvider(Injector.class, new Provider<Injector>() {
      @Override
      public Injector get() {
        return LightsaberInjector.this;
      }
    });

    final InjectorConfigurator configurator = (InjectorConfigurator) component;
    configurator.configureInjector(this);
  }

  @Nonnull
  @Override
  public DependencyResolver getGeneralDependencyResolver() {
    return generalDependencyResolver;
  }

  @Nonnull
  @Override
  public Injector createChildInjector(@Nonnull final Object component) {
    // noinspection ConstantConditions
    if (component == null) {
      throw new NullPointerException("Trying to create an injector with a null component");
    }

    return new LightsaberInjector(component, this, generalDependencyResolverInterceptors);
  }

  @Override
  public void injectMembers(@Nonnull final Object target) {
    if (target instanceof MembersInjector) {
      final MembersInjector membersInjector = (MembersInjector) target;
      membersInjector.injectFields(this);
      membersInjector.injectMethods(this);
    }
  }

  @SuppressWarnings("depreaction")
  @Override
  @Nonnull
  public <T> T getInstance(@Nonnull final Class<T> type) {
    return generalDependencyResolver.getInstance(type);
  }

  @SuppressWarnings("depreaction")
  @Override
  @Nonnull
  public <T> T getInstance(@Nonnull final Type type) {
    return generalDependencyResolver.getInstance(type);
  }

  @SuppressWarnings("depreaction")
  @Override
  @Nonnull
  public <T> T getInstance(@Nonnull final Key<T> key) {
    return generalDependencyResolver.getInstance(key);
  }

  @SuppressWarnings("depreaction")
  @Override
  @Nonnull
  public <T> Provider<? extends T> getProvider(@Nonnull final Class<T> type) {
    return generalDependencyResolver.getProvider(type);
  }

  @SuppressWarnings("depreaction")
  @Override
  @Nonnull
  public <T> Provider<? extends T> getProvider(@Nonnull final Type type) {
    return generalDependencyResolver.getProvider(type);
  }

  @SuppressWarnings("depreaction")
  @Override
  @Nonnull
  public <T> Provider<? extends T> getProvider(@Nonnull final Key<T> key) {
    return generalDependencyResolver.getProvider(key);
  }

  public ConfigurableDependencyResolver getConfigurableGeneralDependencyResolver() {
    return configurableGeneralDependencyResolver;
  }

  public <T> void registerProvider(final Class<T> type, final Provider<? extends T> provider) {
    configurableGeneralDependencyResolver.registerProvider(type, provider);
  }

  public <T> void registerProvider(final Type type, final Provider<? extends T> provider) {
    configurableGeneralDependencyResolver.registerProvider(type, provider);
  }

  public <T> void registerProvider(final Key<T> key, final Provider<? extends T> provider) {
    configurableGeneralDependencyResolver.registerProvider(key, provider);
  }

  private DependencyResolver createWrappedDependencyResolver(final DependencyResolver resolver,
      final List<DependencyResolverInterceptor> interceptors) {
    if (interceptors == null) {
      return resolver;
    }

    DependencyResolver wrappedResolver = resolver;
    for (final DependencyResolverInterceptor interceptor : interceptors) {
      wrappedResolver = interceptor.intercept(this, wrappedResolver);
      // noinspection ConstantConditions
      if (wrappedResolver == null) {
        throw new NullPointerException("DependencyResolverInterceptor returned null: " + interceptor);
      }
    }

    return wrappedResolver;
  }
}
