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

package io.michaelrocks.lightsaber.internal;

import io.michaelrocks.lightsaber.Injector;

public abstract class AbstractInjectingProvider<T> implements InjectingProvider<T> {
  private static final ThreadLocal<Injector> currentInjector = new ThreadLocal<Injector>();

  private final Injector injector;

  protected AbstractInjectingProvider(final Injector injector) {
    this.injector = injector;
  }

  @Override
  public final T get() {
    boolean ownsCurrentInjector = false;
    Injector contextInjector = currentInjector.get();
    if (contextInjector == null) {
      contextInjector = injector;
      currentInjector.set(contextInjector);
      ownsCurrentInjector = true;
    }

    try {
      return getWithInjector(contextInjector);
    } finally {
      if (ownsCurrentInjector) {
        currentInjector.remove();
      }
    }
  }
}
