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

package io.michaelrocks.lightsaber.processor.annotations.proxy;

import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationDescriptor;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.commons.GeneratorAdapter.*;

public class AnnotationProxyGenerator {
    private static final Type ARRAYS_TYPE = Type.getType(Arrays.class);
    private static final Type OBJECT_ARRAY_TYPE = Type.getType(Object[].class);
    private static final Type STRING_BUILDER_TYPE = Type.getType(StringBuilder.class);

    private static final MethodDescriptor HASH_CODE_METHOD = MethodDescriptor.forMethod("hashCode", Type.INT_TYPE);
    private static final MethodDescriptor EQUALS_METHOD =
            MethodDescriptor.forMethod("equals", Type.BOOLEAN_TYPE, Types.OBJECT_TYPE);
    private static final MethodDescriptor TO_STRING_METHOD =
            MethodDescriptor.forMethod("toString", Types.STRING_TYPE);
    private static final MethodDescriptor FLOAT_TO_INT_BITS_METHOD =
            MethodDescriptor.forMethod("floatToIntBits", Type.INT_TYPE, Type.FLOAT_TYPE);
    private static final MethodDescriptor DOUBLE_TO_LONG_BITS_METHOD =
            MethodDescriptor.forMethod("doubleToLongBits", Type.LONG_TYPE, Type.DOUBLE_TYPE);
    private static final MethodDescriptor ANNOTATION_TYPE_METHOD =
            MethodDescriptor.forMethod("annotationType", Type.getType(Class.class));

    private static final FieldDescriptor CACHED_HASH_CODE_FIELD =
            new FieldDescriptor("$cachedHashCode", Types.BOXED_INT_TYPE);
    private static final FieldDescriptor CACHED_TO_STRING_FIELD =
            new FieldDescriptor("$cachedToString", Types.STRING_TYPE);

    private static final MethodResolver stringBuilderAppendMethodResolver = new StringBuilderAppendMethodResolver();

    private final ProcessorContext processorContext;
    private final AnnotationDescriptor annotation;
    private final Type proxyType;

    public AnnotationProxyGenerator(final ProcessorContext processorContext, final AnnotationDescriptor annotation,
            final Type proxyType) {
        this.processorContext = processorContext;
        this.annotation = annotation;
        this.proxyType = proxyType;
    }

