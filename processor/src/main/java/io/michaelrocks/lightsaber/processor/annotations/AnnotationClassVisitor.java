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

import org.apache.commons.lang3.Validate;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AnnotationClassVisitor extends ClassVisitor {
    private AnnotationDescriptorBuilder annotationDescriptorBuilder;
    private AnnotationDataBuilder annotationDataBuilder;

    public AnnotationClassVisitor() {
        super(Opcodes.ASM5);
    }

    public AnnotationDescriptor toAnnotationDescriptor() {
        Validate.notNull(annotationDescriptorBuilder);
        return annotationDescriptorBuilder.build();
    }

    public AnnotationData toAnnotationData() {
        Validate.notNull(annotationDescriptorBuilder);
        return annotationDataBuilder.build();
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        final Type annotationType = Type.getObjectType(name);
        annotationDescriptorBuilder = new AnnotationDescriptorBuilder(annotationType);
        annotationDataBuilder = new AnnotationDataBuilder(annotationType);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        Validate.notNull(annotationDescriptorBuilder);
        annotationDescriptorBuilder.addField(name, Type.getReturnType(desc));
        return new MethodVisitor(Opcodes.ASM5) {
            @Override
            public AnnotationVisitor visitAnnotationDefault() {
                return new AnnotationValueParser() {
                    @Override
                    public void visitEnd() {
                        annotationDataBuilder.addDefaultValue(name, getValue());
                    }
                };
            }
        };
    }
}
