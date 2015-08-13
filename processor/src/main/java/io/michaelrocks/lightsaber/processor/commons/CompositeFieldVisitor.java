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
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.List;

public class CompositeFieldVisitor extends FieldVisitor {
    private final List<FieldVisitor> fieldVisitors = new ArrayList<>();

    public CompositeFieldVisitor() {
        super(Opcodes.ASM5);
    }

    public void addFieldVisitor(final FieldVisitor fieldVisitor) {
        fieldVisitors.add(fieldVisitor);
    }

    public boolean isEmpty() {
        return fieldVisitors.isEmpty();
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final FieldVisitor fieldVisitor : fieldVisitors) {
            final AnnotationVisitor annotationVisitor = fieldVisitor.visitAnnotation(desc, visible);
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
        for (final FieldVisitor fieldVisitor : fieldVisitors) {
            final AnnotationVisitor annotationVisitor =
                    fieldVisitor.visitTypeAnnotation(typeRef, typePath, desc, visible);
            if (annotationVisitor != null) {
                compositeAnnotationVisitor.addAnnotationVisitor(annotationVisitor);
            }
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        for (final FieldVisitor fieldVisitor : fieldVisitors) {
            fieldVisitor.visitAttribute(attr);
        }
    }

    @Override
    public void visitEnd() {
        for (final FieldVisitor fieldVisitor : fieldVisitors) {
            fieldVisitor.visitEnd();
        }
    }
}
