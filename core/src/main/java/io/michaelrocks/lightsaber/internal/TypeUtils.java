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

package io.michaelrocks.lightsaber.internal;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

public class TypeUtils {
  private TypeUtils() {
  }

  public static boolean equals(final Type type1, final Type type2) {
    if (type1 == type2) {
      return true;
    }

    if (type1 == null || type2 == null) {
      return false;
    }

    if (type1 instanceof Class<?> && type2 instanceof Class<?>) {
      return type1.equals(type2);
    } else if (type1 instanceof ParameterizedType && type2 instanceof ParameterizedType) {
      return equals((ParameterizedType) type1, (ParameterizedType) type2);
    } else if (type1 instanceof WildcardType && type2 instanceof WildcardType) {
      return equals((WildcardType) type1, (WildcardType) type2);
    } else if (type1 instanceof GenericArrayType && type2 instanceof GenericArrayType) {
      return equals((GenericArrayType) type1, (GenericArrayType) type2);
    } else if (type1 instanceof TypeVariable<?> && type2 instanceof TypeVariable<?>) {
      return equals((TypeVariable<?>) type1, (TypeVariable<?>) type2);
    } else {
      return type1.equals(type2);
    }
  }

  public static int hashCode(final Type type) {
    if (type == null) {
      return 0;
    }

    if (type instanceof Class<?>) {
      return type.hashCode();
    } else if (type instanceof ParameterizedType) {
      return hashCode((ParameterizedType) type);
    } else if (type instanceof WildcardType) {
      return hashCode((WildcardType) type);
    } else if (type instanceof GenericArrayType) {
      return hashCode((GenericArrayType) type);
    } else if (type instanceof TypeVariable<?>) {
      return hashCode((TypeVariable<?>) type);
    } else {
      return type.hashCode();
    }
  }

  private static boolean equals(final ParameterizedType type1, final ParameterizedType type2) {
    return equals(type1.getOwnerType(), type2.getOwnerType())
        && equals(type1.getRawType(), type2.getRawType())
        && equals(type1.getActualTypeArguments(), type2.getActualTypeArguments());
  }

  private static boolean equals(final WildcardType type1, final WildcardType type2) {
    return equals(type1.getUpperBounds(), type2.getUpperBounds())
        && equals(type1.getLowerBounds(), type2.getLowerBounds());
  }

  private static boolean equals(final GenericArrayType type1, final GenericArrayType type2) {
    return equals(type1.getGenericComponentType(), type2.getGenericComponentType());
  }

  private static boolean equals(final TypeVariable<?> type1, final TypeVariable<?> type2) {
    return equals(type1.getGenericDeclaration(), type2.getGenericDeclaration())
        && equals(type1.getName(), type2.getName())
        && equals(type1.getBounds(), type2.getBounds());
  }

  private static boolean equals(final Type[] types1, final Type[] types2) {
    if (types1 == types2) {
      return true;
    }

    if (types1 == null || types2 == null) {
      return false;
    }

    if (types1.length != types2.length) {
      return false;
    }

    for (int i = 0; i < types1.length; ++i) {
      if (!equals(types1[i], types2[i])) {
        return false;
      }
    }

    return true;
  }

  private static boolean equals(final GenericDeclaration declaration1, final GenericDeclaration declaration2) {
    if (declaration1 == declaration2) {
      return true;
    }

    if (declaration1 == null || declaration2 == null) {
      return false;
    }

    // TODO: Not sure that's a right way to compare GenericDeclaration instances.
    return equals(declaration1.getTypeParameters(), declaration2.getTypeParameters());
  }

  private static boolean equals(final Object object1, final Object object2) {
    return object1 == object2 || (object1 != null && object1.equals(object2));
  }

  private static int hashCode(final ParameterizedType type) {
    int result = 1;
    result = 31 * result + hashCode(type.getActualTypeArguments());
    result = 31 * result + hashCode(type.getOwnerType());
    result = 31 * result + hashCode(type.getRawType());
    return result;
  }

  private static int hashCode(final WildcardType type) {
    int result = 1;
    result = 31 * result + Arrays.hashCode(type.getUpperBounds());
    result = 31 * result + Arrays.hashCode(type.getLowerBounds());
    return result;
  }

  private static int hashCode(final GenericArrayType type) {
    return 31 + hashCode(type.getGenericComponentType());
  }

  private static int hashCode(final TypeVariable<?> type) {
    int result = 1;
    result = 31 * result + hashCode(type.getGenericDeclaration());
    result = 31 * result + type.getName().hashCode();
    result = 31 * result + hashCode(type.getBounds());
    return result;
  }

  // TODO: Not sure that's a right way to compute a hash code for GenericDeclaration.
  private static int hashCode(final GenericDeclaration declaration) {
    if (declaration == null) {
      return 0;
    }

    return hashCode(declaration.getTypeParameters());
  }

  private static int hashCode(final Type[] types) {
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
