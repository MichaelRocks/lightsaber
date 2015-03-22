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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DirectoryClassFileWriter extends ClassFileWriter {
    private final File classesDirectory;

    public DirectoryClassFileWriter(final File classesDirectory) throws IOException {
        this.classesDirectory = classesDirectory;
    }

    @Override
    protected void writeFile(final String path, final byte[] fileData) throws IOException {
        final File file = new File(classesDirectory, path);
        final File directory = file.getParentFile();
        if (directory != null) {
            // noinspection ResultOfMethodCallIgnored
            directory.mkdirs();
        }
        FileUtils.writeByteArrayToFile(file, fileData);
    }

    @Override
    protected void createDirectory(final String path) throws IOException {
        final File file = new File(classesDirectory, path);
        // noinspection ResultOfMethodCallIgnored
        file.mkdirs();
    }

    @Override
    public void close() {
    }
}
