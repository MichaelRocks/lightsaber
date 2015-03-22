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

package com.michaelrocks.lightsaber.processor.files;

import com.michaelrocks.lightsaber.processor.ProcessingException;
import org.apache.commons.collections4.iterators.EnumerationIterator;
import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassFileReader extends ClassFileReader<JarEntry> {
    private final JarFile jarFile;

    public JarClassFileReader(final File sourceFile) throws IOException {
        try {
            jarFile = new JarFile(sourceFile, true);
        } catch (final IOException exception) {
            throw new ProcessingException(new File(sourceFile.getName()), exception);
        }
    }

    @Override
    protected Iterable<JarEntry> iterateFiles() throws IOException {
        final Iterator<JarEntry> entriesIterator = new EnumerationIterator<>(jarFile.entries());
        return new IteratorIterable<>(entriesIterator);
    }

    @Override
    protected boolean isDirectory(final JarEntry entry) {
        return entry.isDirectory();
    }

    @Override
    protected String getFilePath(final JarEntry entry) {
        return entry.getName();
    }

    @Override
    protected byte[] readAsByteArray(final JarEntry entry) throws IOException {
        try (final InputStream stream = jarFile.getInputStream(entry)) {
            return IOUtils.toByteArray(stream);
        }
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(jarFile);
    }
}
