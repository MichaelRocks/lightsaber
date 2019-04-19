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

import javax.annotation.Nonnull;

/**
 * Intercepts {@link DependencyResolver} creation. Can be used to replace real dependencies with mocks in tests.
 */
public interface DependencyResolverInterceptor {
  /**
   * This method is called when the {@link Injector} creates a {@link DependencyResolver}.
   *
   * @param injector
   *     The {@link Injector} that creates the {@link DependencyResolver}.
   * @param resolver
   *     The {@link DependencyResolver} already created for the given injector.
   * @return A {@link DependencyResolver} that will be used by the injector.
   */
  @Nonnull
  DependencyResolver intercept(@Nonnull Injector injector, @Nonnull DependencyResolver resolver);
}
