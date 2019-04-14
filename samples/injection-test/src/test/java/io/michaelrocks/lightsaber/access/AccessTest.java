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

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.Key;
import io.michaelrocks.lightsaber.Lightsaber;


import static org.junit.Assert.assertSame;

public class AccessTest {
  @Test
  public void testInjectionAccess() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final InternalDependency target = injector.getInstance(Key.of(InternalDependency.class));
    target.action();
  }

  @Test
  public void testInjectionAccessWithQualifier() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final InternalQualifier qualifier = AnnotationHolder.class.getAnnotation(InternalQualifier.class);
    final InternalDependency target = injector.getInstance(Key.of(InternalDependency.class, qualifier));
    target.action();
  }

  @Test
  public void testInjectionAccessWithSingletonScope() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final SingletonQualifier qualifier = AnnotationHolder.class.getAnnotation(SingletonQualifier.class);
    final InternalDependency target1 = injector.getInstance(Key.of(InternalDependency.class, qualifier));
    final InternalDependency target2 = injector.getInstance(Key.of(InternalDependency.class, qualifier));
    target1.action();
    target2.action();
    assertSame(target1, target2);
  }

  @Test
  public void testArrayInjectionAccess() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final InternalDependency[] target = injector.getInstance(Key.of(InternalDependency[].class));
    target[0].action();
  }

  @Test
  public void testArrayInjectionAccessWithQualifier() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final InternalQualifier qualifier = AnnotationHolder.class.getAnnotation(InternalQualifier.class);
    final InternalDependency[] target = injector.getInstance(Key.of(InternalDependency[].class, qualifier));
    target[0].action();
  }

  @Test
  public void testArrayInjectionAccessWithSingletonScope() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final SingletonQualifier qualifier = AnnotationHolder.class.getAnnotation(SingletonQualifier.class);
    final InternalDependency[] target1 = injector.getInstance(Key.of(InternalDependency[].class, qualifier));
    final InternalDependency[] target2 = injector.getInstance(Key.of(InternalDependency[].class, qualifier));
    target1[0].action();
    target2[0].action();
    assertSame(target1[0], target2[0]);
  }

  @Test
  public void testGenericInjectionAccess() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final InternalGenericDependency<InternalDependency> target =
        injector.getInstance(createInternalGenericDependencyKey(null));
    target.action();
  }

  @Test
  public void testGenericInjectionAccessWithQualifier() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final InternalQualifier qualifier = AnnotationHolder.class.getAnnotation(InternalQualifier.class);
    final InternalGenericDependency<InternalDependency> target =
        injector.getInstance(createInternalGenericDependencyKey(qualifier));
    target.action();
  }

  @Test
  public void testGenericInjectionAccessWithSingletonScope() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final SingletonQualifier qualifier = AnnotationHolder.class.getAnnotation(SingletonQualifier.class);
    final InternalGenericDependency<InternalDependency> target1 =
        injector.getInstance(createInternalGenericDependencyKey(qualifier));
    final InternalGenericDependency<InternalDependency> target2 =
        injector.getInstance(createInternalGenericDependencyKey(qualifier));
    target1.action();
    target2.action();
    assertSame(target1, target2);
  }

  @Test
  public void testGenericArrayInjectionAccess() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final InternalGenericDependency<InternalDependency>[] target =
        injector.getInstance(createInternalGenericArrayDependencyKey(null));
    target[0].action();
  }

  @Test
  public void testGenericArrayInjectionAccessWithQualifier() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final InternalQualifier qualifier = AnnotationHolder.class.getAnnotation(InternalQualifier.class);
    final InternalGenericDependency<InternalDependency>[] target =
        injector.getInstance(createInternalGenericArrayDependencyKey(qualifier));
    target[0].action();
  }

  @Test
  public void testGenericArrayInjectionAccessWithSingletonScope() {
    final Injector injector = new Lightsaber.Builder().build().createInjector(new AccessComponent());
    final SingletonQualifier qualifier = AnnotationHolder.class.getAnnotation(SingletonQualifier.class);
    final InternalGenericDependency<InternalDependency>[] target1 =
        injector.getInstance(createInternalGenericArrayDependencyKey(qualifier));
    final InternalGenericDependency<InternalDependency>[] target2 =
        injector.getInstance(createInternalGenericArrayDependencyKey(qualifier));
    target1[0].action();
    target2[0].action();
    assertSame(target1[0], target2[0]);
  }

  private static Key<InternalGenericDependency<InternalDependency>> createInternalGenericDependencyKey(
      final Annotation qualifier) {
    return Key.of(getInternalGenericDependencyType(), qualifier);
  }

  private static Type getInternalGenericDependencyType() {
    final Class<?> tokeClass = new TypeToken<InternalGenericDependency<InternalDependency>>() {}.getClass();
    final ParameterizedType superType = (ParameterizedType) tokeClass.getGenericSuperclass();
    return superType.getActualTypeArguments()[0];
  }

  private static Key<InternalGenericDependency<InternalDependency>[]> createInternalGenericArrayDependencyKey(
      final Annotation qualifier) {
    return Key.of(getInternalGenericArrayDependencyType(), qualifier);
  }

  private static Type getInternalGenericArrayDependencyType() {
    final Class<?> tokeClass = new TypeToken<InternalGenericDependency<InternalDependency>[]>() {}.getClass();
    final ParameterizedType superType = (ParameterizedType) tokeClass.getGenericSuperclass();
    return superType.getActualTypeArguments()[0];
  }


  @InternalQualifier
  @SingletonQualifier
  private class AnnotationHolder {
  }

  @SuppressWarnings("unused")
  private static class TypeToken<T> {
  }
}
