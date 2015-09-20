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

import io.michaelrocks.lightsaber.Module;
import io.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.Type;

public class InjectionAnalysisDispatcher extends ProcessorClassVisitor {

    public InjectionAnalysisDispatcher(final ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        if (interfaces != null && ArrayUtils.indexOf(interfaces, Type.getInternalName(Module.class)) >= 0) {
            cv = new ModuleClassAnalyzer(getProcessorContext());
        } else {
            cv = new InjectionTargetAnalyzer(getProcessorContext());
        }

        super.visit(version, access, name, signature, superName, interfaces);
    }
}
