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

import io.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator;
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

public class InjectionDispatcher extends ProcessorClassVisitor {
    private final AnnotationCreator annotationCreator;

    public InjectionDispatcher(final ClassVisitor classVisitor, final ProcessorContext processorContext,
            final AnnotationCreator annotationCreator) {
        super(processorContext, classVisitor);
        this.annotationCreator = annotationCreator;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        cv = new InjectionPatcher(getProcessorContext(), cv);
        final Type type = Type.getObjectType(name);

        final ModuleDescriptor module = getProcessorContext().findModuleByType(type);
        if (module != null) {
            cv = new ModulePatcher(getProcessorContext(), cv, annotationCreator, module);
        }

        final InjectionTargetDescriptor injectableTarget = getProcessorContext().findInjectableTargetByType(type);
        if (injectableTarget != null) {
            cv = new InjectableTargetPatcher(getProcessorContext(), cv, injectableTarget);
        }

        final InjectionTargetDescriptor providableTarget = getProcessorContext().findProvidableTargetByType(type);
        if (providableTarget != null) {
            cv = new ProvidableTargetPatcher(getProcessorContext(), cv, providableTarget);
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }
}
