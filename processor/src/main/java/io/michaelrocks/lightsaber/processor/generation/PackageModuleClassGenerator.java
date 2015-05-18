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

package io.michaelrocks.lightsaber.processor.generation;

import io.michaelrocks.lightsaber.Module;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.injection.ModulePatcher;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.V1_6;

public class PackageModuleClassGenerator {
    private final ClassProducer classProducer;
    private final ProcessorContext processorContext;

    public PackageModuleClassGenerator(final ClassProducer classProducer, final ProcessorContext processorContext) {
        this.classProducer = classProducer;
        this.processorContext = processorContext;
    }

    public void generatePackageModule(final ModuleDescriptor packageModule) {
        final ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        final ModulePatcher modulePatcher = new ModulePatcher(processorContext, classWriter, packageModule);

        generateClass(modulePatcher, packageModule.getModuleType().getInternalName());

        final byte[] classData = classWriter.toByteArray();
        classProducer.produceClass(packageModule.getModuleType().getInternalName(), classData);
    }

    private void generateClass(final ClassVisitor classVisitor, final String className) {
        classVisitor.visit(V1_6, ACC_PUBLIC, className, null, Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(Module.class) });
        generateConstructor(classVisitor);
        classVisitor.visitEnd();
    }

    private void generateConstructor(final ClassVisitor classVisitor) {
        final MethodDescriptor constructor = MethodDescriptor.forDefaultConstructor();
        final Method method = new Method(constructor.getName(), constructor.getDescriptor());
        final GeneratorAdapter methodGenerator = new GeneratorAdapter(ACC_PUBLIC, method, null, null, classVisitor);
        methodGenerator.loadThis();
        methodGenerator.invokeConstructor(Type.getType(Object.class), method);
        methodGenerator.returnValue();
        methodGenerator.endMethod();
    }
}
