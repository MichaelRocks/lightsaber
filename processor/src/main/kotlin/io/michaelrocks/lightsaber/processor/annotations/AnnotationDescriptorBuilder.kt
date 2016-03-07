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

package io.michaelrocks.lightsaber.processor.annotations

import org.objectweb.asm.Type
import java.util.*

class AnnotationDescriptorBuilder(private val annotationType: Type) {
  private var fields: MutableMap<String, Type>? = null

  fun addDefaultField(type: Type): AnnotationDescriptorBuilder {
    return addField("value", type)
  }

  fun addField(name: String, type: Type): AnnotationDescriptorBuilder {
    if (fields == null) {
      fields = LinkedHashMap<String, Type>()
    }
    fields!!.put(name, type)
    return this
  }

  fun build(): AnnotationDescriptor {
    val unmodifiableFields = if (fields == null) emptyMap<String, Type>() else Collections.unmodifiableMap(fields)
    return AnnotationDescriptor(annotationType, unmodifiableFields)
  }
}
