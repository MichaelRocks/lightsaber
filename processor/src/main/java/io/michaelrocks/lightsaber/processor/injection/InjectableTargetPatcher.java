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

package io.michaelrocks.lightsaber.processor.injection;

import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class InjectableTargetPatcher extends BaseInjectionClassVisitor {
    private final InjectionTargetDescriptor injectableTarget;

    public InjectableTargetPatcher(final ProcessorContext processorContext, final ClassVisitor classVisitor,
            final InjectionTargetDescriptor injectableTarget) {
        super(processorContext, classVisitor);
        this.injectableTarget = injectableTarget;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        final int newAccess = (access & ~(ACC_PRIVATE | ACC_PROTECTED)) | ACC_PUBLIC;
        super.visit(version, newAccess, name, signature, superName, interfaces);
        if (newAccess != access) {
            setDirty(true);
        }
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
            final Object value) {
        if (injectableTarget.isInjectableField(name)) {
            final int newAccess = access & ~(ACC_PRIVATE | ACC_FINAL);
            if (newAccess != access) {
                setDirty(true);
            }
            return super.visitField(newAccess, name, desc, signature, value);
        } else {
            return super.visitField(access, name, desc, signature, value);
        }
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        final MethodDescriptor methodDescriptor = new MethodDescriptor(name, desc);
        if (injectableTarget.isInjectableMethod(methodDescriptor)) {
            final int newAccess = access & ~(ACC_PRIVATE | ACC_FINAL);
            if (newAccess != access) {
                setDirty(true);
            }
            return super.visitMethod(newAccess, name, desc, signature, exceptions);
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions);
        }
    }
}
