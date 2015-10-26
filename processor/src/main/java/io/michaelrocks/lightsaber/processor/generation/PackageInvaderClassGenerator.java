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

import io.michaelrocks.lightsaber.LightsaberTypes;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.PackageInvaderDescriptor;
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;

import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class PackageInvaderClassGenerator {
    private final ProcessorContext processorContext;
    private final PackageInvaderDescriptor packageInvader;

    public PackageInvaderClassGenerator(final ProcessorContext processorContext,
            final PackageInvaderDescriptor packageInvader) {
        this.processorContext = processorContext;
        this.packageInvader = packageInvader;
    }

    public byte[] generate() {
        final ClassWriter classWriter =
                new StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, processorContext);
        final ClassVisitor classVisitor = new WatermarkClassVisitor(classWriter, true);
        classVisitor.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                packageInvader.getType().getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { LightsaberTypes.INJECTOR_CONFIGURATOR_TYPE.getInternalName() });

        generateFields(classVisitor);
        generateStaticInitializer(classVisitor);
        generateConstructor(classVisitor);

        classVisitor.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateFields(final ClassVisitor classVisitor) {
        for (final FieldDescriptor field : packageInvader.getClassFields().values()) {
            final FieldVisitor fieldVisitor =
                    classVisitor.visitField(
                            ACC_PUBLIC | ACC_STATIC | ACC_FINAL,
                            field.getName(),
                            field.getDescriptor(),
                            null,
                            null);
            fieldVisitor.visitEnd();
        }
    }

    private void generateConstructor(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator =
                new GeneratorAdapter(classVisitor, ACC_PUBLIC, MethodDescriptor.forDefaultConstructor());
        generator.visitCode();
        generator.loadThis();
        generator.dup();
        generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forDefaultConstructor());
        generator.returnValue();
        generator.endMethod();
    }

    private void generateStaticInitializer(final ClassVisitor classVisitor) {
        final MethodDescriptor staticInitializer = MethodDescriptor.forStaticInitializer();
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_STATIC, staticInitializer);
        generator.visitCode();

        for (final Map.Entry<Type, FieldDescriptor> entry : packageInvader.getClassFields().entrySet()) {
            generator.push(entry.getKey());
            generator.putStatic(packageInvader.getType(), entry.getValue());
        }

        generator.returnValue();
        generator.endMethod();
    }
}
