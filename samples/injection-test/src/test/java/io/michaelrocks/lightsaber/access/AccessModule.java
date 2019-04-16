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

package io.michaelrocks.lightsaber.access;

import javax.inject.Singleton;

import io.michaelrocks.lightsaber.Module;
import io.michaelrocks.lightsaber.Provide;

@Module
class AccessModule {
  @Provide
  private InternalDependency provideInternalDependency(final InternalDependencyImpl impl) {
    return impl;
  }

  @Provide
  @InternalQualifier
  private InternalDependency provideQualifiedInternalDependency(final InternalDependencyImpl impl) {
    return impl;
  }

  @Provide
  @Singleton
  @SingletonQualifier
  private InternalDependency provideSingletonInternalDependency(final InternalDependencyImpl impl) {
    return impl;
  }

  @Provide
  private InternalDependency[] provideInternalDependencyArray(final InternalDependencyImpl impl) {
    return new InternalDependency[] { impl };
  }

  @Provide
  @InternalQualifier
  private InternalDependency[] provideQualifiedInternalDependencyArray(final InternalDependencyImpl impl) {
    return new InternalDependencyImpl[] { impl };
  }

  @Provide
  @Singleton
  @SingletonQualifier
  private InternalDependency[] provideSingletonInternalDependencyArray(final InternalDependencyImpl impl) {
    return new InternalDependencyImpl[] { impl };
  }


  @Provide
  private InternalGenericDependency<InternalDependency> provideInternalGenericDependency(
      final InternalGenericDependencyImpl impl) {
    return impl;
  }

  @Provide
  @InternalQualifier
  private InternalGenericDependency<InternalDependency> provideQualifiedInternalGenericDependency(
      final InternalGenericDependencyImpl impl) {
    return impl;
  }

  @Provide
  @Singleton
  @SingletonQualifier
  private InternalGenericDependency<InternalDependency> provideSingletonInternalGenericDependency(
      final InternalGenericDependencyImpl impl) {
    return impl;
  }

  @Provide
  private InternalGenericDependency<InternalDependency>[] provideInternalGenericDependencyArray(
      final InternalGenericDependencyImpl impl) {
    return new InternalGenericDependencyImpl[] { impl };
  }

  @Provide
  @InternalQualifier
  private InternalGenericDependency<InternalDependency>[] provideQualifiedInternalGenericDependencyArray(
      final InternalGenericDependencyImpl impl) {
    return new InternalGenericDependencyImpl[] { impl };
  }

  @Provide
  @Singleton
  @SingletonQualifier
  private InternalGenericDependency<InternalDependency>[] provideSingletonInternalGenericDependencyArray(
      final InternalGenericDependencyImpl impl) {
    return new InternalGenericDependencyImpl[] { impl };
  }
}
