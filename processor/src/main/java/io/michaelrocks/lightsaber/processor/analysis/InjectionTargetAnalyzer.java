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
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ScopeDescriptor;
import io.michaelrocks.lightsaber.processor.signature.MethodSignature;
import io.michaelrocks.lightsaber.processor.signature.MethodSignatureParser;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import io.michaelrocks.lightsaber.processor.signature.TypeSignatureParser;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import javax.inject.Inject;

import static org.objectweb.asm.Opcodes.ASM5;

public class InjectionTargetAnalyzer extends ProcessorClassVisitor {
    private static final Type INJECT_TYPE = Type.getType(Inject.class);

    private InjectionTargetDescriptor.Builder injectionTargetDescriptorBuilder;

    public InjectionTargetAnalyzer(final ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        injectionTargetDescriptorBuilder = new InjectionTargetDescriptor.Builder(Type.getObjectType(name));
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final ScopeDescriptor scope = getProcessorContext().findScopeByAnnotationType(Type.getType(desc));
        if (scope != null) {
            injectionTargetDescriptorBuilder.setScope(scope);
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final String methodName = name;
        final String methodDesc = desc;

        if (MethodDescriptor.isDefaultConstructor(methodName, methodDesc)) {
            injectionTargetDescriptorBuilder.setHasDefaultConstructor(true);
        }

        final MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(ASM5, methodVisitor) {
            private boolean isInjectableMethod = false;

            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                final Type annotationType = Type.getType(desc);
                if (INJECT_TYPE.equals(annotationType)) {
                    isInjectableMethod = true;
                }
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();

                if (isInjectableMethod) {
                    final Type methodType = Type.getMethodType(methodDesc);
                    final MethodSignature methodSignature =
                            MethodSignatureParser.parseMethodSignature(getProcessorContext(), signature, methodType);
                    final MethodDescriptor methodDescriptor = new MethodDescriptor(methodName, methodSignature);
                    if (MethodDescriptor.isConstructor(methodName)) {
                        injectionTargetDescriptorBuilder.addInjectableConstructor(methodDescriptor);
                    } else {
                        injectionTargetDescriptorBuilder.addInjectableMethod(methodDescriptor);
                    }
                }
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
            private boolean isInjectableField = false;

            @Override
            public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                final Type annotationType = Type.getType(desc);
                if (INJECT_TYPE.equals(annotationType)) {
                    isInjectableField = true;
                }
                return super.visitAnnotation(desc, visible);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();

                if (isInjectableField) {
                    final Type fieldType = Type.getType(fieldDesc);
                    final TypeSignature typeSignature =
                            TypeSignatureParser.parseTypeSignature(getProcessorContext(), signature, fieldType);
                    final FieldDescriptor fieldDescriptor = new FieldDescriptor(fieldName, typeSignature);
                    injectionTargetDescriptorBuilder.addInjectableField(fieldDescriptor);
                }
            }
        };
    }

    @Override
    public void visitEnd() {
        final InjectionTargetDescriptor injectionTarget = injectionTargetDescriptorBuilder.build();
        if (!injectionTarget.getInjectableFields().isEmpty() || !injectionTarget.getInjectableMethods().isEmpty()) {
            getProcessorContext().addInjectableTarget(injectionTarget);
        }
        if (injectionTarget.getInjectableConstructors().size() > 1) {
            final String separator = System.lineSeparator() + "  ";
            final String constructors =
                    StringUtils.join(injectionTarget.getInjectableConstructors(), separator);
            reportError("Class has multiple injectable constructors:" + separator + constructors);
        } else if (!injectionTarget.getInjectableConstructors().isEmpty()) {
            getProcessorContext().addProvidableTarget(injectionTarget);
        }

        super.visitEnd();
    }
}
