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
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

public class CompositeAnnotationVisitor extends AnnotationVisitor implements CompositeVisitor<AnnotationVisitor> {
    private final List<AnnotationVisitor> annotationVisitors = new ArrayList<>();

    public CompositeAnnotationVisitor() {
        super(Opcodes.ASM5);
    }

    @Override
    public void addVisitor(final AnnotationVisitor visitor) {
        annotationVisitors.add(visitor);
    }

    @Override
    public boolean isEmpty() {
        return annotationVisitors.isEmpty();
    }

    @Override
    public void visit(final String name, final Object value) {
        for (final AnnotationVisitor annotationVisitor : annotationVisitors) {
            annotationVisitor.visit(name, value);
        }
    }

    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        for (final AnnotationVisitor annotationVisitor : annotationVisitors) {
            annotationVisitor.visitEnum(name, desc, value);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final AnnotationVisitor annotationVisitor : annotationVisitors) {
            final AnnotationVisitor innerAnnotationVisitor = annotationVisitor.visitAnnotation(name, desc);
            CompositeVisitorHelper.addVisitor(compositeAnnotationVisitor, innerAnnotationVisitor);
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final AnnotationVisitor annotationVisitor : annotationVisitors) {
            final AnnotationVisitor innerAnnotationVisitor = annotationVisitor.visitArray(name);
            CompositeVisitorHelper.addVisitor(compositeAnnotationVisitor, innerAnnotationVisitor);
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public void visitEnd() {
        for (final AnnotationVisitor annotationVisitor : annotationVisitors) {
            annotationVisitor.visitEnd();
        }
    }
}
