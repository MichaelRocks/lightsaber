/*
 * Copyright 2016 Michael Rozumyanskiy
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
import io.michaelrocks.grip.mirrors.AnnotationMirror
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.EnumMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.ClassProducer
import java.lang.reflect.Array
import java.util.HashSet

class AnnotationCreator(
    private val classProducer: ClassProducer,
    private val classRegistry: ClassRegistry
) {
  private val generatedAnnotationProxies = HashSet<Type.Object>()

  fun newAnnotation(generator: GeneratorAdapter, data: AnnotationMirror) {
    val annotationProxyType = composeAnnotationProxyType(data.type)
    classRegistry.getClassMirror(data.type).let { mirror ->
      generateAnnotationProxyClassIfNecessary(mirror, annotationProxyType)
      constructAnnotationProxy(generator, mirror, data, annotationProxyType)
    }
  }

  private fun composeAnnotationProxyType(annotationType: Type.Object): Type.Object =
      getObjectTypeByInternalName(annotationType.internalName + "\$Lightsaber\$Proxy")

  private fun generateAnnotationProxyClassIfNecessary(annotation: ClassMirror, annotationProxyType: Type.Object) {
    if (generatedAnnotationProxies.add(annotationProxyType)) {
      val generator = AnnotationProxyGenerator(classRegistry, annotation, annotationProxyType)
      val annotationProxyClassData = generator.generate()
      classProducer.produceClass(annotationProxyType.internalName, annotationProxyClassData)
    }
  }

  private fun constructAnnotationProxy(generator: GeneratorAdapter, annotation: ClassMirror,
      data: AnnotationMirror, annotationProxyType: Type) {
    generator.newInstance(annotationProxyType)
    generator.dup()

    for (method in annotation.methods) {
      createValue(generator, method.type.returnType, data.values[method.name]!!)
    }

    val fieldTypes = annotation.methods.map { it.type.returnType }
    val argumentTypes = fieldTypes.toTypedArray()
    val constructor = MethodDescriptor.forConstructor(*argumentTypes)
    generator.invokeConstructor(annotationProxyType, constructor)
  }

  private fun createValue(generator: GeneratorAdapter, fieldType: Type, value: Any) {
    when (fieldType) {
      is Type.Primitive -> {
        // TODO: Check if the value class corresponds to fieldType.
        generator.visitLdcInsn(value)
      }
      is Type.Array -> createArray(generator, fieldType, value)
      is Type.Object -> createObject(generator, fieldType, value)
      else -> throw IllegalArgumentException("Unsupported annotation field type: $fieldType")
    }
  }

  private fun createArray(generator: GeneratorAdapter, fieldType: Type.Array, value: Any) {
    // TODO: Check if the value class corresponds to fieldType.
    val elementType = fieldType.elementType
    if (value.javaClass.isArray) {
      val arrayLength = Array.getLength(value)
      generator.newArray(elementType, arrayLength)
      val values = (0 until arrayLength).map { index -> Array.get(value, index) }
      populateArray(generator, elementType, values)
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
      is EnumMirror -> createEnumValue(generator, value)
      is AnnotationMirror -> newAnnotation(generator, value)
      else -> throw IllegalArgumentException("Unsupported annotation value type: $value")
    }
  }

  private fun createEnumValue(generator: GeneratorAdapter, value: EnumMirror) {
    generator.getStatic(value.type, value.value, value.type)
  }
}
