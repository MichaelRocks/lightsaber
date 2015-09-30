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

import io.michaelrocks.lightsaber.processor.ProcessorClassVisitor;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ModuleClassAnalyzer extends ProcessorClassVisitor {
    private ModuleDescriptor.Builder moduleBuilder;
    private boolean isModule;

    public ModuleClassAnalyzer(final ProcessorContext processorContext) {
        super(processorContext);
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        moduleBuilder = new ModuleDescriptor.Builder(Type.getObjectType(name));
        isModule = false;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        if (Type.getType(desc).equals(Types.MODULE_TYPE)) {
            isModule = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        return new ModuleMethodAnalyzer(getProcessorContext(), moduleBuilder, name, desc, signature);
    }

    @Override
    public FieldVisitor visitField(final int access, final String name, final String desc, final String signature,
            final Object value) {
        return new ModuleFieldAnalyzer(getProcessorContext(), moduleBuilder, name, desc);
    }

    @Override
    public void visitEnd() {
        if (isModule) {
            final ModuleDescriptor module = moduleBuilder.build();
            getProcessorContext().addModule(module);
        }
        super.visitEnd();
    }
}
