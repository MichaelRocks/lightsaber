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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;

import io.michaelrocks.lightsaber.internal.InstanceProvider;

public class DependencyResolverInterceptorBuilder {
  private final Map<Object, Provider<?>> providers = new HashMap<Object, Provider<?>>();

  public <T> DependencyResolverInterceptorBuilder addInstanceForClass(@Nonnull final Class<T> type, @Nonnull final T instance) {
    return addInstanceInternal(type, instance);
  }

  public <T> DependencyResolverInterceptorBuilder addInstanceForClass(@Nonnull final Class<T> type, @Nullable final Annotation annotation,
      @Nonnull final T instance) {
    return addInstanceInternal(Key.of(type, annotation), instance);
  }

  public <T> DependencyResolverInterceptorBuilder addInstanceForClass(@Nonnull final Class<T> type,
      @Nullable final Class<? extends Annotation> annotationClass, @Nonnull final T instance) {
    return addInstanceInternal(Key.of(type, createAnnotation(annotationClass)), instance);
  }

  public <T> DependencyResolverInterceptorBuilder addProviderForClass(@Nonnull final Class<T> type, @Nonnull final Provider<? extends T> provider) {
    return addProviderInternal(type, provider);
  }

  public <T> DependencyResolverInterceptorBuilder addProviderForClass(@Nonnull final Class<T> type, @Nullable final Annotation annotation,
      @Nonnull final Provider<? extends T> provider) {
    return addProviderInternal(Key.of(type, annotation), provider);
  }

  public <T> DependencyResolverInterceptorBuilder addProviderForClass(@Nonnull final Class<T> type,
      @Nullable final Class<? extends Annotation> annotationClass, @Nonnull final Provider<? extends T> provider) {
    return addProviderInternal(Key.of(type, createAnnotation(annotationClass)), provider);
  }

  public DependencyResolverInterceptorBuilder addInstanceForType(@Nonnull final Type type, @Nonnull final Object instance) {
    return addInstanceInternal(type, instance);
  }

  public DependencyResolverInterceptorBuilder addInstanceForType(@Nonnull final Type type, @Nullable final Annotation annotation,
      @Nonnull final Object instance) {
    return addInstanceInternal(Key.of(type, annotation), instance);
  }

  public DependencyResolverInterceptorBuilder addInstanceForType(@Nonnull final Type type,
      @Nullable final Class<? extends Annotation> annotationClass, @Nonnull final Object instance) {
    return addInstanceInternal(Key.of(type, createAnnotation(annotationClass)), instance);
  }

  public DependencyResolverInterceptorBuilder addProviderForType(@Nonnull final Type type, @Nonnull final Provider<?> provider) {
    return addProviderInternal(type, provider);
  }

  public DependencyResolverInterceptorBuilder addProviderForType(@Nonnull final Type type, @Nullable final Annotation annotation,
      @Nonnull final Provider<?> provider) {
    return addProviderInternal(Key.of(type, annotation), provider);
  }

  public DependencyResolverInterceptorBuilder addProviderForType(@Nonnull final Type type,
      @Nullable final Class<? extends Annotation> annotationClass,
      @Nonnull final Provider<?> provider) {
    return addProviderInternal(Key.of(type, createAnnotation(annotationClass)), provider);
  }

  public <T> DependencyResolverInterceptorBuilder addInstanceForKey(@Nonnull final Key<T> key, @Nonnull final T instance) {
    if (key.getQualifier() == null) {
      return addInstanceInternal(key.getType(), instance);
    } else {
      return addInstanceInternal(key, instance);
    }
  }

  public <T> DependencyResolverInterceptorBuilder addProviderForKey(@Nonnull final Key<T> key, @Nonnull final Provider<? extends T> provider) {
    if (key.getQualifier() == null) {
      return addProviderInternal(key.getType(), provider);
    } else {
      return addProviderInternal(key, provider);
    }
  }

  @Nonnull
  public DependencyResolverInterceptor build() {
    return new ImmutableDependencyResolverInterceptor(this);
  }

  @Nullable
  private Annotation createAnnotation(@Nullable final Class<? extends Annotation> annotationClass) {
    return annotationClass != null ? new AnnotationBuilder<Annotation>(annotationClass).build() : null;
  }

  private DependencyResolverInterceptorBuilder addInstanceInternal(@Nonnull final Object key, @Nonnull final Object instance) {
    return addProviderInternal(key, new InstanceProvider<Object>(instance));
  }

  private DependencyResolverInterceptorBuilder addProviderInternal(@Nonnull final Object key, @Nonnull final Provider<?> provider) {
    final Provider<?> oldProvider = providers.put(key, provider);
    if (oldProvider != null) {
      throw new IllegalArgumentException("Provider for key " + key + " already exists");
    }

    return this;
  }

  private static class ImmutableDependencyResolverInterceptor implements DependencyResolverInterceptor {
    private final Map<?, Provider<?>> providers;

    ImmutableDependencyResolverInterceptor(@Nonnull final DependencyResolverInterceptorBuilder builder) {
      providers = new HashMap<Object, Provider<?>>(builder.providers);
    }

    @SuppressWarnings("unchecked")
    @Nonnull
    @Override
    public DependencyResolver intercept(@Nonnull final Injector injector, @Nonnull final DependencyResolver resolver) {
      return new DependencyResolver() {
        @Nonnull
        @Override
        public <T> T getInstance(@Nonnull final Class<T> type) {
          final Provider<T> provider = (Provider<T>) providers.get(type);
          return provider != null ? provider.get() : resolver.getInstance(type);
        }

        @Nonnull
        @Override
        public <T> T getInstance(@Nonnull final Type type) {
          final Provider<T> provider = (Provider<T>) providers.get(type);
          return provider != null ? provider.get() : (T) resolver.getInstance(type);
        }

        @Nonnull
        @Override
        public <T> T getInstance(@Nonnull final Key<T> key) {
          final Object normalizedKey = key.getQualifier() == null ? key.getType() : key;
          final Provider<T> provider = (Provider<T>) providers.get(normalizedKey);
          return provider != null ? provider.get() : resolver.getInstance(key);
        }

        @Nonnull
        @Override
        public <T> Provider<? extends T> getProvider(@Nonnull final Class<T> type) {
          final Provider<T> provider = (Provider<T>) providers.get(type);
          return provider != null ? provider : resolver.getProvider(type);
        }

        @Nonnull
        @Override
        public <T> Provider<? extends T> getProvider(@Nonnull final Type type) {
          final Provider<T> provider = (Provider<T>) providers.get(type);
          return provider != null ? provider : (Provider<T>) resolver.getProvider(type);
        }

        @Nonnull
        @Override
        public <T> Provider<? extends T> getProvider(@Nonnull final Key<T> key) {
          final Object normalizedKey = key.getQualifier() == null ? key.getType() : key;
          final Provider<T> provider = (Provider<T>) providers.get(normalizedKey);
          return provider != null ? provider : (Provider<T>) resolver.getProvider(key);
        }
      };
    }
  }
}
