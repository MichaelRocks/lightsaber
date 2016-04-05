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

package io.michaelrocks.lightsaber.processor.generation.model

import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import org.objectweb.asm.Type
import java.util.*

private val CLASS_NAME = "Lightsaber\$PackageInvader"
private val FIELD_PREFIX = "class"

data class PackageInvader(
    val type: Type,
    val packageName: String,
    val classFields: Map<Type, FieldDescriptor>
) {
  private constructor(
      builder: Builder
  ) : this(
      type = builder.type,
      packageName = builder.packageName,
      classFields = Collections.unmodifiableMap(HashMap(builder.classFields))
  )

  fun getClassField(type: Type): FieldDescriptor? {
    return classFields[type]
  }

  class Builder(internal val packageName: String) {
    val type: Type = Type.getObjectType(packageName + '/' + CLASS_NAME)
    val classes = HashSet<Type>()
    val classFields = HashMap<Type, FieldDescriptor>()

    fun addClass(type: Type): Builder {
      classes.add(type)
      return this
    }

    fun build(): PackageInvader {
      addClassFields()
      return PackageInvader(this)
    }

    private fun addClassFields() {
      classFields.clear()
      for (type in classes) {
        addClassField(type)
      }
    }

    private fun addClassField(type: Type) {
      val name = FIELD_PREFIX + classFields.size
      val field = FieldDescriptor(name, Types.CLASS_TYPE)
      classFields.put(type, field)
    }
  }
}
