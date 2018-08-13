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

package io.michaelrocks.lightsaber.access;

import javax.inject.Singleton;

import io.michaelrocks.lightsaber.Module;
import io.michaelrocks.lightsaber.Provides;

@Module
class AccessModule {
  @Provides
  private InternalDependency provideInternalDependency(final InternalDependencyImpl impl) {
    return impl;
  }

  @Provides
  @InternalQualifier
  private InternalDependency provideQualifiedInternalDependency(final InternalDependencyImpl impl) {
    return impl;
  }

  @Provides
  @Singleton
  @SingletonQualifier
  private InternalDependency provideSingletonInternalDependency(final InternalDependencyImpl impl) {
    return impl;
  }

  @Provides
  private InternalGenericDependency<InternalDependency> provideInternalGenericDependency(
      final InternalGenericDependencyImpl impl) {
    return impl;
  }

  @Provides
  @InternalQualifier
  private InternalGenericDependency<InternalDependency> provideQualifiedInternalGenericDependency(
      final InternalGenericDependencyImpl impl) {
    return impl;
  }

  @Provides
  @Singleton
  @SingletonQualifier
  private InternalGenericDependency<InternalDependency> provideSingletonInternalGenericDependency(
      final InternalGenericDependencyImpl impl) {
    return impl;
  }
}
