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

package io.michaelrocks.lightsaber.internal;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public final class ParameterizedTypeImpl implements ParameterizedType {
  private final Type ownerType;
  private final Class<?> rawType;
  private final Type[] typeArguments;

  public ParameterizedTypeImpl(final Type ownerType, final Class<?> rawType, final Type... typeArguments) {
    this.ownerType = ownerType;
    this.rawType = rawType;
    this.typeArguments = typeArguments;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return typeArguments.clone();
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public Type getOwnerType() {
    return ownerType;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder
        .append(rawType.getCanonicalName())
        .append('<');

    if (typeArguments.length > 0) {
      builder.append(TypeUtils.getTypeName(typeArguments[0]));
      for (int i = 1, count = typeArguments.length; i < count; ++i) {
        builder
            .append(", ")
            .append(TypeUtils.getTypeName(typeArguments[i]));
      }
    }

    return builder
        .append('>')
        .toString();
  }
}
