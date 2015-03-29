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
import com.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import com.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class GlobalModuleGenerator {
    private static final Type GLOBAL_MODULE_TYPE = Type.getObjectType("Lightsaber$$GlobalModule");

    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    private final ModuleDescriptor.Builder globalModuleBuilder = new ModuleDescriptor.Builder(GLOBAL_MODULE_TYPE);

    public GlobalModuleGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generateGlobalModule() {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classWriter.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                GLOBAL_MODULE_TYPE.getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(Module.class) });

        generateConstructor(classWriter);
        for (final InjectionTargetDescriptor providableTarget : processorContext.getProvidableTargets()) {
            final MethodDescriptor providerMethod = generateProviderMethod(classWriter, providableTarget);
            globalModuleBuilder.addProviderMethod(providerMethod);
        }

        classWriter.visitEnd();
        final byte[] classData = classWriter.toByteArray();
        classProducer.produceClass(GLOBAL_MODULE_TYPE.getInternalName(), classData);
    }

    public ModuleDescriptor getGlobalModuleDescriptor() {
        return globalModuleBuilder.build();
    }

    private void generateConstructor(final ClassWriter classWriter) {
        final MethodDescriptor defaultConstructor = MethodDescriptor.forConstructor();
        final MethodVisitor methodVisitor = classWriter.visitMethod(
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

    private MethodDescriptor generateProviderMethod(final ClassWriter classWriter,
            final InjectionTargetDescriptor providableTarget) {
        final Type providableTargetType = providableTarget.getTargetType();
        final MethodDescriptor providableTargetConstructor = providableTarget.getInjectableConstructors().get(0);

        final String providerMethodName = "provide" + providableTargetType.getInternalName().replace('/', '$');
        final Type[] providerMethodArgumentTypes = providableTargetConstructor.getType().getArgumentTypes();
        final MethodDescriptor providerMethod =
                MethodDescriptor.forMethod(providerMethodName, providableTargetType, providerMethodArgumentTypes);

        final MethodVisitor methodVisitor = classWriter.visitMethod(
                ACC_PUBLIC,
                providerMethod.getName(),
                providerMethod.getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitTypeInsn(NEW, providableTargetType.getInternalName());
        methodVisitor.visitInsn(DUP);
        for (int i = 1; i <= providerMethodArgumentTypes.length; ++i) {
            methodVisitor.visitVarInsn(ALOAD, i);
        }
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                providableTargetType.getInternalName(),
                providableTargetConstructor.getName(),
                providableTargetConstructor.getDescriptor(),
                false);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();

        return providerMethod;
    }
}
