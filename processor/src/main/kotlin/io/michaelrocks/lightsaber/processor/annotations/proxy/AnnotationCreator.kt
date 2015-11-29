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

import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData
import io.michaelrocks.lightsaber.processor.annotations.AnnotationDescriptor
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.descriptors.EnumValueDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.ClassProducer
import io.michaelrocks.lightsaber.processor.logging.getLogger
import org.apache.commons.collections4.IteratorUtils
import org.objectweb.asm.Type
import java.lang.reflect.Array
import java.util.*

class AnnotationCreator(
    private val processorContext: ProcessorContext,
    private val classProducer: ClassProducer
) {
  private val generatedAnnotationProxies = HashSet<Type>()

  fun newAnnotation(generator: GeneratorAdapter, data: AnnotationData) {
    val annotationProxyType = composeAnnotationProxyType(data.type)
    processorContext.annotationRegistry.findAnnotationByType(data.type)!!.let { annotation ->
      generateAnnotationProxyClassIfNecessary(annotation, annotationProxyType)
      constructAnnotationProxy(generator, annotation, data, annotationProxyType)
    }
  }

  private fun composeAnnotationProxyType(annotationType: Type): Type =
      Type.getObjectType(annotationType.internalName + "\$Lightsaber\$Proxy")

  private fun generateAnnotationProxyClassIfNecessary(annotation: AnnotationDescriptor,
      annotationProxyType: Type) {
    if (generatedAnnotationProxies.add(annotationProxyType)) {
      val generator = AnnotationProxyGenerator(processorContext.typeGraph, annotation, annotationProxyType)
      val annotationProxyClassData = generator.generate()
      classProducer.produceClass(annotationProxyType.internalName, annotationProxyClassData)
    }
  }

  private fun constructAnnotationProxy(generator: GeneratorAdapter, annotation: AnnotationDescriptor,
      data: AnnotationData, annotationProxyType: Type) {
    generator.newInstance(annotationProxyType)
    generator.dup()

    val logger = getLogger()
    logger.error("annotation = $annotation")
    logger.error("data = $data")
    for ((fieldName, fieldType) in annotation.fields.entries) {
      createValue(generator, fieldType, data.values[fieldName]!!)
    }

    val fieldTypes = annotation.fields.values
    val argumentTypes = fieldTypes.toTypedArray()
    val constructor = MethodDescriptor.forConstructor(*argumentTypes)
    generator.invokeConstructor(annotationProxyType, constructor)
  }

  private fun createValue(generator: GeneratorAdapter, fieldType: Type, value: Any) {
    when (fieldType.sort) {
      Type.BOOLEAN, Type.CHAR, Type.BYTE, Type.SHORT, Type.INT, Type.FLOAT, Type.LONG, Type.DOUBLE -> {
        // TODO: Check if the value class corresponds to fieldType.
        generator.visitLdcInsn(value)
      }
      Type.ARRAY -> createArray(generator, fieldType, value)
      Type.OBJECT -> createObject(generator, fieldType, value)
      else -> throw IllegalArgumentException("Unsupported annotation field type: " + fieldType)
    }
  }

  private fun createArray(generator: GeneratorAdapter, fieldType: Type, value: Any) {
    // TODO: Check if the value class corresponds to fieldType.
    val elementType = fieldType.elementType
    if (value.javaClass.isArray) {
      generator.newArray(elementType, Array.getLength(value))
      val iterable = IteratorUtils.asIterable(IteratorUtils.arrayIterator<Any>(value))
      populateArray(generator, elementType, iterable)
    } else {
      @Suppress("UNCHECKED_CAST")
      val list = value as List<Any>
      generator.newArray(elementType, list.size)
      populateArray(generator, elementType, list)
    }
  }

  private fun populateArray(generator: GeneratorAdapter, elementType: Type, values: Iterable<Any>) {
    values.forEachIndexed { index, value ->
      generator.dup()
      generator.push(index)
      createValue(generator, elementType, value)
      generator.arrayStore(elementType)
    }
  }

  private fun createObject(generator: GeneratorAdapter, fieldType: Type, value: Any) {
    when (value) {
      is Type -> generator.push(value)
      is String -> generator.push(value)
      is EnumValueDescriptor -> createEnumValue(generator, value)
      is AnnotationData -> newAnnotation(generator, value)
    }
  }

  private fun createEnumValue(generator: GeneratorAdapter, value: EnumValueDescriptor) {
    generator.getStatic(value.type, value.value, value.type)
  }
}
