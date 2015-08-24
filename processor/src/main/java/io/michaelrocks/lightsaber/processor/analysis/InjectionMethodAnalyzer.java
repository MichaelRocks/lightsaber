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
import io.michaelrocks.lightsaber.processor.ProcessorMethodVisitor;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationDescriptor;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationInstanceParser;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.signature.MethodSignature;
import io.michaelrocks.lightsaber.processor.signature.MethodSignatureParser;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

class InjectionMethodAnalyzer extends ProcessorMethodVisitor {
    private final InjectionTargetDescriptor.Builder injectionTargetBuilder;

    private final String methodName;
    private final String methodDesc;
    private final String signature;

    private boolean isInjectableMethod;
    private final Map<Integer, AnnotationDescriptor> parameterQualifiers = new HashMap<>();

    public InjectionMethodAnalyzer(final ProcessorContext processorContext,
            final InjectionTargetDescriptor.Builder injectionTargetBuilder,
            final String methodName, final String methodDesc, final String signature) {
        super(processorContext);
        this.injectionTargetBuilder = injectionTargetBuilder;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.signature = signature;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final Type annotationType = Type.getType(desc);
        if (Types.INJECT_TYPE.equals(annotationType)) {
            isInjectableMethod = true;
        }
        return null;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc,
            final boolean visible) {
        if (isInjectableMethod) {
            final Type annotationType = Type.getType(desc);
            if (getProcessorContext().isQualifier(annotationType)) {
                return new AnnotationInstanceParser(annotationType) {
                    @Override
                    public void visitEnd() {
                        final AnnotationDescriptor annotation =
                                getProcessorContext().getAnnotationRegistry().resolveAnnotation(toAnnotation());
                        if (parameterQualifiers.put(parameter, annotation) != null) {
                            reportError("Method parameter " + parameter + " has multiple qualifiers: "
                                    + injectionTargetBuilder.getTargetType() + "." + methodName + methodDesc);
                        }
                    }
                };
            }
        }
        return null;
    }

    @Override
    public void visitEnd() {
        if (isInjectableMethod) {
            final Type methodType = Type.getMethodType(methodDesc);
            final MethodSignature methodSignature =
                    MethodSignatureParser.parseMethodSignature(getProcessorContext(), signature, methodType);
            final MethodDescriptor methodDescriptor = new MethodDescriptor(methodName, methodSignature);
            if (MethodDescriptor.isConstructor(methodName)) {
                injectionTargetBuilder.addInjectableConstructor(methodDescriptor, parameterQualifiers);
            } else {
                injectionTargetBuilder.addInjectableMethod(methodDescriptor, parameterQualifiers);
            }
        }
    }
}
