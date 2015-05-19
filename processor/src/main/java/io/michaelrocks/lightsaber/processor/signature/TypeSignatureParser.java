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

import static org.objectweb.asm.Opcodes.ASM5;

public class TypeSignatureParser extends SignatureVisitor {
    private final ProcessorContext processorContext;

    private Type classType;
    private Type classTypeParameter;
    private boolean isValid = true;

    private TypeSignature typeSignature;

    public TypeSignatureParser(final ProcessorContext processorContext) {
        super(ASM5);
        this.processorContext = processorContext;
    }

    public static TypeSignature parseTypeSignature(final ProcessorContext processorContext, final String signature,
            final Type fieldType) {
        final TypeSignature typeSignature;
        if (signature == null) {
            typeSignature = null;
        } else {
            final SignatureReader signatureReader = new SignatureReader(signature);
            final TypeSignatureParser signatureParser = new TypeSignatureParser(processorContext);
            signatureReader.acceptType(signatureParser);
            typeSignature = signatureParser.getTypeSignature();
        }
        return typeSignature != null ? typeSignature : TypeSignature.fromType(fieldType);
    }

    public TypeSignature getTypeSignature() {
        if (isValid && typeSignature == null) {
            typeSignature = new TypeSignature(classType, classTypeParameter);
        }
        return typeSignature;
    }

    @Override
    public void visitClassType(final String name) {
        visitType(Type.getObjectType(name));
    }

    @Override
    public void visitBaseType(final char descriptor) {
        visitType(Type.getType(Character.toString(descriptor)));
    }

    private void visitType(final Type type) {
        if (classType == null) {
            classType = type;
        } else if (classTypeParameter == null) {
            classTypeParameter = type;
        } else {
            reportError(classType + " has multiple type arguments");
        }
    }

    @Override
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        if (wildcard == INSTANCEOF) {
            return this;
        }

        reportError("Injectable type cannot have wildcards in its signature");
        return this;
    }

    // Prohibited callbacks.

    @Override
    public void visitInnerClassType(final String name) {
        reportError("Injectable type cannot have an inner class type in its signature");
    }

    @Override
    public SignatureVisitor visitArrayType() {
        reportError("Injectable type cannot be an array");
        return this;
    }

    @Override
    public void visitTypeVariable(final String name) {
        reportError("Injectable type cannot have type variables in its signature");
    }

    @Override
    public void visitTypeArgument() {
        reportError("Injectable type cannot have unbounded type arguments in its signature");
    }

    private void reportError(final String message) {
        processorContext.reportError(message);
        isValid = false;
    }
}