    public byte[] generate() {
        final ClassWriter classWriter =
                new StandaloneClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES, processorContext);
        final ClassVisitor classVisitor = new WatermarkClassVisitor(classWriter, true);
        classVisitor.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                proxyType.getInternalName(),
                null,
                Type.getInternalName(Object.class),
                new String[] { annotation.getType().getInternalName() });

        generateFields(classVisitor);
        generateConstructor(classVisitor);
        generateEqualsMethod(classVisitor);
        generateHashCodeMethod(classVisitor);
        generateToStringMethod(classVisitor);
        generateAnnotationTypeMethod(classVisitor);
        generateGetters(classVisitor);

        classVisitor.visitEnd();
        return classWriter.toByteArray();
    }

    private void generateFields(final ClassVisitor classVisitor) {
        for (final Map.Entry<String, Type> field : annotation.getFields().entrySet()) {
            final String name = field.getKey();
            final Type type = field.getValue();

            final FieldVisitor fieldVisitor = classVisitor.visitField(
                    ACC_PRIVATE | ACC_FINAL,
                    name,
                    type.getDescriptor(),
                    null,
                    null);
            fieldVisitor.visitEnd();
        }

        final FieldVisitor cachedHashCodeField = classVisitor.visitField(
                ACC_PRIVATE,
                CACHED_HASH_CODE_FIELD.getName(),
                CACHED_HASH_CODE_FIELD.getDescriptor(),
                null,
                null);
        cachedHashCodeField.visitEnd();

        final FieldVisitor cachedToStringField = classVisitor.visitField(
                ACC_PRIVATE,
                CACHED_TO_STRING_FIELD.getName(),
                CACHED_TO_STRING_FIELD.getDescriptor(),
                null,
                null);
        cachedToStringField.visitEnd();
    }

    private void generateConstructor(final ClassVisitor classVisitor) {
        final Collection<Type> fieldTypes = annotation.getFields().values();
        final Type[] argumentTypes = fieldTypes.toArray(new Type[fieldTypes.size()]);
        final MethodDescriptor method = MethodDescriptor.forConstructor(argumentTypes);
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, method);
        generator.visitCode();

        // Call the constructor of the super class.
        generator.loadThis();
        final MethodDescriptor constructor = MethodDescriptor.forDefaultConstructor();
        generator.invokeConstructor(Types.OBJECT_TYPE, constructor);

        // Initialize fields with arguments passed to the constructor.
        int localPosition = 0;
        for (final Map.Entry<String, Type> entry : annotation.getFields().entrySet()) {
            final String fieldName = entry.getKey();
            final Type fieldType = entry.getValue();
            generator.loadThis();
            generator.loadArg(localPosition++);
            generator.putField(proxyType, fieldName, fieldType);
        }

        generator.returnValue();
        generator.endMethod();
    }

    private void generateEqualsMethod(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, EQUALS_METHOD);
        generator.visitCode();

        // if (this == object) return true;
        generator.loadThis();
        generator.loadArg(0);
        final Label referencesNotEqualLabel = generator.newLabel();
        generator.ifCmp(Types.OBJECT_TYPE, NE, referencesNotEqualLabel);
        generator.push(true);
        generator.returnValue();

        generator.visitLabel(referencesNotEqualLabel);
        // if (!(object instanceof *AnnotationType*)) return false;
        generator.loadArg(0);
        generator.instanceOf(annotation.getType());
        final Label objectHasSameTypeLabel = generator.newLabel();
        generator.ifZCmp(NE, objectHasSameTypeLabel);
        generator.push(false);
        generator.returnValue();

        generator.visitLabel(objectHasSameTypeLabel);
        // Cast the object to *AnnotationType*.
        final int annotationLocal = generator.newLocal(annotation.getType());
        generator.loadArg(0);
        generator.checkCast(annotation.getType());
        generator.storeLocal(annotationLocal);

        final Label fieldsNotEqualLabel = generator.newLabel();
        for (final Map.Entry<String, Type> entry : annotation.getFields().entrySet()) {
            generateEqualsInvocationForField(
                    generator, entry.getKey(), entry.getValue(), annotationLocal, fieldsNotEqualLabel);
        }
        generator.push(true);

        final Label returnLabel = generator.newLabel();
        generator.goTo(returnLabel);

        generator.visitLabel(fieldsNotEqualLabel);
        generator.push(false);

        generator.visitLabel(returnLabel);
        generator.returnValue();
        generator.endMethod();
    }

    private void generateEqualsInvocationForField(final GeneratorAdapter generator, final String fieldName,
            final Type fieldType, final int annotationLocal, final Label fieldsNotEqualLabel) {
        final MethodDescriptor fieldAccessor = MethodDescriptor.forMethod(fieldName, fieldType);

        generator.loadThis();
        generator.invokeVirtual(proxyType, fieldAccessor);
        convertFieldValue(generator, fieldType);

        generator.loadLocal(annotationLocal);
        generator.invokeInterface(annotation.getType(), fieldAccessor);
        convertFieldValue(generator, fieldType);

        if (fieldType.getSort() == Type.ARRAY) {
            // Call Arrays.equals() with a corresponding signature.
            final Type elementType = fieldType.getElementType();
            final Type argumentType = Types.isPrimitive(elementType) ? fieldType : OBJECT_ARRAY_TYPE;
            final MethodDescriptor equalsMethod =
                    MethodDescriptor.forMethod(EQUALS_METHOD.getName(), Type.BOOLEAN_TYPE, argumentType, argumentType);
            generator.invokeStatic(ARRAYS_TYPE, equalsMethod);
            generator.ifZCmp(EQ, fieldsNotEqualLabel);
        } else if (Types.isPrimitive(fieldType)) {
            switch (fieldType.getSort()) {
                case Type.DOUBLE:
                case Type.LONG:
                    generator.ifCmp(Type.LONG_TYPE, NE, fieldsNotEqualLabel);
                    break;
                default:
                    generator.ifICmp(NE, fieldsNotEqualLabel);
                    break;
            }
        } else {
            // Call equals() on the instances on the stack.
            generator.invokeVirtual(Types.OBJECT_TYPE, EQUALS_METHOD);
            generator.ifZCmp(EQ, fieldsNotEqualLabel);
        }
    }

    private void convertFieldValue(final GeneratorAdapter generator, final Type fieldType) {
        switch (fieldType.getSort()) {
            case Type.FLOAT:
                generator.invokeStatic(Types.BOXED_FLOAT_TYPE, FLOAT_TO_INT_BITS_METHOD);
                break;
            case Type.DOUBLE:
                generator.invokeStatic(Types.BOXED_DOUBLE_TYPE, DOUBLE_TO_LONG_BITS_METHOD);
                break;
        }
    }

    private void generateHashCodeMethod(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, HASH_CODE_METHOD);
        generator.visitCode();

        final Label cacheHashCodeIsNullLabel = generator.newLabel();
        generator.loadThis();
        generator.getField(proxyType, CACHED_HASH_CODE_FIELD);
        generator.dup();
        generator.ifNull(cacheHashCodeIsNullLabel);
        generator.unbox(Type.INT_TYPE);
        generator.returnValue();

        generator.visitLabel(cacheHashCodeIsNullLabel);
        generator.push(0);

        for (final Map.Entry<String, Type> entry : annotation.getFields().entrySet()) {
            // Hash code for annotation is the sum of 127 * fieldName.hashCode() ^ fieldValue.hashCode().
            generateHashCodeComputationForField(generator, entry.getKey(), entry.getValue());
            generator.math(ADD, Type.INT_TYPE);
        }

        generator.dup();
        generator.valueOf(Type.INT_TYPE);
        generator.loadThis();
        generator.swap(Type.INT_TYPE, proxyType);
        generator.putField(proxyType, CACHED_HASH_CODE_FIELD);

        generator.returnValue();
        generator.endMethod();
    }

    private void generateHashCodeComputationForField(final GeneratorAdapter generator, final String fieldName,
            final Type fieldType) {
        // Compute hash code of the field name.
        generator.push(fieldName);
        generator.invokeVirtual(Types.STRING_TYPE, HASH_CODE_METHOD);
        // Multiple it by 127.
        generator.push(127);
        generator.math(MUL, Type.INT_TYPE);

        // Load field value on the stack.
        generator.loadThis();
        final MethodDescriptor fieldAccessor = MethodDescriptor.forMethod(fieldName, fieldType);
        generator.invokeVirtual(proxyType, fieldAccessor);

        if (fieldType.getSort() == Type.ARRAY) {
            // Call Arrays.hashCode() with a corresponding signature.
            final Type elementType = fieldType.getElementType();
            final Type argumentType = Types.isPrimitive(elementType) ? fieldType : OBJECT_ARRAY_TYPE;
            final MethodDescriptor hashCodeMethod =
                    MethodDescriptor.forMethod(HASH_CODE_METHOD.getName(), Type.INT_TYPE, argumentType);
            generator.invokeStatic(ARRAYS_TYPE, hashCodeMethod);
        } else {
            // If the field has primitive type then box it.
            generator.valueOf(fieldType);
            // Call hashCode() on the instance on the stack.
            generator.invokeVirtual(Types.OBJECT_TYPE, HASH_CODE_METHOD);
        }

        // Xor the field name and the field value hash codes.
        generator.math(XOR, Type.INT_TYPE);
    }

    private void generateToStringMethod(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, TO_STRING_METHOD);
        generator.visitCode();

        final Label cachedToStringIsNullLabel = generator.newLabel();
        generator.loadThis();
        generator.getField(proxyType, CACHED_TO_STRING_FIELD);
        generator.dup();
        generator.ifNull(cachedToStringIsNullLabel);
        generator.returnValue();

        generator.visitLabel(cachedToStringIsNullLabel);
        generator.newInstance(STRING_BUILDER_TYPE);
        generator.dup();
        generator.invokeConstructor(STRING_BUILDER_TYPE, MethodDescriptor.forDefaultConstructor());

        generator.push("@" + annotation.getType().getClassName() + "(");
        generateStringBuilderAppendInvocation(generator, Types.STRING_TYPE);

        boolean addComma = false;
        for (final Map.Entry<String, Type> entry : annotation.getFields().entrySet()) {
            final String fieldName = entry.getKey();
            final Type fieldType = entry.getValue();
            appendFieldName(generator, fieldName, addComma);
            appendFieldValue(generator, fieldName, fieldType);
            addComma = true;
        }

        generator.push(')');
        generateStringBuilderAppendInvocation(generator, Type.CHAR_TYPE);
        generator.invokeVirtual(STRING_BUILDER_TYPE, TO_STRING_METHOD);

        generator.dup();
        generator.loadThis();
        generator.swap();
        generator.putField(proxyType, CACHED_TO_STRING_FIELD);

        generator.returnValue();
        generator.endMethod();
    }

    private void appendFieldName(final GeneratorAdapter generator, final String fieldName, final boolean addComma) {
        final String prefix = addComma ? ", " : "";
        generator.push(prefix + fieldName + '=');
        generateStringBuilderAppendInvocation(generator, Types.STRING_TYPE);
    }

    private void appendFieldValue(final GeneratorAdapter generator, final String fieldName, final Type fieldType) {
        generator.loadThis();
        generator.getField(proxyType, fieldName, fieldType);
        if (fieldType.getSort() == Type.ARRAY) {
            generateArraysToStringInvocation(generator, fieldType);
            generateStringBuilderAppendInvocation(generator, Types.STRING_TYPE);
        } else {
            generateStringBuilderAppendInvocation(generator, fieldType);
        }
    }

    private void generateArraysToStringInvocation(final GeneratorAdapter generator, final Type fieldType) {
        final Type elementType = fieldType.getElementType();
        final Type argumentType = Types.isPrimitive(elementType) ? fieldType : OBJECT_ARRAY_TYPE;
        final MethodDescriptor toStringMethod =
                MethodDescriptor.forMethod(TO_STRING_METHOD.getName(), Types.STRING_TYPE, argumentType);
        generator.invokeStatic(ARRAYS_TYPE, toStringMethod);
    }

    private void generateStringBuilderAppendInvocation(final GeneratorAdapter generator, final Type parameterType) {
        final MethodDescriptor appendMethod = stringBuilderAppendMethodResolver.resolveMethod(parameterType);
        generator.invokeVirtual(STRING_BUILDER_TYPE, appendMethod);
    }

    private void generateAnnotationTypeMethod(final ClassVisitor classVisitor) {
        final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, ANNOTATION_TYPE_METHOD);
        generator.visitCode();
        generator.push(annotation.getType());
        generator.returnValue();
        generator.endMethod();
    }

    private void generateGetters(final ClassVisitor classVisitor) {
        for (final Map.Entry<String, Type> field : annotation.getFields().entrySet()) {
            final String name = field.getKey();
            final Type type = field.getValue();

            final MethodDescriptor method = MethodDescriptor.forMethod(name, type);
            final GeneratorAdapter generator = new GeneratorAdapter(classVisitor, ACC_PUBLIC, method);
            generator.visitCode();
            generator.loadThis();
            generator.getField(proxyType, name, type);
            generator.returnValue();
            generator.endMethod();
        }
    }

    private interface MethodResolver {
        MethodDescriptor resolveMethod(Type type);
    }

    private static final class StringBuilderAppendMethodResolver implements MethodResolver {
        private static final String METHOD_NAME = "append";
        private static final MethodDescriptor BOOLEAN_METHOD = appendMethod(Type.BOOLEAN_TYPE);
        private static final MethodDescriptor CHAR_METHOD = appendMethod(Type.CHAR_TYPE);
        private static final MethodDescriptor FLOAT_METHOD = appendMethod(Type.FLOAT_TYPE);
        private static final MethodDescriptor DOUBLE_METHOD = appendMethod(Type.DOUBLE_TYPE);
        private static final MethodDescriptor INT_METHOD = appendMethod(Type.INT_TYPE);
        private static final MethodDescriptor LONG_METHOD = appendMethod(Type.LONG_TYPE);
        private static final MethodDescriptor OBJECT_METHOD = appendMethod(Types.OBJECT_TYPE);
        private static final MethodDescriptor STRING_METHOD = appendMethod(Types.STRING_TYPE);

        private static MethodDescriptor appendMethod(final Type parameterType) {
            return MethodDescriptor.forMethod(METHOD_NAME, STRING_BUILDER_TYPE, parameterType);
        }

        @Override
        public MethodDescriptor resolveMethod(final Type type) {
            switch (type.getSort()) {
                case Type.BOOLEAN:
                    return BOOLEAN_METHOD;
                case Type.CHAR:
                    return CHAR_METHOD;
                case Type.FLOAT:
                    return FLOAT_METHOD;
                case Type.DOUBLE:
                    return DOUBLE_METHOD;
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                    return INT_METHOD;
                case Type.LONG:
                    return LONG_METHOD;
                default:
                    return Types.STRING_TYPE.equals(type) ? STRING_METHOD : OBJECT_METHOD;
            }
        }
    }
}

