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

import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.internal.TypeAgent;
import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.InjectorDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedFieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedMethodDescriptor;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class TypeAgentClassGenerator {
    private static final String KEY_FIELD_NAME_PREFIX = "key";

    private static final MethodDescriptor KEY_CONSTRUCTOR =
            MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE);
    private static final MethodDescriptor GET_TYPE_METHOD =
            MethodDescriptor.forMethod("getType", Type.getType(Class.class));
    private static final MethodDescriptor INJECT_FIELDS_METHOD =
            MethodDescriptor.forMethod("injectFields",
                    Type.VOID_TYPE, Type.getType(Injector.class), Type.getType(Object.class));
    private static final MethodDescriptor INJECT_METHODS_METHOD =
            MethodDescriptor.forMethod("injectMethods",
                    Type.VOID_TYPE, Type.getType(Injector.class), Type.getType(Object.class));
    private static final MethodDescriptor GET_INSTANCE_METHOD =
            MethodDescriptor.forMethod("getInstance", Types.OBJECT_TYPE, Types.KEY_TYPE);
    private static final MethodDescriptor GET_PROVIDER_METHOD =
            MethodDescriptor.forMethod("getProvider", Types.PROVIDER_TYPE, Types.KEY_TYPE);

    private final ProcessorContext processorContext;
    private final AnnotationCreator annotationCreator;
    private final InjectorDescriptor injector;

    public TypeAgentClassGenerator(final ProcessorContext processorContext, final AnnotationCreator annotationCreator,
            final InjectorDescriptor injector) {
        this.processorContext = processorContext;
        this.annotationCreator = annotationCreator;
        this.injector = injector;
    }

    public byte[] generate() {
        final ClassWriter classWriter =
                new StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS, processorContext);
        final ClassVisitor classVisitor = new WatermarkClassVisitor(classWriter, true);
        classVisitor.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                injector.getInjectorType().getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { Type.getInternalName(TypeAgent.class) });

        generateKeyFields(classVisitor);
        generateStaticInitializer(classVisitor);
        generateConstructor(classVisitor);
        generateGetTypeMethod(classVisitor);
        generateInjectFieldsMethod(classVisitor);
        generateInjectMethodsMethod(classVisitor);

        classVisitor.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateKeyFields(final ClassVisitor classVisitor) {
        final Collection<QualifiedFieldDescriptor> injectableFields =
                injector.getInjectableTarget().getInjectableFields();
        for (int i = 0, fieldCount = injectableFields.size(); i < fieldCount; ++i) {
            final FieldVisitor fieldVisitor = classVisitor.visitField(
                    ACC_PRIVATE | ACC_STATIC | ACC_FINAL,
                    KEY_FIELD_NAME_PREFIX + i,
                    Types.KEY_TYPE.getDescriptor(),
                    null,
                    null);
            fieldVisitor.visitEnd();
        }

        final Collection<QualifiedMethodDescriptor> injectableMethods =
                injector.getInjectableTarget().getInjectableMethods();
        int i = 0;
        for (final QualifiedMethodDescriptor injectableMethod : injectableMethods) {
            for (int j = 0, count = injectableMethod.getArgumentTypes().size(); j < count; ++j) {
                final FieldVisitor fieldVisitor = classVisitor.visitField(
                        ACC_PRIVATE | ACC_STATIC | ACC_FINAL,
                        KEY_FIELD_NAME_PREFIX + i + '_' + j,
                        Types.KEY_TYPE.getDescriptor(),
                        null,
                        null);
                fieldVisitor.visitEnd();
            }
            i += 1;
        }
    }

    private void generateStaticInitializer(final ClassVisitor classVisitor) {
        final MethodDescriptor staticInitializer = MethodDescriptor.forStaticInitializer();
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_STATIC, staticInitializer);
        generator.visitCode();

        initializeFieldKeys(generator);
        initializeMethodKeys(generator);

        generator.returnValue();
        generator.endMethod();
    }

    private void initializeFieldKeys(final GeneratorAdapter generator) {
        int i = 0;
        for (final QualifiedFieldDescriptor injectableField : injector.getInjectableTarget().getInjectableFields()) {
            final Type dependencyType = getDependencyTypeForType(injectableField.getSignature());

            generator.newInstance(Types.KEY_TYPE);
            generator.dup();
            generator.push(dependencyType);
            if (injectableField.getQualifier() == null) {
                generator.pushNull();
            } else {
                annotationCreator.newAnnotation(generator, injectableField.getQualifier());
            }
            generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR);
            generator.putStatic(injector.getInjectorType(), KEY_FIELD_NAME_PREFIX + i, Types.KEY_TYPE);

            i += 1;
        }
    }

    private void initializeMethodKeys(final GeneratorAdapter generator) {
        int i = 0;
        for (final QualifiedMethodDescriptor injectableMethod : injector.getInjectableTarget().getInjectableMethods()) {
            final List<TypeSignature> argumentTypes = injectableMethod.getArgumentTypes();
            final List<AnnotationData> parameterQualifiers = injectableMethod.getParameterQualifiers();
            Validate.isTrue(argumentTypes.size() == parameterQualifiers.size());

            for (int j = 0, count = argumentTypes.size(); j < count; ++j) {
                final Type dependencyType = getDependencyTypeForType(argumentTypes.get(j));
                final AnnotationData parameterQualifier = parameterQualifiers.get(j);

                generator.newInstance(Types.KEY_TYPE);
                generator.dup();
                generator.push(dependencyType);
                if (parameterQualifier == null) {
                    generator.pushNull();
                } else {
                    annotationCreator.newAnnotation(generator, parameterQualifier);
                }
                generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR);
                generator.putStatic(injector.getInjectorType(), KEY_FIELD_NAME_PREFIX + i + '_' + j, Types.KEY_TYPE);
            }
            i += 1;
        }
    }

    private void generateConstructor(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator =
                new GeneratorAdapter(classVisitor, ACC_PUBLIC, MethodDescriptor.forConstructor());
        generator.visitCode();
        generator.loadThis();
        generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forConstructor());
        generator.returnValue();
        generator.endMethod();
    }

    private void generateGetTypeMethod(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, GET_TYPE_METHOD);
        generator.visitCode();
        generator.push(injector.getInjectableTarget().getTargetType());
        generator.returnValue();
        generator.endMethod();
    }

    private void generateInjectFieldsMethod(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, INJECT_FIELDS_METHOD);
        generator.visitCode();

        if (!injector.getInjectableTarget().getInjectableFields().isEmpty()) {
            generator.loadArg(1);
            generator.checkCast(injector.getInjectableTarget().getTargetType());
            final int injectableTargetLocal = generator.newLocal(injector.getInjectableTarget().getTargetType());
            generator.storeLocal(injectableTargetLocal);

            int fieldIndex = 0;
            for (final QualifiedFieldDescriptor injectableField : injector.getInjectableTarget()
                    .getInjectableFields()) {
                generateFieldInitializer(generator, injectableTargetLocal, injectableField, fieldIndex);
                fieldIndex += 1;
            }
        }

        generator.returnValue();
        generator.endMethod();
    }

    private void generateFieldInitializer(final GeneratorAdapter generator, final int injectableTargetLocal,
            final QualifiedFieldDescriptor qualifiedField, final int fieldIndex) {
        generator.loadLocal(injectableTargetLocal);
        generator.loadArg(0);

        generator.getStatic(injector.getInjectorType(), KEY_FIELD_NAME_PREFIX + fieldIndex, Types.KEY_TYPE);

        final MethodDescriptor method = getInjectorMethodForType(qualifiedField.getSignature());
        generator.invokeInterface(Types.INJECTOR_TYPE, method);
        GenerationHelper.convertDependencyToTargetType(generator, qualifiedField.getSignature());
        generator.putField(injector.getInjectableTarget().getTargetType(), qualifiedField.getField());
    }

    private void generateInjectMethodsMethod(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, INJECT_METHODS_METHOD);
        generator.visitCode();

        generator.loadArg(1);
        generator.checkCast(injector.getInjectableTarget().getTargetType());
        final int injectableTargetLocal = generator.newLocal(injector.getInjectableTarget().getTargetType());
        generator.storeLocal(injectableTargetLocal);

        int methodIndex = 0;
        for (final QualifiedMethodDescriptor injectableMethod : injector.getInjectableTarget().getInjectableMethods()) {
            generateMethodInvocation(generator, injectableTargetLocal, injectableMethod, methodIndex);
            methodIndex += 1;
        }

        generator.returnValue();
        generator.endMethod();
    }

    private void generateMethodInvocation(final GeneratorAdapter generator, final int injectableTargetLocal,
            final QualifiedMethodDescriptor qualifiedMethod, final int methodIndex) {
        generator.loadLocal(injectableTargetLocal);

        final List<TypeSignature> argumentTypes = qualifiedMethod.getArgumentTypes();
        for (int i = 0, count = argumentTypes.size(); i < count; i++) {
            final TypeSignature argumentType = argumentTypes.get(i);
            generator.loadArg(0);
            generator.getStatic(
                    injector.getInjectorType(), KEY_FIELD_NAME_PREFIX + methodIndex + '_' + i, Types.KEY_TYPE);
            final MethodDescriptor method = getInjectorMethodForType(argumentType);
            generator.invokeInterface(Types.INJECTOR_TYPE, method);
            GenerationHelper.convertDependencyToTargetType(generator, argumentType);
        }
        generator.invokeVirtual(injector.getInjectableTarget().getTargetType(), qualifiedMethod.getMethod());
    }

    private static Type getDependencyTypeForType(final TypeSignature type) {
        return type.isParameterized() ? type.getParameterType() : Types.box(type.getRawType());
    }

    private static MethodDescriptor getInjectorMethodForType(final TypeSignature type) {
        if (type.isParameterized()) {
            return GET_PROVIDER_METHOD;
        } else {
            return GET_INSTANCE_METHOD;
        }
    }
}
