/*
 * Copyright 2018 Michael Rozumyanskiy
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

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getArrayType
import io.michaelrocks.grip.mirrors.getObjectType
import io.michaelrocks.grip.mirrors.isPrimitive
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.descriptor
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.ACC_FINAL
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.V1_6
import org.objectweb.asm.commons.GeneratorAdapter.ADD
import org.objectweb.asm.commons.GeneratorAdapter.EQ
import org.objectweb.asm.commons.GeneratorAdapter.MUL
import org.objectweb.asm.commons.GeneratorAdapter.NE
import org.objectweb.asm.commons.GeneratorAdapter.XOR
import java.util.Arrays

class AnnotationProxyGenerator(
    private val classRegistry: ClassRegistry,
    private val annotation: ClassMirror,
    private val proxyType: Type
) {
  companion object {
    private val ARRAYS_TYPE = getObjectType<Arrays>()
    private val OBJECT_ARRAY_TYPE = getArrayType<Array<Any>>()
    private val STRING_BUILDER_TYPE = getObjectType<StringBuilder>()

    private val HASH_CODE_METHOD = MethodDescriptor.forMethod("hashCode", Type.Primitive.Int)
    private val EQUALS_METHOD = MethodDescriptor.forMethod("equals", Type.Primitive.Boolean, Types.OBJECT_TYPE)
    private val TO_STRING_METHOD = MethodDescriptor.forMethod("toString", Types.STRING_TYPE)
    private val FLOAT_TO_INT_BITS_METHOD =
        MethodDescriptor.forMethod("floatToIntBits", Type.Primitive.Int, Type.Primitive.Float)
    private val DOUBLE_TO_LONG_BITS_METHOD =
        MethodDescriptor.forMethod("doubleToLongBits", Type.Primitive.Long, Type.Primitive.Double)
    private val ANNOTATION_TYPE_METHOD = MethodDescriptor.forMethod("annotationType", Types.CLASS_TYPE)

    private val CACHED_HASH_CODE_FIELD = FieldDescriptor("\$cachedHashCode", Types.BOXED_INT_TYPE)
    private val CACHED_TO_STRING_FIELD = FieldDescriptor("\$cachedToString", Types.STRING_TYPE)

    private val STRING_BUILDER_APPEND_METHOD_RESOLVER = StringBuilderAppendMethodResolver()
  }

  fun generate(): ByteArray {
    val classWriter = StandaloneClassWriter(ClassWriter.COMPUTE_MAXS or ClassWriter.COMPUTE_FRAMES, classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
        V1_6,
        ACC_PUBLIC or ACC_SUPER,
        proxyType.internalName,
        null,
        Types.OBJECT_TYPE.internalName,
        arrayOf(annotation.type.internalName)
    )

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
    for (method in annotation.methods) {
      val fieldVisitor = classVisitor.visitField(
          ACC_PRIVATE or ACC_FINAL,
          method.name,
          method.type.returnType.descriptor,
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
    val fieldTypes = annotation.methods.map { it.type.returnType }
    val argumentTypes = fieldTypes.toTypedArray()
    val constructor = MethodDescriptor.forConstructor(*argumentTypes)
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, constructor)
    generator.visitCode()

    // Call the constructor of the super class.
    generator.loadThis()
    generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forDefaultConstructor())

    // Initialize fields with arguments passed to the constructor.
    annotation.methods.forEachIndexed { localPosition, method ->
      generator.loadThis()
      generator.loadArg(localPosition)
      generator.putField(proxyType, method.name, method.type.returnType)
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
    for (method in annotation.methods) {
      generateEqualsInvocationForField(
          generator, method.name, method.type.returnType, annotationLocal, fieldsNotEqualLabel)
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

    if (fieldType is Type.Array) {
      // Call Arrays.equals() with a corresponding signature.
      val elementType = fieldType.elementType
      val argumentType = if (elementType.isPrimitive) fieldType else OBJECT_ARRAY_TYPE
      val equalsMethod =
          MethodDescriptor.forMethod(EQUALS_METHOD.name, Type.Primitive.Boolean, argumentType, argumentType)
      generator.invokeStatic(ARRAYS_TYPE, equalsMethod)
      generator.ifZCmp(EQ, fieldsNotEqualLabel)
    } else if (fieldType is Type.Primitive) {
      when (fieldType) {
        Type.Primitive.Double, Type.Primitive.Long -> generator.ifCmp(Type.Primitive.Long, NE, fieldsNotEqualLabel)
        else -> generator.ifICmp(NE, fieldsNotEqualLabel)
      }
    } else {
      // Call equals() on the instances on the stack.
      generator.invokeVirtual(Types.OBJECT_TYPE, EQUALS_METHOD)
      generator.ifZCmp(EQ, fieldsNotEqualLabel)
    }
  }

  private fun convertFieldValue(generator: GeneratorAdapter, fieldType: Type) {
    when (fieldType) {
      is Type.Primitive.Float -> generator.invokeStatic(Types.BOXED_FLOAT_TYPE, FLOAT_TO_INT_BITS_METHOD)
      is Type.Primitive.Double -> generator.invokeStatic(Types.BOXED_DOUBLE_TYPE, DOUBLE_TO_LONG_BITS_METHOD)
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
    generator.unbox(Type.Primitive.Int)
    generator.returnValue()

    generator.visitLabel(cacheHashCodeIsNullLabel)
    generator.push(0)

    for (method in annotation.methods) {
      // Hash code for annotation is the sum of 127 * name.hashCode() ^ value.hashCode().
      generateHashCodeComputationForField(generator, method.name, method.type.returnType)
      generator.math(ADD, Type.Primitive.Int)
    }

    generator.dup()
    generator.valueOf(Type.Primitive.Int)
    generator.loadThis()
    generator.swap(Type.Primitive.Int, proxyType)
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
    generator.math(MUL, Type.Primitive.Int)

    // Load field value on the stack.
    generator.loadThis()
    val fieldAccessor = MethodDescriptor.forMethod(fieldName, fieldType)
    generator.invokeVirtual(proxyType, fieldAccessor)

    if (fieldType is Type.Array) {
      // Call Arrays.hashCode() with a corresponding signature.
      val elementType = fieldType.elementType
      val argumentType = if (elementType.isPrimitive) fieldType else OBJECT_ARRAY_TYPE
      val hashCodeMethod = MethodDescriptor.forMethod(HASH_CODE_METHOD.name, Type.Primitive.Int, argumentType)
      generator.invokeStatic(ARRAYS_TYPE, hashCodeMethod)
    } else {
      // If the field has primitive type then box it.
      generator.valueOf(fieldType)
      // Call hashCode() on the instance on the stack.
      generator.invokeVirtual(Types.OBJECT_TYPE, HASH_CODE_METHOD)
    }

    // Xor the field name and the field value hash codes.
    generator.math(XOR, Type.Primitive.Int)
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
    for (method in annotation.methods) {
      appendFieldName(generator, method.name, addComma)
      appendFieldValue(generator, method.name, method.type.returnType)
      addComma = true
    }

    generator.push(')'.toInt())
    generateStringBuilderAppendInvocation(generator, Type.Primitive.Char)
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
    if (fieldType is Type.Array) {
      generateArraysToStringInvocation(generator, fieldType)
      generateStringBuilderAppendInvocation(generator, Types.STRING_TYPE)
    } else {
      generateStringBuilderAppendInvocation(generator, fieldType)
    }
  }

  private fun generateArraysToStringInvocation(generator: GeneratorAdapter, fieldType: Type.Array) {
    val elementType = fieldType.elementType
    val argumentType = if (elementType.isPrimitive) fieldType else OBJECT_ARRAY_TYPE
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
    for (method in annotation.methods) {

      val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, MethodDescriptor(method.name, method.type))
      generator.visitCode()
      generator.loadThis()
      generator.getField(proxyType, method.name, method.type.returnType)
      generator.returnValue()
      generator.endMethod()
    }
  }

  private class StringBuilderAppendMethodResolver {
    companion object {
      private const val METHOD_NAME = "append"
      private val BOOLEAN_METHOD = appendMethod(Type.Primitive.Boolean)
      private val CHAR_METHOD = appendMethod(Type.Primitive.Char)
      private val FLOAT_METHOD = appendMethod(Type.Primitive.Float)
      private val DOUBLE_METHOD = appendMethod(Type.Primitive.Double)
      private val INT_METHOD = appendMethod(Type.Primitive.Int)
      private val LONG_METHOD = appendMethod(Type.Primitive.Long)
      private val OBJECT_METHOD = appendMethod(Types.OBJECT_TYPE)
      private val STRING_METHOD = appendMethod(Types.STRING_TYPE)

      private fun appendMethod(parameterType: Type): MethodDescriptor {
        return MethodDescriptor.forMethod(METHOD_NAME, STRING_BUILDER_TYPE, parameterType)
      }
    }

    fun resolveMethod(type: Type): MethodDescriptor {
      if (type is Type.Primitive) {
        when (type) {
          Type.Primitive.Boolean -> return BOOLEAN_METHOD
          Type.Primitive.Char -> return CHAR_METHOD
          Type.Primitive.Float -> return FLOAT_METHOD
          Type.Primitive.Double -> return DOUBLE_METHOD
          Type.Primitive.Byte, Type.Primitive.Short, Type.Primitive.Int -> return INT_METHOD
          Type.Primitive.Long -> return LONG_METHOD
          else -> error("Cannot append $type to StringBuilder")
        }
      } else {
        return if (type == Types.STRING_TYPE) STRING_METHOD else OBJECT_METHOD
      }
    }
  }
}
