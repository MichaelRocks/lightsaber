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

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class LightweightHashMap<K, V> implements IterableMap<K, V> {
  private static final int DEFAULT_CAPACITY = 16;
  private static final float DEFAULT_LOAD_FACTOR = 0.7f;
  private static final int MAXIMUM_CAPACITY = 1 << 29;

  private final transient float loadFactor;
  private transient int size;
  private transient Object[] data;
  private transient int threshold;
  private transient int modificationCount;

  public LightweightHashMap() {
    this(DEFAULT_CAPACITY);
  }

  public LightweightHashMap(final int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
  }

  public LightweightHashMap(final int initialCapacity, final float loadFactor) {
    final int capacity = calculateNewCapacity(initialCapacity);
    this.loadFactor = loadFactor;
    this.data = new Object[capacity << 1];
    this.threshold = calculateThreshold(capacity, loadFactor);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public boolean containsKey(final Object key) {
    final int index = findInTable(key);
    return getKeyAt(index) != null;
  }

  @Override
  public boolean containsValue(final Object value) {
    if (size == 0) {
      return false;
    }

    final MapIterator<K, V> iterator = iterator();
    while (iterator.hasNext()) {
      iterator.next();
      final V currentValue = iterator.getValue();
      if (value == currentValue || (value != null && value.equals(currentValue))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public V get(final Object key) {
    final int index = findInTable(key);
    if (index == -1) {
      return null;
    }
    final Object value = getValueAt(index);
    // noinspection unchecked
    return value == Null.VALUE ? null : (V) value;
  }

  @Override
  public V put(final K key, final V value) {
    modificationCount++;
    maybeEnsureCapacity();
    final int index = findInTable(key);
    if (index == -1) {
      throw new IllegalStateException("Map is full");
    }
    // noinspection unchecked
    return (V) setValueAt(index, key, value);
  }

  @Override
  public V remove(final Object key) {
    throw new UnsupportedOperationException("remove(Object) is not supported");
  }

  @Override
  public void putAll(@Nonnull final Map<? extends K, ? extends V> map) {
    modificationCount++;
    if (map.isEmpty()) {
      return;
    }

    ensureCapacity(calculateNewCapacity(size + map.size()));
    if (map instanceof IterableMap<?, ?>) {
      // noinspection unchecked
      final MapIterator<? extends K, ? extends V> iterator = ((IterableMap<? extends K, ? extends V>) map).iterator();
      while (iterator.hasNext()) {
        final Object key = iterator.next();
        final int index = findInTable(key);
        if (index != -1) {
          setValueAt(index, key, iterator.getValue());
        }
      }
    } else {
      for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
        final Object key = entry.getKey();
        final int index = findInTable(key);
        if (index != -1) {
          setValueAt(index, key, entry.getValue());
        }
      }
    }
  }

  @Override
  public void clear() {
    size = 0;
    Arrays.fill(data, null);
  }

  @Nonnull
  @Override
  public Set<K> keySet() {
    throw new UnsupportedOperationException("keySet() is not supported");
  }

  @Nonnull
  @Override
  public Collection<V> values() {
    throw new UnsupportedOperationException("values() is not supported");
  }

  @Nonnull
  @Override
  public Set<Entry<K, V>> entrySet() {
    throw new UnsupportedOperationException("entrySet() is not supported");
  }

  @Nonnull
  @Override
  public MapIterator<K, V> iterator() {
    return new AddOnlyHashMapIterator<K, V>(this);
  }

  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    }

    if (!(other instanceof Map)) {
      return false;
    }

    final Map<?, ?> that = (Map<?, ?>) other;
    if (that.size() != size()) {
      return false;
    }

    try {
      final MapIterator<K, V> iterator = iterator();
      while (iterator.hasNext()) {
        final Object key = iterator.next();
        final Object value = iterator.getValue();
        if (value == null) {
          if (!that.containsKey(key) || that.get(key) != null) {
            return false;
          }
        } else {
          if (!value.equals(that.get(key))) {
            return false;
          }
        }
      }
    } catch (final NullPointerException unused) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result = 0;
    final MapIterator<K, V> iterator = iterator();
    while (iterator.hasNext()) {
      final Object key = iterator.next();
      final Object value = iterator.getValue();
      final int keyHashCode = key == null ? 0 : key.hashCode();
      final int valueHashCode = value == null ? 0 : value.hashCode();
      result += keyHashCode ^ valueHashCode;
    }
    return result;
  }

  public String toString() {
    final MapIterator<K, V> iterator = iterator();
    if (!iterator.hasNext()) {
      return "{}";
    }

    final StringBuilder builder = new StringBuilder();
    builder.append('{');
    while (true) {
      final Object key = iterator.next();
      final Object value = iterator.getValue();
      builder.append(key == this ? "(this Map)" : key);
      builder.append('=');
      builder.append(value == this ? "(this Map)" : value);
      if (!iterator.hasNext()) {
        return builder.append('}').toString();
      }
      builder.append(',').append(' ');
    }
  }

  protected Object clone() throws CloneNotSupportedException {
    final LightweightHashMap<?, ?> result = (LightweightHashMap<?, ?>) super.clone();
    modificationCount = 0;
    return result;
  }

  protected int hashCode(final Object key) {
    return key == null ? 0 : key.hashCode();
  }

  protected boolean areKeysEqual(final Object key1, final Object key2) {
    return key1 == key2 || (key1 != null && key1.equals(key2));
  }

  private int findInTable(final Object key) {
    final int fromIndex = hashIndex(key);
    final int index = findInRange(key, fromIndex, data.length >>> 1);
    if (index != -1) {
      return index;
    }
    return findInRange(key, 0, fromIndex);
  }

  private int findInRange(final Object key, final int fromIndex, final int toIndex) {
    for (int i = fromIndex; i < toIndex; ++i) {
      final Object currentKey = getKeyAt(i);
      if (currentKey == null || (currentKey == Null.VALUE && key == null) || areKeysEqual(currentKey, key)) {
        return i;
      }
    }

    return -1;
  }

  private int hashIndex(final Object key) {
    return hashCode(key) & ((data.length >>> 1) - 1);
  }

  private Object getKeyAt(final int index) {
    return data[index << 1];
  }

  private Object getValueAt(final int index) {
    return data[(index << 1) + 1];
  }

  private Object setValueAt(final int index, final Object key, final Object value) {
    final int keyIndex = index << 1;
    data[keyIndex] = key == null ? Null.VALUE : key;
    final Object oldValue = data[keyIndex + 1];
    data[keyIndex + 1] = value == null ? Null.VALUE : value;
    if (oldValue == null) {
      size++;
    }
    // noinspection unchecked
    return oldValue == Null.VALUE ? null : (V) oldValue;
  }

  private void maybeEnsureCapacity() {
    if (size >= threshold) {
      final int newCapacity = data.length;
      if (newCapacity <= MAXIMUM_CAPACITY) {
        ensureCapacity(newCapacity);
      }
    }
  }

  private void ensureCapacity(final int newCapacity) {
    if (newCapacity <= data.length >>> 1) {
      return;
    }
    if (size == 0) {
      threshold = calculateThreshold(newCapacity, loadFactor);
      data = new Object[newCapacity << 1];
    } else {
      final Object[] oldData = data;
      data = new Object[newCapacity << 1];
      threshold = calculateThreshold(newCapacity, loadFactor);

      for (int i = 0, count = oldData.length; i < count; i += 2) {
        final Object escapedKey = oldData[i];
        if (escapedKey != null) {
          final Object key = escapedKey == Null.VALUE ? null : escapedKey;
          final int index = findInTable(key);
          final int keyIndex = index << 1;
          data[keyIndex] = escapedKey;
          data[keyIndex + 1] = oldData[i + 1];

          oldData[i] = null;
          oldData[i + 1] = null;
        }
      }
    }
  }

  private int calculateNewCapacity(final int proposedCapacity) {
    if (proposedCapacity >= MAXIMUM_CAPACITY) {
      return MAXIMUM_CAPACITY;
    } else if (proposedCapacity < 0) {
      return 1;
    } else {
      int newCapacity = proposedCapacity - 1;
      newCapacity |= newCapacity >>> 1;
      newCapacity |= newCapacity >>> 2;
      newCapacity |= newCapacity >>> 4;
      newCapacity |= newCapacity >>> 8;
      newCapacity |= newCapacity >>> 16;
      return newCapacity + 1;
    }
  }

  private int calculateThreshold(final int newCapacity, final float factor) {
    return (int) (newCapacity * factor);
  }

  private enum Null {
    VALUE
  }

  private static class AddOnlyHashMapIterator<K, V> implements MapIterator<K, V> {
    private static final int STATE_INITIAL = -1;
    private static final int STATE_UNKNOWN = 0;
    private static final int STATE_FINISHED = 1;
    private static final int STATE_CACHED = 2;

    private final LightweightHashMap<K, V> map;
    private final int expectedModificationCount;
    private int index = -2;
    private int nextIndex;
    private int state = STATE_INITIAL;

    private AddOnlyHashMapIterator(final LightweightHashMap<K, V> map) {
      this.map = map;
      this.expectedModificationCount = map.modificationCount;
    }

    @Override
    public boolean hasNext() {
      maybeCacheNext();
      return state != STATE_FINISHED;
    }

    @Override
    public K next() {
      maybeCacheNext();
      if (state == STATE_FINISHED) {
        throw new NoSuchElementException();
      }

      state = STATE_UNKNOWN;
      index = nextIndex;

      final Object key = map.data[index];
      // noinspection unchecked
      return key == Null.VALUE ? null : (K) key;
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("remove() is not supported");
    }

    @Override
    public V getValue() {
      if (state == STATE_INITIAL) {
        throw new IllegalStateException("next() must be called before getValue()");
      }
      if (state == STATE_FINISHED) {
        throw new NoSuchElementException();
      }

      final Object value = map.data[index + 1];
      // noinspection unchecked
      return value == Null.VALUE ? null : (V) value;
    }

    @Override
    public V setValue(final V value) {
      if (state == STATE_INITIAL) {
        throw new IllegalStateException("next() must be called before getValue()");
      }
      if (state == STATE_FINISHED) {
        throw new NoSuchElementException();
      }

      final int valueIndex = index + 1;
      final Object oldValue = map.data[valueIndex];
      map.data[valueIndex] = value == null ? Null.VALUE : value;
      // noinspection unchecked
      return oldValue == Null.VALUE ? null : (V) oldValue;

    }

    private void maybeCacheNext() {
      if (state != STATE_UNKNOWN && state != STATE_INITIAL) {
        return;
      }

      if (map.modificationCount != expectedModificationCount) {
        throw new ConcurrentModificationException();
      }

      for (int i = index + 2, count = map.data.length; i < count; i += 2) {
        if (map.data[i] != null) {
          state = STATE_CACHED;
          nextIndex = i;
          return;
        }
      }

      state = STATE_FINISHED;
    }
  }
}
