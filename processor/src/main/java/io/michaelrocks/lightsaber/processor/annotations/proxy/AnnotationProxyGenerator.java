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
import io.michaelrocks.lightsaber.processor.commons.Boxer;
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter;
import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

public class AnnotationProxyGenerator {
    private static final String OBJECT_TYPE_NAME = Type.getInternalName(Object.class);
    private static final String STRING_TYPE_NAME = Type.getInternalName(String.class);
    private static final String ARRAYS_TYPE_NAME = Type.getInternalName(Arrays.class);
    private static final String FLOAT_TYPE_NAME = Type.getInternalName(Float.class);
    private static final String DOUBLE_TYPE_NAME = Type.getInternalName(Double.class);
    private static final String STRING_BUILDER_TYPE_NAME = Type.getInternalName(StringBuilder.class);

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

    private final String annotationTypeName;
    private final String proxyTypeName;

    public AnnotationProxyGenerator(final ProcessorContext processorContext, final AnnotationDescriptor annotation,
            final Type proxyType) {
        this.processorContext = processorContext;
        this.annotation = annotation;

        this.annotationTypeName = annotation.getType().getInternalName();
        this.proxyTypeName = proxyType.getInternalName();
    }

    public byte[] generate() {
        final ClassWriter classWriter =
                new StandaloneClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES, processorContext);
        classWriter.visit(
                V1_6,
                ACC_PUBLIC | ACC_SUPER,
                proxyTypeName,
                null,
                Type.getInternalName(Object.class),
                new String[] { annotationTypeName });

        generateFields(classWriter);
        generateConstructor(classWriter);
        generateEqualsMethod(classWriter);
        generateHashCodeMethod(classWriter);
        generateToStringMethod(classWriter);
        generateAnnotationTypeMethod(classWriter);
        generateGetters(classWriter);

        classWriter.visitEnd();
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
        final MethodVisitor methodVisitor = classVisitor.visitMethod(
                ACC_PUBLIC,
                method.getName(),
                method.getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();

        // Call the constructor of the super class.
        methodVisitor.visitVarInsn(ALOAD, 0);
        final MethodDescriptor constructor = MethodDescriptor.forDefaultConstructor();
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                OBJECT_TYPE_NAME,
                constructor.getName(),
                constructor.getDescriptor(),
                false);

