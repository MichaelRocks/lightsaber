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

import java.lang.reflect.Type;

public class PolymorphicKeyHashMap<V> extends LightweightHashMap<Object, V> {
  public PolymorphicKeyHashMap() {
  }

  public PolymorphicKeyHashMap(final int initialCapacity) {
    super(initialCapacity);
  }

  public PolymorphicKeyHashMap(final int initialCapacity, final float loadFactor) {
    super(initialCapacity, loadFactor);
  }

  @Override
  protected int hashCode(final Object key) {
    if (key instanceof Class<?>) {
      return key.hashCode();
    }
    if (key instanceof Type) {
      return TypeUtils.hashCode((Type) key);
    } else {
      return key.hashCode();
    }
  }

  @Override
  protected boolean areKeysEqual(final Object key1, final Object key2) {
    if (key1 == key2) {
      return true;
    }

    if (key1 instanceof Class<?> && key2 instanceof Class<?>) {
      return key1.equals(key2);
    }
    if (key1 instanceof Type && key2 instanceof Type) {
      return TypeUtils.equals((Type) key1, (Type) key2);
    } else {
      return key1.equals(key2);
    }
  }
}
