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

package io.michaelrocks.lightsaber.processor.descriptors

import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.commons.Types
import org.objectweb.asm.Type
import java.util.*

inline fun PackageInvaderDescriptor(
    packageName: String,
    body: PackageInvaderDescriptor.Builder.() -> Unit
): PackageInvaderDescriptor =
    PackageInvaderDescriptor.Builder(packageName).apply { body() }.build()

data class PackageInvaderDescriptor(
    val type: Type,
    val packageName: String,
    val classFields: Map<Type, FieldDescriptor>
) {
  private constructor(
      builder: PackageInvaderDescriptor.Builder
  ) : this(
      type = builder.type,
      packageName = builder.packageName,
      classFields = Collections.unmodifiableMap(HashMap(builder.classFields))
  )

  fun getClassField(type: Type): FieldDescriptor? {
    return classFields[type]
  }

  class Builder(internal val packageName: String) {
    companion object {
      private val CLASS_NAME = "Lightsaber\$PackageInvader"
      private val FIELD_PREFIX = "class"
    }

    internal val type: Type = Type.getObjectType(packageName + '/' + CLASS_NAME)
    internal val classes = HashSet<Type>()
    internal val classFields = HashMap<Type, FieldDescriptor>()

    fun addClass(type: Type): Builder {
      classes.add(type)
      return this
    }

    fun build(): PackageInvaderDescriptor {
      addClassFields()
      return PackageInvaderDescriptor(this)
    }

    private fun addClassFields() {
      classFields.clear()
      for (type in classes) {
        addClassField(type)
      }
    }

    private fun addClassField(type: Type) {
      val fieldName = FIELD_PREFIX + classFields.size
      val fieldType = GenericType.ParameterizedType(Types.CLASS_TYPE, GenericType.RawType(type))
      val field = FieldDescriptor(fieldName, fieldType)
      classFields.put(type, field)
    }
  }
}
