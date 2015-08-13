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

package io.michaelrocks.lightsaber.processor.commons;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.List;

public class CompositeClassVisitor extends ClassVisitor {
    final List<ClassVisitor> classVisitors = new ArrayList<>();

    public CompositeClassVisitor() {
        super(Opcodes.ASM5);
    }

    public void addClassVisitor(final ClassVisitor classVisitor) {
        classVisitors.add(classVisitor);
    }

    public boolean isEmpty() {
        return classVisitors.isEmpty();
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        for (final ClassVisitor classVisitor : classVisitors) {
            classVisitor.visit(version, access, name, signature, superName, interfaces);
        }
    }

    @Override
    public void visitSource(final String source, final String debug) {
        for (final ClassVisitor classVisitor : classVisitors) {
            classVisitor.visitSource(source, debug);
        }
    }

    @Override
    public void visitOuterClass(final String owner, final String name, final String desc) {
        for (final ClassVisitor classVisitor : classVisitors) {
            classVisitor.visitOuterClass(owner, name, desc);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final ClassVisitor classVisitor : classVisitors) {
            final AnnotationVisitor annotationVisitor = classVisitor.visitAnnotation(desc, visible);
            if (annotationVisitor != null) {
                compositeAnnotationVisitor.addAnnotationVisitor(annotationVisitor);
            }
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc,
            final boolean visible) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final ClassVisitor classVisitor : classVisitors) {
            final AnnotationVisitor annotationVisitor =
                    classVisitor.visitTypeAnnotation(typeRef, typePath, desc, visible);
            if (annotationVisitor != null) {
                compositeAnnotationVisitor.addAnnotationVisitor(annotationVisitor);
            }
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        for (final ClassVisitor classVisitor : classVisitors) {
            classVisitor.visitAttribute(attr);
        }
    }

    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        for (final ClassVisitor classVisitor : classVisitors) {
            classVisitor.visitInnerClass(name, outerName, innerName, access);
        }
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
            final Object value) {
        final CompositeFieldVisitor compositeFieldVisitor = new CompositeFieldVisitor();
        for (final ClassVisitor classVisitor : classVisitors) {
            final FieldVisitor fieldVisitor = classVisitor.visitField(access, name, desc, signature, value);
            if (fieldVisitor != null) {
                compositeFieldVisitor.addFieldVisitor(fieldVisitor);
            }
        }
        return compositeFieldVisitor.isEmpty() ? null : compositeFieldVisitor;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final CompositeMethodVisitor compositeMethodVisitor = new CompositeMethodVisitor();
        for (final ClassVisitor classVisitor : classVisitors) {
            final MethodVisitor methodVisitor = classVisitor.visitMethod(access, name, desc, signature, exceptions);
            if (methodVisitor != null) {
                compositeMethodVisitor.addMethodVisitor(methodVisitor);
            }
        }
        return compositeMethodVisitor.isEmpty() ? null : compositeMethodVisitor;
    }

    @Override
    public void visitEnd() {
        for (final ClassVisitor classVisitor : classVisitors) {
            classVisitor.visitEnd();
        }
    }
}
