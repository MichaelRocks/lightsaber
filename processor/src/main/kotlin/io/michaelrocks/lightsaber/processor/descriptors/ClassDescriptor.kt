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

package io.michaelrocks.lightsaber.processor.descriptors

import io.michaelrocks.lightsaber.processor.annotations.AnnotationData
import org.objectweb.asm.Type
import java.util.*

fun ClassDescriptor(
    access: Int,
    name: String,
    superName: String?,
    interfaces: Array<String>? = null,
    annotations: Array<AnnotationData>? = null
) =
    ClassDescriptor(
        access,
        Type.getObjectType(name),
        superName?.let { Type.getObjectType(it) },
        interfaces?.map { Type.getObjectType(it) }.orEmpty(),
        annotations?.toList().orEmpty()
    )

data class ClassDescriptor(
    val access: Int,
    val classType: Type,
    val superType: Type?,
    val interfaceTypes: List<Type> = emptyList(),
    val annotations: List<AnnotationData> = emptyList()
) {
  constructor(builder: ClassDescriptor.Builder) : this(
      builder.access,
      Type.getObjectType(builder.className),
      builder.superName?.let { Type.getObjectType(it) },
      builder.interfaces?.map { Type.getObjectType(it) }.orEmpty(),
      Collections.unmodifiableList(builder.annotations)
  )

  class Builder(val access: Int, val className: String, val superName: String?, val interfaces: Array<String>?) {
    var annotations: List<AnnotationData> = emptyList()
      private set

    fun addAnnotation(annotation: AnnotationData): Builder {
      if (annotations.isEmpty()) {
        annotations = ArrayList()
      }
      annotations += annotation
      return this
    }

    fun build() = ClassDescriptor(this)
  }
}
