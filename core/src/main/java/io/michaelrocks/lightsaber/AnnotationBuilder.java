/*
 * Copyright 2020 Michael Rozumyanskiy
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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import io.michaelrocks.lightsaber.internal.AnnotationInvocationHandler;

public class AnnotationBuilder<T extends Annotation> {
  private final Class<? extends T> annotationClass;
  private Map<String, Object> values = null;

  public AnnotationBuilder(final Class<? extends T> annotationClass) {
    if (!annotationClass.isAnnotation()) {
      throw new IllegalArgumentException("Not an annotation class " + annotationClass);
    }

    this.annotationClass = annotationClass;
  }

  public AnnotationBuilder<T> addMember(final String member, final boolean value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, final char value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, final byte value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, final short value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, final int value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, final long value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, final float value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, final double value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final String value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final Class<?> value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final Enum<?> value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final Annotation value) {
    return addMemberInternal(member, value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final boolean[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final char[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final byte[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final short[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final int[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final long[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final float[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final double[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final String[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public AnnotationBuilder<T> addMember(final String member, @Nonnull final Class<?>[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public <E extends Enum<E>> AnnotationBuilder<T> addMember(final String member, @Nonnull final E[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public <A extends Annotation> AnnotationBuilder<T> addMember(final String member, @Nonnull final A[] value) {
    return addMemberInternal(member, value.length > 0 ? value.clone() : value);
  }

  public T build() {
    return AccessController.doPrivileged(
        new PrivilegedAction<T>() {
          @SuppressWarnings("unchecked")
          public T run() {
            final Map<String, ?> explicitValues = values == null ? Collections.<String, Object>emptyMap() : values;
            final InvocationHandler handler = new AnnotationInvocationHandler(annotationClass, explicitValues);
            return (T) Proxy.newProxyInstance(annotationClass.getClassLoader(), new Class<?>[] { annotationClass }, handler);
          }
        }
    );
  }

  private AnnotationBuilder<T> addMemberInternal(final String member, final Object value) {
    if (values == null) {
      values = new HashMap<String, Object>();
    }

    final Object oldValue = values.put(member, value);
    if (oldValue != null) {
      throw new IllegalArgumentException("Member with name " + member + " already exists");
    }

    return this;
  }
}
