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

package com.michaelrocks.lightsaber.processor.io;

import com.michaelrocks.lightsaber.processor.ProcessingException;
import org.apache.commons.collections4.iterators.IteratorIterable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class DirectoryClassFileReader extends ClassFileReader<File> {
    private final File classesDirectory;

    public DirectoryClassFileReader(final File classesDirectory) throws IOException {
        this.classesDirectory = classesDirectory;
    }

    @Override
    protected Iterable<File> iterateFiles() throws IOException {
        if (!classesDirectory.exists() || !classesDirectory.isDirectory()) {
            throw new ProcessingException("Invalid classes directory", classesDirectory.getAbsolutePath());
        }

        final Iterator<File> filesIterator =
                FileUtils.iterateFilesAndDirs(classesDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        return new IteratorIterable<>(filesIterator);
    }

    @Override
    protected boolean isDirectory(final File file) {
        return file.isDirectory();
    }

    @Override
    protected String getFilePath(final File file) {
        return classesDirectory.toPath().relativize(file.toPath()).toString();
    }

    @Override
    protected byte[] readAsByteArray(final File file) throws IOException {
        return FileUtils.readFileToByteArray(file);
    }

    @Override
    public void close() {
    }
}
