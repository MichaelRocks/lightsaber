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

package io.michaelrocks.lightsaber.processor.io;

import java.io.IOException;

public abstract class ClassFileReader<T> implements AutoCloseable {
    public void accept(final ClassFileVisitor visitor) throws IOException {
        for (final T file : iterateFiles()) {
            processFile(visitor, file);
        }
        visitor.visitEnd();
    }

    private void processFile(final ClassFileVisitor visitor, final T file) throws IOException {
        final String path = getFilePath(file);
        if (path.isEmpty()) {
            // Skip empty path as it's the root path.
        } else if (isDirectory(file)) {
            visitor.visitDirectory(path);
        } else {
            final byte[] fileData = readAsByteArray(file);
            if (path.endsWith(".class")) {
                visitor.visitClassFile(path, fileData);
            } else {
                visitor.visitOtherFile(path, fileData);
            }
        }
    }

    protected abstract Iterable<T> iterateFiles() throws IOException;

    protected abstract boolean isDirectory(T file);

    protected abstract String getFilePath(T file);

    protected abstract byte[] readAsByteArray(T file) throws IOException;
}
