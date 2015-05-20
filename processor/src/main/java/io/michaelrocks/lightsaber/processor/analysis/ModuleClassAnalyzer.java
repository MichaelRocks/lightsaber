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

import io.michaelrocks.lightsaber.Provides;
import io.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ScopeDescriptor;
import io.michaelrocks.lightsaber.processor.signature.MethodSignature;
import io.michaelrocks.lightsaber.processor.signature.MethodSignatureParser;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ASM5;

public class ModuleClassAnalyzer extends ProcessorClassVisitor {
    private ModuleDescriptor.Builder moduleDescriptorBuilder;

    public ModuleClassAnalyzer(final ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        moduleDescriptorBuilder = new ModuleDescriptor.Builder(Type.getObjectType(name));
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final String methodName = name;
        final String methodDesc = desc;
        final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(ASM5, methodVisitor) {
            private ScopeDescriptor scope;
            private boolean isProviderMethod;

            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                if (Type.getDescriptor(Provides.class).equals(desc)) {
                    isProviderMethod = true;
                } else if (scope != null) {
                    scope = getProcessorContext().findScopeByAnnotationType(Type.getType(desc));
                }

                return super.visitAnnotation(desc, visible);
            }

            @Override
            public void visitEnd() {
                if (isProviderMethod) {
                    final Type methodType = Type.getMethodType(methodDesc);
                    final MethodSignature methodSignature =
                            MethodSignatureParser.parseMethodSignature(getProcessorContext(), signature, methodType);
                    final MethodDescriptor providerMethod = new MethodDescriptor(methodName, methodSignature);
                    moduleDescriptorBuilder.addProviderMethod(providerMethod, scope);
                }
                super.visitEnd();
            }
        };
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
            final Object value) {
        final String fieldName = name;
        final String fieldDesc = desc;
        final FieldVisitor fieldVisitor = super.visitField(access, name, desc, signature, value);
        return new FieldVisitor(ASM5, fieldVisitor) {
            private boolean isProviderField;

            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                if (Type.getDescriptor(Provides.class).equals(desc)) {
                    isProviderField = true;
                }

                return super.visitAnnotation(desc, visible);
            }

            @Override
            public void visitEnd() {
                if (isProviderField) {
                    final FieldDescriptor providerField = new FieldDescriptor(fieldName, fieldDesc);
                    moduleDescriptorBuilder.addProviderField(providerField);
                }
                super.visitEnd();
            }
        };
    }

    @Override
    public void visitEnd() {
        final ModuleDescriptor module = moduleDescriptorBuilder.build();
        getProcessorContext().addModule(module);
        super.visitEnd();
    }
}
