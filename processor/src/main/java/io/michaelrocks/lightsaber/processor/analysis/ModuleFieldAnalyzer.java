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
import io.michaelrocks.lightsaber.processor.annotations.AnnotationDescriptor;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationInstanceParser;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

class ModuleFieldAnalyzer extends ProcessorFieldVisitor {
    private final ModuleDescriptor.Builder moduleBuilder;

    private final String fieldName;
    private final String fieldDesc;

    private boolean isProviderField;
    private AnnotationDescriptor qualifier;

    public ModuleFieldAnalyzer(final ProcessorContext processorContext, final ModuleDescriptor.Builder moduleBuilder,
            final String fieldName, final String fieldDesc) {
        super(processorContext);
        this.moduleBuilder = moduleBuilder;
        this.fieldName = fieldName;
        this.fieldDesc = fieldDesc;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final Type annotationType = Type.getType(desc);
        if (Types.PROVIDES_TYPE.equals(annotationType)) {
            isProviderField = true;
        } else if (getProcessorContext().isQualifier(annotationType)) {
            if (qualifier == null) {
                return new AnnotationInstanceParser(annotationType) {
                    @Override
                    public void visitEnd() {
                        qualifier = getProcessorContext().getAnnotationRegistry().resolveAnnotation(toAnnotation());
                    }
                };
            } else {
                reportError("Field has multiple qualifier annotations: "
                        + moduleBuilder.getModuleType() + "." + fieldName + ": " + fieldDesc);
            }
        }

        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitEnd() {
        if (isProviderField) {
            final FieldDescriptor providerField = new FieldDescriptor(fieldName, fieldDesc);
            moduleBuilder.addProviderField(providerField, qualifier);
        }
        super.visitEnd();
    }
}
