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
import javax.inject.Provider;

/**
 * Intercepts dependency {@link Provider} resolving. Can be used to replace real providers with mocks in tests.
 */
public interface ProviderInterceptor {
  /**
   * This method is called when the {@link Injector} is requested to resolve a {@link Provider} for the given key.
   *
   * @param chain
   *     An interceptor {@link Chain} that contains provision parameters and can be used to proceed with the default resolution.
   * @param key
   *     The {@link Key} to resolve a {@link Provider} for.
   * @return A {@link Provider} that will be returned for the given key.
   */
  @Nonnull
  Provider<?> intercept(@Nonnull Chain chain, @Nonnull Key<?> key);

  interface Chain {
    /** @return The {@link Injector} used to resolve a {@link Provider}. */
    @Nonnull
    Injector injector();

    /**
     * Proceed with the default {@link Provider} resolution.
     *
     * @param key
     *     The {@link Key} to resolve a {@link Provider} for.
     * @return A {@link Provider} that will be returned for the given key.
     */
    @Nonnull
    Provider<?> proceed(@Nonnull Key<?> key);
  }
}
