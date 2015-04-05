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
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

public class InjectionDispatcher extends ProcessorClassVisitor {
    public InjectionDispatcher(final ClassVisitor classVisitor, final ProcessorContext processorContext) {
        super(processorContext, classVisitor);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        final ModuleDescriptor module = getProcessorContext().findModuleByType(Type.getObjectType(name));
        if (module != null) {
            cv = new ModulePatcher(getProcessorContext(), cv);
        } else {
            cv = new InjectionPatcher(getProcessorContext(), cv);
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }
}
