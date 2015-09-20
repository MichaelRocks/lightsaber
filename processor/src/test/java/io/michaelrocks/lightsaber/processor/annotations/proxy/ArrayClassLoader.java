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

package io.michaelrocks.lightsaber.processor.annotations.proxy;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@ParametersAreNonnullByDefault
public class ArrayClassLoader extends ClassLoader {
    private final Set<String> loadedClasses = new HashSet<>();
    private final Map<String, byte[]> pendingClasses = new HashMap<>();

    public ArrayClassLoader() {
    }

    public ArrayClassLoader(final ClassLoader parentClassLoader) {
        super(parentClassLoader);
    }

    public void addClass(final String name, final byte[] bytes) {
        Validate.isTrue(!loadedClasses.contains(name));
        pendingClasses.put(name, bytes);
    }

    public boolean hasClass(final String name) {
        return loadedClasses.contains(name) || pendingClasses.containsKey(name);
    }

    @Override
    @Nonnull
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        final Class<?> loadedClass;
        final byte[] bytes = pendingClasses.remove(name);
        if (bytes == null) {
            loadedClass = findLoadedClass(name);
        } else {
            loadedClass = defineClass(name, bytes, 0, bytes.length);
            loadedClasses.add(name);
        }

        if (loadedClass != null) {
            return loadedClass;
        }

        throw new ClassNotFoundException(name);
    }
}