        // Initialize fields with arguments passed to the constructor.
        int localPosition = 1;
        for (final Map.Entry<String, Type> entry : annotation.getFields().entrySet()) {
            final String fieldName = entry.getKey();
            final Type fieldType = entry.getValue();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(fieldType.getOpcode(ILOAD), localPosition);
            methodVisitor.visitFieldInsn(PUTFIELD, proxyTypeName, fieldName, fieldType.getDescriptor());
            localPosition += fieldType.getSize();
        }

        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateEqualsMethod(final ClassVisitor classVisitor) {
        final MethodVisitor methodVisitor;
        methodVisitor = classVisitor.visitMethod(
                ACC_PUBLIC,
                EQUALS_METHOD.getName(),
                EQUALS_METHOD.getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();

        // if (this == object) return true;
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitVarInsn(ALOAD, 1);
        final Label referencesNotEqualLabel = new Label();
        methodVisitor.visitJumpInsn(IF_ACMPNE, referencesNotEqualLabel);
        methodVisitor.visitInsn(ICONST_1);
        methodVisitor.visitInsn(IRETURN);

        methodVisitor.visitLabel(referencesNotEqualLabel);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

        // if (!(object instanceof *AnnotationType*)) return false;
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitTypeInsn(INSTANCEOF, annotationTypeName);
        final Label objectHasSameTypeLabel = new Label();
        methodVisitor.visitJumpInsn(IFNE, objectHasSameTypeLabel);
        methodVisitor.visitInsn(ICONST_0);
        methodVisitor.visitInsn(IRETURN);

        methodVisitor.visitLabel(objectHasSameTypeLabel);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

        // Cast the object to *AnnotationType*.
        methodVisitor.visitVarInsn(ALOAD, 1);
        methodVisitor.visitTypeInsn(CHECKCAST, annotationTypeName);
        methodVisitor.visitVarInsn(ASTORE, 2);

        final Label fieldsNotEqualLabel = new Label();
        for (final Map.Entry<String, Type> entry : annotation.getFields().entrySet()) {
            generateEqualsInvocationForField(methodVisitor, entry.getKey(), entry.getValue(), fieldsNotEqualLabel);
        }
        methodVisitor.visitInsn(ICONST_1);

        final Label returnLabel = new Label();
        methodVisitor.visitJumpInsn(GOTO, returnLabel);

        methodVisitor.visitLabel(fieldsNotEqualLabel);
        methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[] { annotationTypeName }, 0, null);

        methodVisitor.visitInsn(ICONST_0);

        methodVisitor.visitLabel(returnLabel);
        methodVisitor.visitFrame(Opcodes.F_SAME1, 0, null, 1, new Object[] { Opcodes.INTEGER });

        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateEqualsInvocationForField(final MethodVisitor methodVisitor, final String fieldName,
            final Type fieldType, final Label fieldsNotEqualLabel) {
        final MethodDescriptor fieldAccessor = MethodDescriptor.forMethod(fieldName, fieldType);

        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                proxyTypeName,
                fieldAccessor.getName(),
                fieldAccessor.getDescriptor(),
                false);
        convertFieldValue(methodVisitor, fieldType);

        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitMethodInsn(
                INVOKEINTERFACE,
                annotationTypeName,
                fieldAccessor.getName(),
                fieldAccessor.getDescriptor(),
                true);
        convertFieldValue(methodVisitor, fieldType);

        if (fieldType.getSort() == Type.ARRAY) {
            // Call Arrays.equals() with a corresponding signature.
            final Type elementType = fieldType.getElementType();
            final Type argumentType = Types.isPrimitive(elementType) ? fieldType : OBJECT_ARRAY_TYPE;
            final MethodDescriptor equalsMethod =
                    MethodDescriptor.forMethod(EQUALS_METHOD.getName(), Type.BOOLEAN_TYPE, argumentType, argumentType);
            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    ARRAYS_TYPE_NAME,
                    equalsMethod.getName(),
                    equalsMethod.getDescriptor(),
                    false);
            methodVisitor.visitJumpInsn(IFEQ, fieldsNotEqualLabel);
        } else if (Types.isPrimitive(fieldType)) {
            switch (fieldType.getSort()) {
                case Type.DOUBLE:
                case Type.LONG:
                    methodVisitor.visitInsn(LCMP);
                    methodVisitor.visitJumpInsn(IFNE, fieldsNotEqualLabel);
                    break;
                default:
                    methodVisitor.visitJumpInsn(IF_ICMPNE, fieldsNotEqualLabel);
                    break;
            }
        } else {
            // Call equals() on the instances on the stack.
            methodVisitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    OBJECT_TYPE_NAME,
                    EQUALS_METHOD.getName(),
                    EQUALS_METHOD.getDescriptor(),
                    false);
            methodVisitor.visitJumpInsn(IFEQ, fieldsNotEqualLabel);
        }
    }

    private void convertFieldValue(final MethodVisitor methodVisitor, final Type fieldType) {
        switch (fieldType.getSort()) {
            case Type.FLOAT:
                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        FLOAT_TYPE_NAME,
                        FLOAT_TO_INT_BITS_METHOD.getName(),
                        FLOAT_TO_INT_BITS_METHOD.getDescriptor(),
                        false);
                break;
            case Type.DOUBLE:
                methodVisitor.visitMethodInsn(
                        INVOKESTATIC,
                        DOUBLE_TYPE_NAME,
                        DOUBLE_TO_LONG_BITS_METHOD.getName(),
                        DOUBLE_TO_LONG_BITS_METHOD.getDescriptor(),
                        false);
                break;
        }
    }

    private void generateHashCodeMethod(final ClassVisitor classVisitor) {
        final MethodVisitor methodVisitor;
        methodVisitor = classVisitor.visitMethod(
                ACC_PUBLIC,
                HASH_CODE_METHOD.getName(),
                HASH_CODE_METHOD.getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();

        final Label cacheHashCodeIsNullLabel = new Label();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(
                GETFIELD,
                proxyTypeName,
                CACHED_HASH_CODE_FIELD.getName(),
                CACHED_HASH_CODE_FIELD.getDescriptor());
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitJumpInsn(IFNULL, cacheHashCodeIsNullLabel);
        Boxer.unbox(methodVisitor, Types.BOXED_INT_TYPE);
        methodVisitor.visitInsn(IRETURN);

        methodVisitor.visitLabel(cacheHashCodeIsNullLabel);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

        methodVisitor.visitInsn(ICONST_0);

        for (final Map.Entry<String, Type> entry : annotation.getFields().entrySet()) {
            // Hash code for annotation is the sum of 127 * fieldName.hashCode() ^ fieldValue.hashCode().
            generateHashCodeComputationForField(methodVisitor, entry.getKey(), entry.getValue());
            methodVisitor.visitInsn(IADD);
        }

        methodVisitor.visitInsn(DUP);
        Boxer.box(methodVisitor, Type.INT_TYPE);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(SWAP);
        methodVisitor.visitFieldInsn(
                PUTFIELD,
                proxyTypeName,
                CACHED_HASH_CODE_FIELD.getName(),
                CACHED_HASH_CODE_FIELD.getDescriptor());

        methodVisitor.visitInsn(IRETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateHashCodeComputationForField(final MethodVisitor methodVisitor, final String fieldName,
            final Type fieldType) {
        // Compute hash code of the field name.
        methodVisitor.visitLdcInsn(fieldName);
        methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                STRING_TYPE_NAME,
                HASH_CODE_METHOD.getName(),
                HASH_CODE_METHOD.getDescriptor(),
                false);
        // Multiple it by 127.
        methodVisitor.visitIntInsn(BIPUSH, 127);
        methodVisitor.visitInsn(IMUL);

        // Load field value on the stack.
        methodVisitor.visitVarInsn(ALOAD, 0);
        final MethodDescriptor fieldAccessor = MethodDescriptor.forMethod(fieldName, fieldType);
        methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                proxyTypeName,
                fieldAccessor.getName(),
                fieldAccessor.getDescriptor(),
                false);

        if (fieldType.getSort() == Type.ARRAY) {
            // Call Arrays.hashCode() with a corresponding signature.
            final Type elementType = fieldType.getElementType();
            final Type argumentType = Types.isPrimitive(elementType) ? fieldType : OBJECT_ARRAY_TYPE;
            final MethodDescriptor hashCodeMethod =
                    MethodDescriptor.forMethod(HASH_CODE_METHOD.getName(), Type.INT_TYPE, argumentType);
            methodVisitor.visitMethodInsn(
                    INVOKESTATIC,
                    ARRAYS_TYPE_NAME,
                    hashCodeMethod.getName(),
                    hashCodeMethod.getDescriptor(),
                    false);
        } else {
            if (Types.isPrimitive(fieldType)) {
                // If the field has primitive type then box it.
                Boxer.box(methodVisitor, fieldType);
            }
            // Call hashCode() on the instance on the stack.
            methodVisitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    OBJECT_TYPE_NAME,
                    HASH_CODE_METHOD.getName(),
                    HASH_CODE_METHOD.getDescriptor(),
                    false);
        }

        // Xor the field name and the field value hash codes.
        methodVisitor.visitInsn(IXOR);
    }

    private void generateToStringMethod(final ClassVisitor classVisitor) {
        final MethodVisitor methodVisitor = classVisitor.visitMethod(
                ACC_PUBLIC,
                TO_STRING_METHOD.getName(),
                TO_STRING_METHOD.getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();

        final Label cachedToStringIsNullLabel = new Label();
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(
                GETFIELD,
                proxyTypeName,
                CACHED_TO_STRING_FIELD.getName(),
                CACHED_TO_STRING_FIELD.getDescriptor());
        methodVisitor.visitInsn(DUP);
        methodVisitor.visitJumpInsn(IFNULL, cachedToStringIsNullLabel);
        methodVisitor.visitInsn(ARETURN);

        methodVisitor.visitLabel(cachedToStringIsNullLabel);
        methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

        methodVisitor.visitTypeInsn(NEW, STRING_BUILDER_TYPE_NAME);
        methodVisitor.visitInsn(DUP);
        final MethodDescriptor constructor = MethodDescriptor.forDefaultConstructor();
        methodVisitor.visitMethodInsn(
                INVOKESPECIAL,
                STRING_BUILDER_TYPE_NAME,
                constructor.getName(),
                constructor.getDescriptor(),
                false);

        methodVisitor.visitLdcInsn("@" + annotation.getType().getClassName() + "(");
        generateStringBuilderAppendInvocation(methodVisitor, Types.STRING_TYPE);

        boolean addComma = false;
        for (final Map.Entry<String, Type> entry : annotation.getFields().entrySet()) {
            final String fieldName = entry.getKey();
            final Type fieldType = entry.getValue();
            appendFieldName(methodVisitor, fieldName, addComma);
            appendFieldValue(methodVisitor, fieldName, fieldType);
            addComma = true;
        }

        methodVisitor.visitIntInsn(BIPUSH, ')');
        generateStringBuilderAppendInvocation(methodVisitor, Type.CHAR_TYPE);
        methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                STRING_BUILDER_TYPE_NAME,
                TO_STRING_METHOD.getName(),
                TO_STRING_METHOD.getDescriptor(),
                false);

        methodVisitor.visitInsn(DUP);
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitInsn(SWAP);
        methodVisitor.visitFieldInsn(
                PUTFIELD,
                proxyTypeName,
                CACHED_TO_STRING_FIELD.getName(),
                CACHED_TO_STRING_FIELD.getDescriptor());

        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void appendFieldName(final MethodVisitor methodVisitor, final String fieldName, final boolean addComma) {
        final String prefix = addComma ? ", " : "";
        methodVisitor.visitLdcInsn(prefix + fieldName + '=');
        generateStringBuilderAppendInvocation(methodVisitor, Types.STRING_TYPE);
    }

    private void appendFieldValue(final MethodVisitor methodVisitor, final String fieldName, final Type fieldType) {
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitFieldInsn(GETFIELD, proxyTypeName, fieldName, fieldType.getDescriptor());
        if (fieldType.getSort() == Type.ARRAY) {
            generateArraysToStringInvocation(methodVisitor, fieldType);
            generateStringBuilderAppendInvocation(methodVisitor, Types.STRING_TYPE);
        } else {
            generateStringBuilderAppendInvocation(methodVisitor, fieldType);
        }
    }

    private void generateArraysToStringInvocation(final MethodVisitor methodVisitor, final Type fieldType) {
        final Type elementType = fieldType.getElementType();
        final Type argumentType = Types.isPrimitive(elementType) ? fieldType : OBJECT_ARRAY_TYPE;
        final MethodDescriptor toStringMethod =
                MethodDescriptor.forMethod(TO_STRING_METHOD.getName(), Types.STRING_TYPE, argumentType);
        methodVisitor.visitMethodInsn(
                INVOKESTATIC,
                ARRAYS_TYPE_NAME,
                toStringMethod.getName(),
                toStringMethod.getDescriptor(),
                false);
    }

    private void generateStringBuilderAppendInvocation(final MethodVisitor methodVisitor, final Type parameterType) {
        final MethodDescriptor appendMethod = stringBuilderAppendMethodResolver.resolveMethod(parameterType);
        methodVisitor.visitMethodInsn(
                INVOKEVIRTUAL,
                STRING_BUILDER_TYPE_NAME,
                appendMethod.getName(),
                appendMethod.getDescriptor(),
                false);
    }

    private void generateAnnotationTypeMethod(final ClassVisitor classVisitor) {
        final MethodVisitor methodVisitor = classVisitor.visitMethod(
                ACC_PUBLIC,
                ANNOTATION_TYPE_METHOD.getName(),
                ANNOTATION_TYPE_METHOD.getDescriptor(),
                null,
                null);
        methodVisitor.visitCode();
        methodVisitor.visitLdcInsn(annotation.getType());
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(0, 0);
        methodVisitor.visitEnd();
    }

    private void generateGetters(final ClassVisitor classVisitor) {
        for (final Map.Entry<String, Type> field : annotation.getFields().entrySet()) {
            final String name = field.getKey();
            final Type type = field.getValue();

            final MethodDescriptor method = MethodDescriptor.forMethod(name, type);
            final MethodVisitor methodVisitor = classVisitor.visitMethod(
                    ACC_PUBLIC,
                    method.getName(),
                    method.getDescriptor(),
                    null,
                    null);
            methodVisitor.visitCode();
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitFieldInsn(GETFIELD, proxyTypeName, name, type.getDescriptor());

            methodVisitor.visitInsn(type.getOpcode(IRETURN));
            methodVisitor.visitMaxs(0, 0);
            methodVisitor.visitEnd();
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

