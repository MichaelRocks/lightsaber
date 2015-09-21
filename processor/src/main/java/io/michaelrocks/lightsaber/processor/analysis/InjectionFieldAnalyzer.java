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

import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.ProcessorFieldVisitor;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationInstanceParser;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedFieldDescriptor;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import io.michaelrocks.lightsaber.processor.signature.TypeSignatureParser;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

class InjectionFieldAnalyzer extends ProcessorFieldVisitor {
    private final InjectionTargetDescriptor.Builder injectionTargetBuilder;

    private final int access;
    private final String fieldName;
    private final String fieldDesc;
    private final String signature;

    private boolean isInjectableField;
    private AnnotationData qualifier;

    public InjectionFieldAnalyzer(final ProcessorContext processorContext,
            final InjectionTargetDescriptor.Builder injectionTargetBuilder,
            final int access, final String fieldName, final String fieldDesc, final String signature) {
        super(processorContext);
        this.injectionTargetBuilder = injectionTargetBuilder;
        this.access = access;
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
        this.signature = signature;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final Type annotationType = Type.getType(desc);
        if (Types.INJECT_TYPE.equals(annotationType)) {
            isInjectableField = true;
        } else if (getProcessorContext().isQualifier(annotationType)) {
            return new AnnotationInstanceParser(annotationType) {
                @Override
                public void visitEnd() {
                    final AnnotationData annotation =
                            getProcessorContext().getAnnotationRegistry().resolveAnnotation(toAnnotation());
                    if (qualifier == null) {
                        qualifier = annotation;
                    } else {
                        reportError("Field has multiple qualifiers: "
                                + injectionTargetBuilder.getTargetType()
                                + "." + fieldName + ": " + fieldDesc);

                    }
                }
            };
        }
        return null;
    }

    @Override
    public void visitEnd() {
        if (isInjectableField) {
            final Type fieldType = Type.getType(fieldDesc);
            final TypeSignature typeSignature =
                    TypeSignatureParser.parseTypeSignature(getProcessorContext(), signature, fieldType);
            final FieldDescriptor field = new FieldDescriptor(fieldName, typeSignature);
            final QualifiedFieldDescriptor qualifiedField = new QualifiedFieldDescriptor(field, qualifier);
            if ((access & Opcodes.ACC_STATIC) == 0) {
                injectionTargetBuilder.addInjectableField(qualifiedField);
            } else {
                injectionTargetBuilder.addInjectableStaticField(qualifiedField);
            }
        }
    }
}
