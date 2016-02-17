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

import org.apache.commons.lang3.Validate
import org.objectweb.asm.Type
import java.util.*

class AnnotationDataBuilder {
  private val annotationType: Type
  private var values: MutableMap<String, Any>? = null

  constructor(annotationType: Type) {
    this.annotationType = annotationType
  }

  constructor(annotationType: Type, defaults: AnnotationData) {
    this.annotationType = annotationType
    values = HashMap(defaults.values)
  }

  fun addDefaultValue(defaultValue: Any): AnnotationDataBuilder {
    return addDefaultValue("value", defaultValue)
  }

  fun addDefaultValue(name: String, defaultValue: Any): AnnotationDataBuilder {
    Validate.notNull(name)
    Validate.notNull(defaultValue)
    if (values == null) {
      values = LinkedHashMap<String, Any>()
    }
    values!!.put(name, defaultValue)
    return this
  }

  fun build(): AnnotationData {
    val unmodifiableValues = if (values == null) emptyMap<String, Any>() else Collections.unmodifiableMap(values)
    return AnnotationData(annotationType, unmodifiableValues)
  }
}
