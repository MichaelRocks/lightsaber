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

import java.lang.reflect.Type;

import javax.annotation.Nonnull;
import javax.inject.Provider;

public interface Injector extends DependencyResolver {
  @Nonnull
  DependencyResolver getGeneralDependencyResolver();

  @Nonnull
  Injector createChildInjector(@Nonnull final Object component);

  void injectMembers(@Nonnull Object target);

  /**
   * @deprecated Get {@link DependencyResolver} by calling {@link #getGeneralDependencyResolver()} and invoke
   * {@link DependencyResolver#getInstance(Class)} on it.
   */
  @Nonnull
  @Override
  <T> T getInstance(@Nonnull Class<T> type);

  /**
   * @deprecated Get {@link DependencyResolver} by calling {@link #getGeneralDependencyResolver()} and invoke
   * {@link DependencyResolver#getInstance(Type)} on it.
   */
  @Nonnull
  @Override
  <T> T getInstance(@Nonnull Type type);

  /**
   * @deprecated Get {@link DependencyResolver} by calling {@link #getGeneralDependencyResolver()} and invoke
   * {@link DependencyResolver#getInstance(Key)} on it.
   */
  @Nonnull
  @Override
  <T> T getInstance(@Nonnull Key<T> key);

  /**
   * @deprecated Get {@link DependencyResolver} by calling {@link #getGeneralDependencyResolver()} and invoke
   * {@link DependencyResolver#getProvider(Class)} on it.
   */
  @Nonnull
  @Override
  <T> Provider<? extends T> getProvider(@Nonnull Class<T> type);

  /**
   * @deprecated Get {@link DependencyResolver} by calling {@link #getGeneralDependencyResolver()} and invoke
   * {@link DependencyResolver#getProvider(Type)} on it.
   */
  @Nonnull
  @Override
  <T> Provider<? extends T> getProvider(@Nonnull Type type);

  /**
   * @deprecated Get {@link DependencyResolver} by calling {@link #getGeneralDependencyResolver()} and invoke
   * {@link DependencyResolver#getProvider(Key)} on it.
   */
  @Nonnull
  @Override
  <T> Provider<? extends T> getProvider(@Nonnull Key<T> key);
}
