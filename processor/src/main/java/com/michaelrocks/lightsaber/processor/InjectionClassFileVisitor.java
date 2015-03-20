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

package com.michaelrocks.lightsaber.processor;

import com.michaelrocks.lightsaber.processor.files.ClassFileVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InjectionClassFileVisitor extends ClassFileVisitor {
    private final InjectionClassProducer classProducer = new InjectionClassProducer();

    public InjectionClassFileVisitor(final ClassFileVisitor classFileVisitor) {
        super(classFileVisitor);
    }

    @Override
    public void visitClassFile(final String path, final byte[] classData) throws IOException {
        final ClassReader classReader = new ClassReader(classData);
        final ClassWriter classWriter =
                new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classReader.accept(new RootVisitor(classWriter, classProducer), ClassReader.SKIP_FRAMES);
        super.visitClassFile(path, classWriter.toByteArray());
    }

    @Override
    public void visitEnd() throws IOException {
        super.visitEnd();

        // TODO: Support multiple errors in ProcessingException
        final List<ProcessingException> errors = classProducer.getErrors();
        if (!errors.isEmpty()) {
            final StringBuilder builder = new StringBuilder();
            for (final ProcessingException error : errors) {
                builder.append(error.getMessage());
                builder.append(System.lineSeparator());
            }
            throw new ProcessingException(builder.toString());
        }
    }

    private class InjectionClassProducer implements ClassProducer {
        private final List<ProcessingException> errors = new ArrayList<>();

        @Override
        public void produceClass(final String internalName, final byte[] classData) {
            try {
                visitClassFile(internalName + ".class", classData);
            } catch (final IOException exception) {
                final String message = String.format("Failed to produce class with %d bytes", classData.length);
                errors.add(new ProcessingException(new File(internalName), message, exception));
            }
        }

        public List<ProcessingException> getErrors() {
            return Collections.unmodifiableList(errors);
        }
    }
}
