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

import com.michaelrocks.lightsaber.Module;
import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.generation.ClassProducer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import java.util.Arrays;

public class RootVisitor extends ProducingClassVisitor {
    private final ProcessorContext processorContext;

    public RootVisitor(final ClassVisitor classVisitor, final ClassProducer classProducer,
            final ProcessorContext processorContext) {
        super(classVisitor, classProducer);

        this.processorContext = processorContext;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        if (interfaces != null && Arrays.asList(interfaces).indexOf(Type.getInternalName(Module.class)) >= 0) {
            // FIXME: This code must be removed when the injection package gets refactored.
            if (!processorContext.getGlobalModule().getModuleType().getInternalName().equals(name)) {
                cv = new ModuleVisitor(cv);
            }
        } else {
            // FIXME: This code must be removed when the injection package gets refactored.
            if (!processorContext.getInjectorFactoryType().getInternalName().equals(name)) {
                cv = new InjectionVisitor(cv, getClassProducer());
            }
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }
}
