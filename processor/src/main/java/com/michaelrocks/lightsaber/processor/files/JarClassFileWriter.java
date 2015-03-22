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

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class JarClassFileWriter extends ClassFileWriter {
    private final JarOutputStream stream;

    public JarClassFileWriter(final File targetFile) throws IOException {
        stream = new JarOutputStream(new FileOutputStream(targetFile));
    }

    @Override
    protected void writeFile(final String path, final byte[] fileData) throws IOException {
        final JarEntry entry = new JarEntry(path);
        stream.putNextEntry(entry);
        stream.write(fileData);
        stream.closeEntry();
    }

    @Override
    protected void createDirectory(final String path) throws IOException {
        final String directoryPath = path.endsWith("/") ? path : path + '/';
        final JarEntry entry = new JarEntry(directoryPath);
        stream.putNextEntry(entry);
        stream.closeEntry();
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(stream);
    }
}
