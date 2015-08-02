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

public class AnnotationClassVisitor extends ClassVisitor {
    private AnnotationDescriptorBuilder annotationBuilder;

    public AnnotationClassVisitor() {
        super(Opcodes.ASM5);
    }

    public AnnotationDescriptor toAnnotation() {
        Validate.notNull(annotationBuilder);
        return annotationBuilder.build();
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        annotationBuilder = new AnnotationDescriptorBuilder(Type.getObjectType(name));
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        return new MethodVisitor(Opcodes.ASM5) {
            @Override
            public AnnotationVisitor visitAnnotationDefault() {
                return new AnnotationValueParser() {
                    @Override
                    public void visitEnd() {
                        Validate.notNull(annotationBuilder);
                        annotationBuilder.addDefaultValue(name, getValue());
                    }
                };
            }
        };
    }
}
