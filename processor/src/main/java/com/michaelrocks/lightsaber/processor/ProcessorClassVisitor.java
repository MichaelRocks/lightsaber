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

package com.michaelrocks.lightsaber.processor;

import org.objectweb.asm.ClassVisitor;

import static org.objectweb.asm.Opcodes.ASM5;

public abstract class ProcessorClassVisitor extends ClassVisitor {
    private final ProcessorContext processorContext;

    public ProcessorClassVisitor(final ProcessorContext processorContext) {
        this(processorContext, null);
    }

    public ProcessorClassVisitor(final ProcessorContext processorContext, final ClassVisitor classVisitor) {
        super(ASM5, classVisitor);

        this.processorContext = processorContext;
    }

    public ProcessorContext getProcessorContext() {
        return processorContext;
    }

    public void reportError(final String errorMessage) {
        reportError(new ProcessingException(errorMessage));
    }

    public void reportError(final Exception error) {
        processorContext.reportError(error);
    }
}
