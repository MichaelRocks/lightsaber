/*
 * Copyright 2016 Michael Rozumyanskiy
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

import io.michaelrocks.lightsaber.internal.InjectingProvider;

class SingletonProvider<T> implements InjectingProvider<T> {
  private final InjectingProvider<T> provider;
  private volatile T instance;
  private final Object instanceLock = new Object();

  public SingletonProvider(final InjectingProvider<T> provider) {
    this.provider = provider;
  }

  @Override
  public T get() {
    if (instance == null) {
      synchronized (instanceLock) {
        if (instance == null) {
          instance = provider.get();
        }
      }
    }
    return instance;
  }

  @Override
  public T getWithInjector(final Injector injector) {
    if (instance == null) {
      synchronized (instanceLock) {
        if (instance == null) {
          instance = provider.getWithInjector(injector);
        }
      }
    }
    return instance;
  }
}
