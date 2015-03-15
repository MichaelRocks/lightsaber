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

import java.io.IOException;

public abstract class ClassFileWriter extends ClassFileVisitor implements AutoCloseable {
    public ClassFileWriter() {
        super(null);
    }

    @Override
    public void visitClassFile(final String path, final byte[] classData) throws IOException {
        writeFile(path, classData);
    }

    @Override
    public void visitOtherFile(final String path, final byte[] fileData) throws IOException {
        writeFile(path, fileData);
    }

    @Override
    public void visitDirectory(final String path) throws IOException {
        createDirectory(path);
    }

    protected abstract void writeFile(String path, byte[] fileData) throws IOException;

    protected abstract void createDirectory(String path) throws IOException;
}
