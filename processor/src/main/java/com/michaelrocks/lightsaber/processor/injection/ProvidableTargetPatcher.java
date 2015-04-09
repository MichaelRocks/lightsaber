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

package com.michaelrocks.lightsaber.processor.injection;

import com.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;

public class ProvidableTargetPatcher extends ProcessorClassVisitor {
    private final InjectionTargetDescriptor providableTarget;

    public ProvidableTargetPatcher(final ProcessorContext processorContext, final ClassVisitor classVisitor,
            final InjectionTargetDescriptor providableTarget) {
        super(processorContext, classVisitor);
        this.providableTarget = providableTarget;
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final MethodDescriptor methodDescriptor = new MethodDescriptor(name, desc);
        if (providableTarget.getInjectableConstructor().equals(methodDescriptor)) {
            final int newAccess = access & ~ACC_PRIVATE;
            return super.visitMethod(newAccess, name, desc, signature, exceptions);
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
