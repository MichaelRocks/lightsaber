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

package io.michaelrocks.lightsaber;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AnnotationInvocationHandler implements InvocationHandler {
  private final Class<? extends Annotation> annotationClass;
  private final Map<String, Member> members;

  AnnotationInvocationHandler(@Nonnull final Class<? extends Annotation> annotationClass, @Nonnull final Map<String, ?> values) {
    this.annotationClass = annotationClass;
    this.members = AccessController.doPrivileged(
        new PrivilegedAction<Map<String, Member>>() {
          @Override
          public Map<String, Member> run() {
            return getMembers(annotationClass, values);
          }
        }
    );
  }

  @Override
  public int hashCode() {
    return 31 * annotationClass.hashCode() + invokeHashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    } else if (!(other instanceof AnnotationInvocationHandler)) {
      return false;
    }

    final AnnotationInvocationHandler that = (AnnotationInvocationHandler) other;
    if (!annotationClass.equals(that.annotationClass)) {
      return false;
    }

    if (members.size() != that.members.size()) {
      return false;
    }

    for (final Member member : members.values()) {
      final String memberName = member.method.getName();
      final Member thatMember = that.members.get(memberName);
      if (thatMember == null || !member.value.equals(thatMember.value)) {
        return false;
      }
    }

    return true;
  }

  @Override
  public Object invoke(final Object proxy, final Method method, final Object[] arguments) throws Exception {
    final String methodName = method.getName();

    if (method.getDeclaringClass() != annotationClass) {
      if (methodName.equals("equals") && method.getParameterTypes().length == 1) {
        return invokeEquals(arguments[0]);
      } else if (methodName.equals("hashCode")) {
        return invokeHashCode();
      } else if (methodName.equals("toString")) {
        return invokeToString();
      } else if (methodName.equals("annotationType")) {
        return annotationClass;
      }

      throw new IllegalStateException("Unexpected method is called: " + method);
    }

    return getMember(methodName);
  }

  private boolean invokeEquals(@Nullable final Object other) throws Exception {
    if (this == other) {
      return true;
    } else if (!annotationClass.isInstance(other)) {
      return false;
    } else if (Proxy.isProxyClass(other.getClass())) {
      final InvocationHandler handler = Proxy.getInvocationHandler(other);
      if (handler instanceof AnnotationInvocationHandler) {
        return equals(handler);
      }
    }

    for (final Member member : members.values()) {
      final Object thisValue = member.value;
      final Object thatValue;
      try {
        thatValue = member.method.invoke(other);
      } catch (IllegalAccessException exception) {
        throw new InvocationTargetException(exception);
      }

      if (!valueEquals(thisValue, thatValue)) {
        return false;
      }
    }

    return true;
  }

  @SuppressWarnings("ConstantConditions")
  private static boolean valueEquals(final Object value1, final Object value2) {
    Class<?> value1Class = value1.getClass();

    if (!value1Class.isArray()) {
      return value1.equals(value2);
    } else if (value1 instanceof Object[] && value2 instanceof Object[]) {
      return Arrays.equals((Object[]) value1, (Object[]) value2);
    } else if (value1Class != value2.getClass()) {
      return false;
    } else if (value1Class == boolean[].class) {
      return Arrays.equals((boolean[]) value1, (boolean[]) value2);
    } else if (value1Class == char[].class) {
      return Arrays.equals((char[]) value1, (char[]) value2);
    } else if (value1Class == byte[].class) {
      return Arrays.equals((byte[]) value1, (byte[]) value2);
    } else if (value1Class == short[].class) {
      return Arrays.equals((short[]) value1, (short[]) value2);
    } else if (value1Class == int[].class) {
      return Arrays.equals((int[]) value1, (int[]) value2);
    } else if (value1Class == long[].class) {
      return Arrays.equals((long[]) value1, (long[]) value2);
    } else if (value1Class == float[].class) {
      return Arrays.equals((float[]) value1, (float[]) value2);
    } else if (value1Class == double[].class) {
      return Arrays.equals((double[]) value1, (double[]) value2);
    }

    throw new IllegalStateException("Unknown array type " + value1Class);
  }

  private int invokeHashCode() {
    int result = 0;
    for (final Member member : members.values()) {
      result += (127 * member.method.getName().hashCode()) ^ valueHashCode(member.value);
    }

    return result;
  }

  private static int valueHashCode(Object value) {
    final Class<?> valueClass = value.getClass();
    if (!valueClass.isArray()) {
      return value.hashCode();
    }

    if (valueClass == boolean[].class) {
      return Arrays.hashCode((boolean[]) value);
    } else if (valueClass == char[].class) {
      return Arrays.hashCode((char[]) value);
    } else if (valueClass == byte[].class) {
      return Arrays.hashCode((byte[]) value);
    } else if (valueClass == short[].class) {
      return Arrays.hashCode((short[]) value);
    } else if (valueClass == int[].class) {
      return Arrays.hashCode((int[]) value);
    } else if (valueClass == long[].class) {
      return Arrays.hashCode((long[]) value);
    } else if (valueClass == float[].class) {
      return Arrays.hashCode((float[]) value);
    } else if (valueClass == double[].class) {
      return Arrays.hashCode((double[]) value);
    }

    return Arrays.hashCode((Object[]) value);
  }

  private String invokeToString() {
    StringBuilder result = new StringBuilder(128);
    result.append('@');
    result.append(annotationClass.getName());
    result.append('(');

    boolean isFirstValue = true;
    for (final Member member : members.values()) {
      if (!isFirstValue) {
        result.append(", ");
      } else {
        isFirstValue = false;
      }

      result.append(member.method.getName());
      result.append('=');
      result.append(valueToString(member.value));
    }

    result.append(')');
    return result.toString();
  }

  private static String valueToString(Object value) {
    final Class<?> valueClass = value.getClass();
    if (!valueClass.isArray()) {
      return value.toString();
    }

    if (valueClass == boolean[].class) {
      return Arrays.toString((boolean[]) value);
    } else if (valueClass == char[].class) {
      return Arrays.toString((char[]) value);
    } else if (valueClass == byte[].class) {
      return Arrays.toString((byte[]) value);
    } else if (valueClass == short[].class) {
      return Arrays.toString((short[]) value);
    } else if (valueClass == int[].class) {
      return Arrays.toString((int[]) value);
    } else if (valueClass == long[].class) {
      return Arrays.toString((long[]) value);
    } else if (valueClass == float[].class) {
      return Arrays.toString((float[]) value);
    } else if (valueClass == double[].class) {
      return Arrays.toString((double[]) value);
    }

    return Arrays.toString((Object[]) value);
  }

  @Nonnull
  private Object getMember(final String memberName) {
    final Member member = members.get(memberName);
    if (member == null) {
      throw new IncompleteAnnotationException(annotationClass, memberName);
    }

    final Object result = member.value;
    if (result.getClass().isArray()) {
      return maybeCloneArray(result);
    }

    return result;
  }

  @Nonnull
  private static Map<String, Member> getMembers(@Nonnull final Class<? extends Annotation> annotationClass,
      @Nonnull final Map<String, ?> explicitValues) {

    final Map<String, Member> members = new HashMap<String, Member>();
    final Method[] methods = annotationClass.getDeclaredMethods();
    AccessibleObject.setAccessible(methods, true);

    for (final Method method : methods) {
      final String memberName = method.getName();
      Object value = explicitValues.get(memberName);
      if (value == null) {
        final Object defaultValue = method.getDefaultValue();
        if (defaultValue != null) {
          value = defaultValue;
        } else {
          throw new IncompleteAnnotationException(annotationClass, memberName);
        }
      }

      members.put(memberName, new Member(method, value));
    }

    for (final String memberName : explicitValues.keySet()) {
      if (!members.containsKey(memberName)) {
        throw new IllegalArgumentException(annotationClass.getName() + " doesn't have a member " + memberName);
      }
    }

    return members;
  }

  private static Object maybeCloneArray(final Object array) {
    final int length = Array.getLength(array);
    if (length == 0) {
      return array;
    }

    final Class<?> arrayClass = array.getClass();
    if (arrayClass == boolean[].class) {
      return ((boolean[]) array).clone();
    } else if (arrayClass == char[].class) {
      return ((char[]) array).clone();
    } else if (arrayClass == byte[].class) {
      return ((byte[]) array).clone();
    } else if (arrayClass == short[].class) {
      return ((short[]) array).clone();
    } else if (arrayClass == int[].class) {
      return ((int[]) array).clone();
    } else if (arrayClass == long[].class) {
      return ((long[]) array).clone();
    } else if (arrayClass == float[].class) {
      return ((float[]) array).clone();
    } else if (arrayClass == double[].class) {
      return ((double[]) array).clone();
    }

    return ((Object[]) array).clone();
  }

  private static class Member {
    Method method;
    Object value;

    Member(final Method method, final Object value) {
      this.method = method;
      this.value = value;
    }
  }
}
