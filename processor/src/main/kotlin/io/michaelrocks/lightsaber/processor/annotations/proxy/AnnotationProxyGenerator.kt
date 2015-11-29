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

package io.michaelrocks.lightsaber.processor.annotations.proxy

import io.michaelrocks.lightsaber.processor.annotations.AnnotationDescriptor
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.getType
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.descriptor
import io.michaelrocks.lightsaber.processor.graph.TypeGraph
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.commons.GeneratorAdapter.*
import java.util.*

class AnnotationProxyGenerator(
    private val typeGraph: TypeGraph,
    private val annotation: AnnotationDescriptor,
    private val proxyType: Type
) {
  companion object {
    private val ARRAYS_TYPE = getType<Arrays>()
    private val OBJECT_ARRAY_TYPE = getType<Array<Any>>()
    private val STRING_BUILDER_TYPE = getType<StringBuilder>()

    private val HASH_CODE_METHOD = MethodDescriptor.forMethod("hashCode", Type.INT_TYPE)
    private val EQUALS_METHOD = MethodDescriptor.forMethod("equals", Type.BOOLEAN_TYPE, Types.OBJECT_TYPE)
    private val TO_STRING_METHOD = MethodDescriptor.forMethod("toString", Types.STRING_TYPE)
    private val FLOAT_TO_INT_BITS_METHOD = MethodDescriptor.forMethod("floatToIntBits", Type.INT_TYPE, Type.FLOAT_TYPE)
    private val DOUBLE_TO_LONG_BITS_METHOD =
        MethodDescriptor.forMethod("doubleToLongBits", Type.LONG_TYPE, Type.DOUBLE_TYPE)
    private val ANNOTATION_TYPE_METHOD = MethodDescriptor.forMethod("annotationType", Types.CLASS_TYPE)

    private val CACHED_HASH_CODE_FIELD = FieldDescriptor("\$cachedHashCode", Types.BOXED_INT_TYPE)
    private val CACHED_TO_STRING_FIELD = FieldDescriptor("\$cachedToString", Types.STRING_TYPE)

    private val STRING_BUILDER_APPEND_METHOD_RESOLVER = StringBuilderAppendMethodResolver()
  }

  fun generate(): ByteArray {
    val classWriter = StandaloneClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, typeGraph)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
        V1_6,
        ACC_PUBLIC or ACC_SUPER,
        proxyType.internalName,
        null,
        Type.getInternalName(Any::class.java),
        arrayOf<String>(annotation.type.internalName))

    generateFields(classVisitor)
    generateConstructor(classVisitor)
    generateEqualsMethod(classVisitor)
    generateHashCodeMethod(classVisitor)
    generateToStringMethod(classVisitor)
    generateAnnotationTypeMethod(classVisitor)
    generateGetters(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateFields(classVisitor: ClassVisitor) {
    for ((name, type) in annotation.fields.entries) {
      val fieldVisitor = classVisitor.visitField(
          ACC_PRIVATE or ACC_FINAL,
          name,
          type.descriptor,
          null,
          null)
      fieldVisitor.visitEnd()
    }

    val cachedHashCodeField = classVisitor.visitField(
        ACC_PRIVATE,
        CACHED_HASH_CODE_FIELD.name,
        CACHED_HASH_CODE_FIELD.descriptor,
        null,
        null)
    cachedHashCodeField.visitEnd()

    val cachedToStringField = classVisitor.visitField(
        ACC_PRIVATE,
        CACHED_TO_STRING_FIELD.name,
        CACHED_TO_STRING_FIELD.descriptor,
        null,
        null)
    cachedToStringField.visitEnd()
  }

  private fun generateConstructor(classVisitor: ClassVisitor) {
    val fieldTypes = annotation.fields.values
    val argumentTypes = fieldTypes.toTypedArray()
    val method = MethodDescriptor.forConstructor(*argumentTypes)
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, method)
    generator.visitCode()

    // Call the constructor of the super class.
    generator.loadThis()
    val constructor = MethodDescriptor.forDefaultConstructor()
    generator.invokeConstructor(Types.OBJECT_TYPE, constructor)

    // Initialize fields with arguments passed to the constructor.
    annotation.fields.entries.forEachIndexed { localPosition, entry ->
      val (fieldName, fieldType) = entry
      generator.loadThis()
      generator.loadArg(localPosition)
      generator.putField(proxyType, fieldName, fieldType)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateEqualsMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, EQUALS_METHOD)
    generator.visitCode()

    // if (this == object) return true;
    generator.loadThis()
    generator.loadArg(0)
    val referencesNotEqualLabel = generator.newLabel()
    generator.ifCmp(Types.OBJECT_TYPE, NE, referencesNotEqualLabel)
    generator.push(true)
    generator.returnValue()

    generator.visitLabel(referencesNotEqualLabel)
    // if (!(object instanceof *AnnotationType*)) return false;
    generator.loadArg(0)
    generator.instanceOf(annotation.type)
    val objectHasSameTypeLabel = generator.newLabel()
    generator.ifZCmp(NE, objectHasSameTypeLabel)
    generator.push(false)
    generator.returnValue()

    generator.visitLabel(objectHasSameTypeLabel)
    // Cast the object to *AnnotationType*.
    val annotationLocal = generator.newLocal(annotation.type)
    generator.loadArg(0)
    generator.checkCast(annotation.type)
    generator.storeLocal(annotationLocal)

    val fieldsNotEqualLabel = generator.newLabel()
    for ((fieldName, fieldType) in annotation.fields.entries) {
      generateEqualsInvocationForField(generator, fieldName, fieldType, annotationLocal, fieldsNotEqualLabel)
    }
    generator.push(true)

    val returnLabel = generator.newLabel()
    generator.goTo(returnLabel)

    generator.visitLabel(fieldsNotEqualLabel)
    generator.push(false)

    generator.visitLabel(returnLabel)
    generator.returnValue()
    generator.endMethod()
  }

  private fun generateEqualsInvocationForField(generator: GeneratorAdapter, fieldName: String, fieldType: Type,
      annotationLocal: Int, fieldsNotEqualLabel: Label) {
    val fieldAccessor = MethodDescriptor.forMethod(fieldName, fieldType)

    generator.loadThis()
    generator.invokeVirtual(proxyType, fieldAccessor)
    convertFieldValue(generator, fieldType)

    generator.loadLocal(annotationLocal)
    generator.invokeInterface(annotation.type, fieldAccessor)
    convertFieldValue(generator, fieldType)

    if (fieldType.sort == Type.ARRAY) {
      // Call Arrays.equals() with a corresponding signature.
      val elementType = fieldType.elementType
      val argumentType = if (Types.isPrimitive(elementType)) fieldType else OBJECT_ARRAY_TYPE
      val equalsMethod = MethodDescriptor.forMethod(EQUALS_METHOD.name, Type.BOOLEAN_TYPE, argumentType, argumentType)
      generator.invokeStatic(ARRAYS_TYPE, equalsMethod)
      generator.ifZCmp(EQ, fieldsNotEqualLabel)
    } else if (Types.isPrimitive(fieldType)) {
      when (fieldType.sort) {
        Type.DOUBLE, Type.LONG -> generator.ifCmp(Type.LONG_TYPE, NE, fieldsNotEqualLabel)
        else -> generator.ifICmp(NE, fieldsNotEqualLabel)
      }
    } else {
      // Call equals() on the instances on the stack.
      generator.invokeVirtual(Types.OBJECT_TYPE, EQUALS_METHOD)
      generator.ifZCmp(EQ, fieldsNotEqualLabel)
    }
  }

  private fun convertFieldValue(generator: GeneratorAdapter, fieldType: Type) {
    when (fieldType.sort) {
      Type.FLOAT -> generator.invokeStatic(Types.BOXED_FLOAT_TYPE, FLOAT_TO_INT_BITS_METHOD)
      Type.DOUBLE -> generator.invokeStatic(Types.BOXED_DOUBLE_TYPE, DOUBLE_TO_LONG_BITS_METHOD)
    }
  }

  private fun generateHashCodeMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, HASH_CODE_METHOD)
    generator.visitCode()

    val cacheHashCodeIsNullLabel = generator.newLabel()
    generator.loadThis()
    generator.getField(proxyType, CACHED_HASH_CODE_FIELD)
    generator.dup()
    generator.ifNull(cacheHashCodeIsNullLabel)
    generator.unbox(Type.INT_TYPE)
    generator.returnValue()

    generator.visitLabel(cacheHashCodeIsNullLabel)
    generator.push(0)

    for ((fieldName, fieldType) in annotation.fields.entries) {
      // Hash code for annotation is the sum of 127 * fieldName.hashCode() ^ fieldValue.hashCode().
      generateHashCodeComputationForField(generator, fieldName, fieldType)
      generator.math(ADD, Type.INT_TYPE)
    }

    generator.dup()
    generator.valueOf(Type.INT_TYPE)
    generator.loadThis()
    generator.swap(Type.INT_TYPE, proxyType)
    generator.putField(proxyType, CACHED_HASH_CODE_FIELD)

    generator.returnValue()
    generator.endMethod()
  }

  private fun generateHashCodeComputationForField(generator: GeneratorAdapter, fieldName: String, fieldType: Type) {
    // Compute hash code of the field name.
    generator.push(fieldName)
    generator.invokeVirtual(Types.STRING_TYPE, HASH_CODE_METHOD)
    // Multiple it by 127.
    generator.push(127)
    generator.math(MUL, Type.INT_TYPE)

    // Load field value on the stack.
    generator.loadThis()
    val fieldAccessor = MethodDescriptor.forMethod(fieldName, fieldType)
    generator.invokeVirtual(proxyType, fieldAccessor)

    if (fieldType.sort == Type.ARRAY) {
      // Call Arrays.hashCode() with a corresponding signature.
      val elementType = fieldType.elementType
      val argumentType = if (Types.isPrimitive(elementType)) fieldType else OBJECT_ARRAY_TYPE
      val hashCodeMethod = MethodDescriptor.forMethod(HASH_CODE_METHOD.name, Type.INT_TYPE, argumentType)
      generator.invokeStatic(ARRAYS_TYPE, hashCodeMethod)
    } else {
      // If the field has primitive type then box it.
      generator.valueOf(fieldType)
      // Call hashCode() on the instance on the stack.
      generator.invokeVirtual(Types.OBJECT_TYPE, HASH_CODE_METHOD)
    }

    // Xor the field name and the field value hash codes.
    generator.math(XOR, Type.INT_TYPE)
  }

  private fun generateToStringMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, TO_STRING_METHOD)
    generator.visitCode()

    val cachedToStringIsNullLabel = generator.newLabel()
    generator.loadThis()
    generator.getField(proxyType, CACHED_TO_STRING_FIELD)
    generator.dup()
    generator.ifNull(cachedToStringIsNullLabel)
    generator.returnValue()

    generator.visitLabel(cachedToStringIsNullLabel)
    generator.newInstance(STRING_BUILDER_TYPE)
    generator.dup()
    generator.invokeConstructor(STRING_BUILDER_TYPE, MethodDescriptor.forDefaultConstructor())

    generator.push("@" + annotation.type.className + "(")
    generateStringBuilderAppendInvocation(generator, Types.STRING_TYPE)

    var addComma = false
    for (entry in annotation.fields.entries) {
      val fieldName = entry.key
      val fieldType = entry.value
      appendFieldName(generator, fieldName, addComma)
      appendFieldValue(generator, fieldName, fieldType)
      addComma = true
    }

    generator.push(')'.toInt())
    generateStringBuilderAppendInvocation(generator, Type.CHAR_TYPE)
    generator.invokeVirtual(STRING_BUILDER_TYPE, TO_STRING_METHOD)

    generator.dup()
    generator.loadThis()
    generator.swap()
    generator.putField(proxyType, CACHED_TO_STRING_FIELD)

    generator.returnValue()
    generator.endMethod()
  }

  private fun appendFieldName(generator: GeneratorAdapter, fieldName: String, addComma: Boolean) {
    val prefix = if (addComma) ", " else ""
    generator.push("$prefix$fieldName=")
    generateStringBuilderAppendInvocation(generator, Types.STRING_TYPE)
  }

  private fun appendFieldValue(generator: GeneratorAdapter, fieldName: String, fieldType: Type) {
    generator.loadThis()
    generator.getField(proxyType, fieldName, fieldType)
    if (fieldType.sort == Type.ARRAY) {
      generateArraysToStringInvocation(generator, fieldType)
      generateStringBuilderAppendInvocation(generator, Types.STRING_TYPE)
    } else {
      generateStringBuilderAppendInvocation(generator, fieldType)
    }
  }

  private fun generateArraysToStringInvocation(generator: GeneratorAdapter, fieldType: Type) {
    val elementType = fieldType.elementType
    val argumentType = if (Types.isPrimitive(elementType)) fieldType else OBJECT_ARRAY_TYPE
    val toStringMethod = MethodDescriptor.forMethod(TO_STRING_METHOD.name, Types.STRING_TYPE, argumentType)
    generator.invokeStatic(ARRAYS_TYPE, toStringMethod)
  }

  private fun generateStringBuilderAppendInvocation(generator: GeneratorAdapter, parameterType: Type) {
    val appendMethod = STRING_BUILDER_APPEND_METHOD_RESOLVER.resolveMethod(parameterType)
    generator.invokeVirtual(STRING_BUILDER_TYPE, appendMethod)
  }

  private fun generateAnnotationTypeMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, ANNOTATION_TYPE_METHOD)
    generator.visitCode()
    generator.push(annotation.type)
    generator.returnValue()
    generator.endMethod()
  }

  private fun generateGetters(classVisitor: ClassVisitor) {
    for ((fieldName, fieldType) in annotation.fields.entries) {

      val method = MethodDescriptor.forMethod(fieldName, fieldType)
      val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, method)
      generator.visitCode()
      generator.loadThis()
      generator.getField(proxyType, fieldName, fieldType)
      generator.returnValue()
      generator.endMethod()
    }
  }

  private class StringBuilderAppendMethodResolver {
    companion object {
      private const val METHOD_NAME = "append"
      private val BOOLEAN_METHOD = appendMethod(Type.BOOLEAN_TYPE)
      private val CHAR_METHOD = appendMethod(Type.CHAR_TYPE)
      private val FLOAT_METHOD = appendMethod(Type.FLOAT_TYPE)
      private val DOUBLE_METHOD = appendMethod(Type.DOUBLE_TYPE)
      private val INT_METHOD = appendMethod(Type.INT_TYPE)
      private val LONG_METHOD = appendMethod(Type.LONG_TYPE)
      private val OBJECT_METHOD = appendMethod(Types.OBJECT_TYPE)
      private val STRING_METHOD = appendMethod(Types.STRING_TYPE)

      private fun appendMethod(parameterType: Type): MethodDescriptor {
        return MethodDescriptor.forMethod(METHOD_NAME, STRING_BUILDER_TYPE, parameterType)
      }
    }

    fun resolveMethod(type: Type): MethodDescriptor {
      when (type.sort) {
        Type.BOOLEAN -> return BOOLEAN_METHOD
        Type.CHAR -> return CHAR_METHOD
        Type.FLOAT -> return FLOAT_METHOD
        Type.DOUBLE -> return DOUBLE_METHOD
        Type.BYTE, Type.SHORT, Type.INT -> return INT_METHOD
        Type.LONG -> return LONG_METHOD
        else -> return if (type == Types.STRING_TYPE) STRING_METHOD else OBJECT_METHOD
      }
    }
  }
}
