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

package io.michaelrocks.lightsaber.processor.signature;

import io.michaelrocks.lightsaber.processor.ProcessorContext;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ASM5;

public class MethodSignatureParser extends SignatureVisitor {
    private final ProcessorContext processorContext;

    private final List<TypeSignatureParser> argumentTypeParsers = new ArrayList<>();
    private TypeSignatureParser returnTypeParser;
    private boolean isValid = true;

    private MethodSignature methodSignature;

    public MethodSignatureParser(final ProcessorContext processorContext) {
        super(ASM5);
        this.processorContext = processorContext;
    }

    public static MethodSignature parseMethodSignature(final ProcessorContext processorContext, final String signature,
            final Type methodType) {
        final MethodSignature methodSignature;
        if (signature == null) {
            methodSignature = null;
        } else {
            final SignatureReader signatureReader = new SignatureReader(signature);
            final MethodSignatureParser signatureParser = new MethodSignatureParser(processorContext);
            signatureReader.accept(signatureParser);
            methodSignature = signatureParser.getMethodSignature();
        }
        return methodSignature != null ? methodSignature : new MethodSignature(methodType);
    }

    public MethodSignature getMethodSignature() {
        if (isValid && methodSignature == null) {
            methodSignature = createMethodSignature();
            isValid = methodSignature != null;
        }
        return methodSignature;
    }

    private MethodSignature createMethodSignature() {
        if (returnTypeParser == null) {
            return null;
        }

        final TypeSignature returnType = returnTypeParser.getTypeSignature();
        if (returnType == null) {
            return null;
        }

        final List<TypeSignature> argumentTypes = new ArrayList<>(argumentTypeParsers.size());
        for (final TypeSignatureParser argumentTypeParser : argumentTypeParsers) {
            final TypeSignature argumentType = argumentTypeParser.getTypeSignature();
            if (argumentType == null) {
                return null;
            }
            argumentTypes.add(argumentType);
        }

        return new MethodSignature(returnType, argumentTypes);
    }

    @Override
    public SignatureVisitor visitParameterType() {
        final TypeSignatureParser argumentTypeParser = new TypeSignatureParser(processorContext);
        argumentTypeParsers.add(argumentTypeParser);
        return argumentTypeParser;
    }

    @Override
    public SignatureVisitor visitReturnType() {
        if (returnTypeParser != null) {
            reportError("Return type has already been parsed");
        }
        returnTypeParser = new TypeSignatureParser(processorContext);
        return returnTypeParser;
    }

    // Prohibited callbacks.

    @Override
    public void visitFormalTypeParameter(final String name) {
        reportError("Injectable methods cannot have type parameters");
    }

    private void reportError(final String message) {
        processorContext.reportError(message);
        isValid = false;
    }
}
