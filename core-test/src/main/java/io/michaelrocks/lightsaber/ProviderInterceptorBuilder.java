/*
 * Copyright 2020 Michael Rozumyanskiy
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

public class ProviderInterceptorBuilder {
  private final Map<Key<?>, Provider<?>> providers = new HashMap<Key<?>, Provider<?>>();

  public <T> ProviderInterceptorBuilder addProviderForClass(@Nonnull final Class<T> type, @Nonnull final Provider<? extends T> provider) {
    return addProviderInternal(Key.of(type), provider);
  }

  public <T> ProviderInterceptorBuilder addProviderForClass(@Nonnull final Class<T> type, @Nullable final Annotation annotation,
      @Nonnull final Provider<? extends T> provider) {
    return addProviderInternal(Key.of(type, annotation), provider);
  }

  public <T> ProviderInterceptorBuilder addProviderForClass(@Nonnull final Class<T> type, @Nullable final Class<? extends Annotation> annotationClass,
      @Nonnull final Provider<? extends T> provider) {
    return addProviderInternal(Key.of(type, createAnnotation(annotationClass)), provider);
  }

  public ProviderInterceptorBuilder addProviderForType(@Nonnull final Type type, @Nullable final Annotation annotation,
      @Nonnull final Provider<?> provider) {
    return addProviderInternal(Key.of(type, annotation), provider);
  }

  public ProviderInterceptorBuilder addProviderForType(@Nonnull final Type type, @Nullable final Class<? extends Annotation> annotationClass,
      @Nonnull final Provider<?> provider) {
    return addProviderInternal(Key.of(type, createAnnotation(annotationClass)), provider);
  }

  public ProviderInterceptorBuilder addProviderForType(@Nonnull final Type type, @Nonnull final Provider<?> provider) {
    return addProviderInternal(Key.of(type), provider);
  }

  public <T> ProviderInterceptorBuilder addProviderForKey(@Nonnull final Key<T> key, @Nonnull final Provider<? extends T> provider) {
    return addProviderInternal(key, provider);
  }

  @Nonnull
  public ProviderInterceptor build() {
    return new ImmutableProviderInterceptor(this);
  }

  @Nullable
  private Annotation createAnnotation(@Nullable final Class<? extends Annotation> annotationClass) {
    return annotationClass != null ? new AnnotationBuilder<Annotation>(annotationClass).build() : null;
  }

  private ProviderInterceptorBuilder addProviderInternal(@Nonnull final Key<?> key, @Nonnull final Provider<?> provider) {
    final Provider<?> oldProvider = providers.put(key, provider);
    if (oldProvider != null) {
      throw new IllegalArgumentException("Provider for key " + key + " already exists");
    }

    return this;
  }

  private static class ImmutableProviderInterceptor implements ProviderInterceptor {
    private final Map<Key<?>, Provider<?>> providers;

    ImmutableProviderInterceptor(@Nonnull final ProviderInterceptorBuilder builder) {
      providers = new HashMap<Key<?>, Provider<?>>(builder.providers);
    }

    @Nonnull
    @Override
    public Provider<?> intercept(@Nonnull final Chain chain, @Nonnull final Key<?> key) {
      final Provider<?> provider = providers.get(key);
      if (provider != null) {
        return provider;
      }

      return chain.proceed(key);
    }
  }
}
