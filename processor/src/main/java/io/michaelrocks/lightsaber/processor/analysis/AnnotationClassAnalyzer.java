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

package io.michaelrocks.lightsaber.processor.analysis;

import io.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationClassVisitor;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import javax.inject.Qualifier;

public class AnnotationClassAnalyzer extends ProcessorClassVisitor {
    private static final String QUALIFIER_DESCRIPTOR = Type.getDescriptor(Qualifier.class);

    private final AnnotationClassVisitor annotationClassVisitor;
    private boolean isQualifier;

    public AnnotationClassAnalyzer(final ProcessorContext processorContext) {
        this(processorContext, new AnnotationClassVisitor());
    }

    private AnnotationClassAnalyzer(final ProcessorContext processorContext,
            final AnnotationClassVisitor annotationClassVisitor) {
        super(processorContext, annotationClassVisitor);
        this.annotationClassVisitor = annotationClassVisitor;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        isQualifier = false;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if (QUALIFIER_DESCRIPTOR.equals(desc)) {
            isQualifier = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();

        final AnnotationData annotation = annotationClassVisitor.toAnnotation();
        getProcessorContext().getAnnotationRegistry().addAnnotationDefaults(annotation);
        if (isQualifier) {
            getProcessorContext().addQualifier(annotation.getType());
        }
    }
}
