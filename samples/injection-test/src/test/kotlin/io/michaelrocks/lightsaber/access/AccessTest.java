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

import org.junit.Test;

import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.Key;
import io.michaelrocks.lightsaber.Lightsaber;


import static org.junit.Assert.assertSame;

class AccessTest {
  @Test
  public void testInjectionAccess() {
    final Injector injector = Lightsaber.get().createInjector(new AccessComponent());
    final InternalDependency target = injector.getInstance(Key.of(InternalDependency.class));
    target.action();
  }

  @Test
  public void testInjectionAccessWithQualifier() {
    final Injector injector = Lightsaber.get().createInjector(new AccessComponent());
    final InternalQualifier qualifier = AnnotationHolder.class.getAnnotation(InternalQualifier.class);
    final InternalDependency target = injector.getInstance(Key.of(InternalDependency.class, qualifier));
    target.action();
  }

  @Test
  public void testInjectionAccessWithSingletonScope() {
    final Injector injector = Lightsaber.get().createInjector(new AccessComponent());
    final SingletonQualifier qualifier = AnnotationHolder.class.getAnnotation(SingletonQualifier.class);
    final InternalDependency target1 = injector.getInstance(Key.of(InternalDependency.class, qualifier));
    final InternalDependency target2 = injector.getInstance(Key.of(InternalDependency.class, qualifier));
    target1.action();
    target2.action();
    assertSame(target1, target2);
  }

  @InternalQualifier
  @SingletonQualifier
  private class AnnotationHolder {
  }
}
