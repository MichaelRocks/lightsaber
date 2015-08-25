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
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationInstanceParser;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedMethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ScopeDescriptor;
import io.michaelrocks.lightsaber.processor.signature.MethodSignature;
import io.michaelrocks.lightsaber.processor.signature.MethodSignatureParser;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

class ModuleMethodAnalyzer extends ProcessorMethodVisitor {
    private final ModuleDescriptor.Builder moduleBuilder;

    private final String methodName;
    private final String methodDesc;
    private final String signature;

    private boolean isProviderMethod;
    private AnnotationData resultQualifier;
    private final Map<Integer, AnnotationData> parameterQualifiers = new HashMap<>();
    private ScopeDescriptor scope;

    public ModuleMethodAnalyzer(final ProcessorContext processorContext, final ModuleDescriptor.Builder moduleBuilder,
            final String methodName, final String methodDesc, final String signature) {
        super(processorContext);
        this.moduleBuilder = moduleBuilder;
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.signature = signature;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final Type annotationType = Type.getType(desc);
        if (Types.PROVIDES_TYPE.equals(annotationType)) {
            isProviderMethod = true;
        } else if (getProcessorContext().isQualifier(annotationType)) {
            if (resultQualifier == null) {
                return new AnnotationInstanceParser(annotationType) {
                    @Override
                    public void visitEnd() {
                        resultQualifier = getProcessorContext().getAnnotationRegistry().resolveAnnotation(toAnnotation());
                    }
                };
            } else {
                reportError("Method has multiple qualifier annotations: "
                        + moduleBuilder.getModuleType() + "." + methodName + methodDesc);
            }
        } else if (scope == null) {
            scope = getProcessorContext().findScopeByAnnotationType(annotationType);
        } else if (getProcessorContext().findScopeByAnnotationType(annotationType) != null) {
            reportError("Method has multiple scope annotations: " + moduleBuilder.getModuleType()
                    + "." + methodName + methodDesc);
        }

        return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc,
            final boolean visible) {
        if (isProviderMethod) {
            final Type annotationType = Type.getType(desc);
            if (getProcessorContext().isQualifier(annotationType)) {
                return new AnnotationInstanceParser(annotationType) {
                    @Override
                    public void visitEnd() {
                        final AnnotationData annotation =
                                getProcessorContext().getAnnotationRegistry().resolveAnnotation(toAnnotation());
                        if (parameterQualifiers.put(parameter, annotation) != null) {
                            reportError("Method parameter " + parameter + " has multiple qualifiers: "
                                    + moduleBuilder.getModuleType() + "." + methodName + methodDesc);
                        }
                    }
                };
            }
        }
        return null;
    }

    @Override
    public void visitEnd() {
        if (isProviderMethod) {
            final Type methodType = Type.getMethodType(methodDesc);
            final MethodSignature methodSignature =
                    MethodSignatureParser.parseMethodSignature(getProcessorContext(), signature, methodType);
            final MethodDescriptor providerMethod = new MethodDescriptor(methodName, methodSignature);
            final QualifiedMethodDescriptor qualifiedProviderMethod =
                    QualifiedMethodDescriptor.from(providerMethod, parameterQualifiers, resultQualifier);
            moduleBuilder.addProviderMethod(qualifiedProviderMethod, scope);
        }
        super.visitEnd();
    }
}
