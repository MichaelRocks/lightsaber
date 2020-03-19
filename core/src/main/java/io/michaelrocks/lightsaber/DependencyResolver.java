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

public interface DependencyResolver {
  @Nonnull
  <T> T getInstance(@Nonnull Class<? extends T> type);

  @Nonnull
  <T> T getInstance(@Nonnull Type type);

  @Nonnull
  <T> T getInstance(@Nonnull Key<? extends T> key);

  @Nonnull
  <T> Provider<T> getProvider(@Nonnull Class<? extends T> type);

  @Nonnull
  <T> Provider<T> getProvider(@Nonnull Type type);

  @Nonnull
  <T> Provider<T> getProvider(@Nonnull Key<? extends T> key);
}
