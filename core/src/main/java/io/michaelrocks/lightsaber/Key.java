/*
 * Copyright 2015 Michael Rozumyanskiy
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

public class Key<T> {
    private final Class<T> type;
    private final Annotation qualifier;

    public Key(final Class<T> type) {
        this(type, null);
    }

    public Key(final Class<T> type, final Annotation qualifier) {
        this.type = type;
        this.qualifier = qualifier;
    }

    public Class<T> getType() {
        return type;
    }

    public Annotation getQualifier() {
        return qualifier;
    }

    @Override
    public boolean equals(final Object object) {
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
        int result = type.hashCode();
        result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Key{type=" + type + ", qualifier=" + qualifier + '}';
    }
}
