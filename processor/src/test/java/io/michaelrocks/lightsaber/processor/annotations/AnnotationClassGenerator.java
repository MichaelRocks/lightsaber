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

package io.michaelrocks.lightsaber.processor.annotations;

import io.michaelrocks.lightsaber.processor.descriptors.EnumValueDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class AnnotationClassGenerator {
    private final ClassVisitor classVisitor;

    private AnnotationClassGenerator(final ClassVisitor classVisitor) {
        this.classVisitor = classVisitor;
    }

    public static AnnotationClassGenerator create(final ClassVisitor classVisitor, final Type annotationType) {
        final AnnotationClassGenerator builder = new AnnotationClassGenerator(classVisitor);
        classVisitor.visit(
                V1_6,
                ACC_PUBLIC | ACC_ANNOTATION | ACC_ABSTRACT | ACC_INTERFACE,
                annotationType.getInternalName(),
                null,
                Type.getType(Object.class).getInternalName(),
                new String[] { Type.getType(Annotation.class).getInternalName() });
        return builder;
    }

    public AnnotationClassGenerator addMethod(final String name, final Type type) {
        addMethod(name, type, null);
        return this;
    }

    public AnnotationClassGenerator addMethod(final String name, final Type type, final Object defaultValue) {
        addMethod(MethodDescriptor.forMethod(name, type), defaultValue);
        return this;
    }

    private void addMethod(final MethodDescriptor method, final Object defaultValue) {
        final MethodVisitor methodVisitor = classVisitor.visitMethod(
                ACC_PUBLIC | ACC_ABSTRACT,
                method.getName(),
                method.getDescriptor(),
                null,
                null);
        if (defaultValue != null) {
            final AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotationDefault();
            addValue(annotationVisitor, null, defaultValue);
            annotationVisitor.visitEnd();
        }
        methodVisitor.visitEnd();
    }

    private void addValue(final AnnotationVisitor annotationVisitor, final String name, final Object defaultValue) {
        if (defaultValue instanceof EnumValueDescriptor) {
            final EnumValueDescriptor enumValue = (EnumValueDescriptor) defaultValue;
            annotationVisitor.visitEnum(name, enumValue.getType().getDescriptor(), enumValue.getValue());
        } else if (defaultValue instanceof AnnotationDescriptor) {
            final AnnotationDescriptor annotation = (AnnotationDescriptor) defaultValue;
            final AnnotationVisitor innerAnnotationVisitor =
                    annotationVisitor.visitAnnotation(name, annotation.getType().getDescriptor());
            for (final Map.Entry<String, Object> entry : annotation.getValues().entrySet()) {
                addValue(innerAnnotationVisitor, entry.getKey(), entry.getValue());
            }
            innerAnnotationVisitor.visitEnd();
        } else if (defaultValue instanceof EnumValueDescriptor[] || defaultValue instanceof AnnotationDescriptor[]) {
            final AnnotationVisitor innerAnnotationVisitor = annotationVisitor.visitArray(name);
            for (int i = 0, length = Array.getLength(defaultValue); i < length; ++i) {
                addValue(innerAnnotationVisitor, null, Array.get(defaultValue, i));
            }
            innerAnnotationVisitor.visitEnd();
        } else {
            annotationVisitor.visit(name, defaultValue);
        }
    }

    public void generate() {
        classVisitor.visitEnd();
    }
}
