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

package io.michaelrocks.lightsaber.processor.graph;

import io.michaelrocks.lightsaber.processor.descriptors.ClassDescriptor;
import io.michaelrocks.lightsaber.processor.io.ClassFileReader;
import io.michaelrocks.lightsaber.processor.io.ClassFileVisitor;
import io.michaelrocks.lightsaber.processor.io.DirectoryClassFileTraverser;
import io.michaelrocks.lightsaber.processor.io.JarClassFileTraverser;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.ASM5;

public class TypeGraphBuilder {
    private final Map<Type, ClassDescriptor> classDescriptors = new HashMap<>();

    public void addClassesFromJar(final File jarFile) throws IOException {
        try (final ClassFileReader classFileReader = new ClassFileReader(new JarClassFileTraverser(jarFile))) {
            addClassesFromReader(classFileReader);
        } catch (final Exception exception) {
            throw new IOException(exception);
        }
    }

    public void addClassesFromClasses(final File classesDir) throws IOException {
        try (final ClassFileReader classFileReader = new ClassFileReader(new DirectoryClassFileTraverser(classesDir))) {
            addClassesFromReader(classFileReader);
        } catch (final Exception exception) {
            throw new IOException(exception);
        }
    }

    public void addClassesFromReader(final ClassFileReader classFileReader) throws IOException {
        classFileReader.accept(new ClassFileVisitor(null) {
            @Override
            public void visitClassFile(final String path, final byte[] classData) throws IOException {
                final ClassReader classReader = new ClassReader(classData);
                final ClassDescriptorReader classDescriptorReader = new ClassDescriptorReader();
                classReader.accept(classDescriptorReader,
                        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                final ClassDescriptor classDescriptor = classDescriptorReader.getClassDescriptor();
                classDescriptors.put(classDescriptor.getClassType(), classDescriptor);
            }
        });
    }

    public TypeGraph build() {
        return new TypeGraph(classDescriptors);
    }

    private static final class ClassDescriptorReader extends ClassVisitor {
        private ClassDescriptor classDescriptor;

        public ClassDescriptorReader() {
            super(ASM5);
        }

        public ClassDescriptor getClassDescriptor() {
            Validate.notNull(classDescriptor);
            return classDescriptor;
        }

        @Override
        public void visit(final int version, final int access, final String name, final String signature,
                final String superName, final String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            classDescriptor = new ClassDescriptor(name, superName, interfaces);
        }
    }
}
