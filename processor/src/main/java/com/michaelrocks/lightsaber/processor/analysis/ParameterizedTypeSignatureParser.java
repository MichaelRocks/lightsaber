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

package com.michaelrocks.lightsaber.processor.analysis;

import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.descriptors.ParameterizedType;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;

import static org.objectweb.asm.Opcodes.ASM5;

class ParameterizedTypeSignatureParser extends SignatureVisitor {
    private final ProcessorContext processorContext;

    private Type classType;
    private Type classTypeParameter;
    private boolean isValid = true;

    public ParameterizedTypeSignatureParser(final ProcessorContext processorContext) {
        super(ASM5);
        this.processorContext = processorContext;
    }

    public ParameterizedType getParameterizedType() {
        return isValid ? new ParameterizedType(classType, classTypeParameter) : null;
    }

    @Override
    public void visitClassType(final String name) {
        if (classType == null) {
            classType = Type.getObjectType(name);
        } else if (classTypeParameter == null) {
            classTypeParameter = Type.getObjectType(name);
        } else {
            reportError(classType + " has multiple type arguments");
        }
    }

    @Override
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        if (wildcard == INSTANCEOF) {
            return this;
        }

        reportError("Injectable field cannot have wildcards in its signature");
        return this;
    }

    // Prohibited callbacks.

    @Override
    public void visitInnerClassType(final String name) {
        reportError("Injectable field cannot have an inner class type in its signature");
    }

    @Override
    public void visitBaseType(final char descriptor) {
        reportError("Injectable field cannot be a primitive type");
    }

    @Override
    public SignatureVisitor visitArrayType() {
        reportError("Injectable field cannot be an array");
        return this;
    }

    @Override
    public void visitTypeVariable(final String name) {
        reportError("Injectable field cannot have type variables in its signature");
    }

    @Override
    public void visitTypeArgument() {
        reportError("Injectable field cannot have unbounded type arguments in its signature");
    }

    private void reportError(final String message) {
        processorContext.reportError(message);
        isValid = false;
    }
}
