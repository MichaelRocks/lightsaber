/*
 * Copyright 2017 Michael Rozumyanskiy
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
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Key<T> {
  private final Type type;
  private final Annotation qualifier;

  public Key(@Nonnull final Type type) {
    this(type, null);
  }

  public Key(@Nonnull final Type type, @Nullable final Annotation qualifier) {
    this.type = type;
    this.qualifier = qualifier;
  }

  public static <T> Key<T> of(@Nonnull final Class<T> type) {
    return new Key<T>(type);
  }

  public static <T> Key<T> of(@Nonnull final Class<T> type, final Annotation annotation) {
    return new Key<T>(type, annotation);
  }

  public static <T> Key<T> of(@Nonnull final Type type) {
    return new Key<T>(type);
  }

  public static <T> Key<T> of(@Nonnull final Type type, final Annotation annotation) {
    return new Key<T>(type, annotation);
  }

  @Nonnull
  public Type getType() {
    return type;
  }

  @Nullable
  public Annotation getQualifier() {
    return qualifier;
  }

  @Override
  public boolean equals(@Nullable final Object object) {
    if (this == object) {
      return true;
    }

    if (object == null || getClass() != object.getClass()) {
      return false;
    }

    final Key<?> key = (Key<?>) object;
    return type.equals(key.type)
        && (qualifier != null ? qualifier.equals(key.qualifier) : key.qualifier == null);
  }

  @Override
  public int hashCode() {
    int result = 1;
    result = 31 * result + hashCode(type);
    result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Key{type=" + type + ", qualifier=" + qualifier + '}';
  }

  private int hashCode(final Type type) {
    if (type == null) {
      return 0;
    }

    if (type instanceof Class<?>) {
      return type.hashCode();
    } else if (type instanceof ParameterizedType) {
      final ParameterizedType parameterizedType = (ParameterizedType) type;
      int result = 1;
      result = 31 * result + hashCode(parameterizedType.getActualTypeArguments());
      result = 31 * result + hashCode(parameterizedType.getOwnerType());
      result = 31 * result + hashCode(parameterizedType.getRawType());
      return result;
    } else if (type instanceof GenericArrayType) {
      final GenericArrayType genericArrayType = (GenericArrayType) type;
      return 31 + hashCode(genericArrayType.getGenericComponentType());
    } else {
      return type.hashCode();
    }
  }

  private int hashCode(final Type[] types) {
    if (types == null) {
      return 0;
    }

    int result = 1;
    for (final Type type : types) {
      result = 31 * result + (type == null ? 0 : hashCode(type));
    }

    return result;
  }
}
