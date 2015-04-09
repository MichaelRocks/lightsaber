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

package com.michaelrocks.lightsaber.processor.generation;

import com.michaelrocks.lightsaber.Module;
import com.michaelrocks.lightsaber.processor.ProcessorContext;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import com.michaelrocks.lightsaber.processor.injection.ModulePatcher;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class GlobalModuleGenerator {
    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    public GlobalModuleGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generateGlobalModule() {
        final ModuleDescriptor globalModule = processorContext.getGlobalModule();
        final Type globalModuleType = globalModule.getModuleType();

        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        final ModulePatcher classVisitor = new ModulePatcher(processorContext, classWriter, globalModule);
        classVisitor.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                globalModuleType.getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(Module.class) });

        generateConstructor(classVisitor);

        classVisitor.visitEnd();
        final byte[] classData = classWriter.toByteArray();
        classProducer.produceClass(globalModuleType.getInternalName(), classData);
    }

    private void generateConstructor(final ClassVisitor classVisitor) {
        final MethodDescriptor defaultConstructor = MethodDescriptor.forConstructor();
        final MethodVisitor methodVisitor = classVisitor.visitMethod(
                0,
                defaultConstructor.getName(),
                defaultConstructor.getType().getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                Type.getInternalName(Object.class),
                defaultConstructor.getName(),
                defaultConstructor.getType().getDescriptor(),
                false);
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }
}
