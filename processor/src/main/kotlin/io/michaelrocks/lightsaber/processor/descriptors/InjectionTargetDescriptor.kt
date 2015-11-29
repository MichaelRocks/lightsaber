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

import org.objectweb.asm.Type
import java.util.*

inline fun InjectionTargetDescriptor(
    targetType: Type,
    body: InjectionTargetDescriptor.Builder.() -> Unit
): InjectionTargetDescriptor =
    InjectionTargetDescriptor.Builder(targetType).apply { body() }.build()

data class InjectionTargetDescriptor private constructor(
    val targetType: Type,
    val injectableFields: Map<String, QualifiedFieldDescriptor>,
    val injectableConstructor: QualifiedMethodDescriptor?,
    val injectableConstructors: Map<MethodDescriptor, QualifiedMethodDescriptor>,
    val injectableMethods: Map<MethodDescriptor, QualifiedMethodDescriptor>,
    val injectableStaticFields: Map<String, QualifiedFieldDescriptor>,
    val injectableStaticMethods: Map<MethodDescriptor, QualifiedMethodDescriptor>,
    val scope: ScopeDescriptor?
) {
  private constructor(builder: InjectionTargetDescriptor.Builder) :
  this(
      targetType = builder.targetType,
      injectableFields = Collections.unmodifiableMap(builder.injectableFields),
      injectableConstructor = builder.injectableConstructors.values.firstOrNull(),
      injectableConstructors = Collections.unmodifiableMap(builder.injectableConstructors),
      injectableMethods = Collections.unmodifiableMap(builder.injectableMethods),
      injectableStaticFields = Collections.unmodifiableMap(builder.injectableStaticFields),
      injectableStaticMethods = Collections.unmodifiableMap(builder.injectableStaticMethods),
      scope = builder.scope
  )

  class Builder(val targetType: Type) {
    internal var hasDefaultConstructor: Boolean = false
    internal val injectableFields = LinkedHashMap<String, QualifiedFieldDescriptor>()
    internal val injectableConstructors = LinkedHashMap<MethodDescriptor, QualifiedMethodDescriptor>()
    internal val injectableMethods = LinkedHashMap<MethodDescriptor, QualifiedMethodDescriptor>()
    internal val injectableStaticFields = LinkedHashMap<String, QualifiedFieldDescriptor>()
    internal val injectableStaticMethods = LinkedHashMap<MethodDescriptor, QualifiedMethodDescriptor>()
    internal var scope: ScopeDescriptor? = null

    fun setHasDefaultConstructor(hasDefaultConstructor: Boolean) {
      this.hasDefaultConstructor = hasDefaultConstructor
    }

    fun addInjectableField(injectableField: QualifiedFieldDescriptor): Builder {
      injectableFields.put(injectableField.name, injectableField)
      return this
    }

    fun addInjectableConstructor(injectableConstructor: QualifiedMethodDescriptor): Builder {
      injectableConstructors.put(injectableConstructor.method, injectableConstructor)
      return this
    }

    fun addInjectableMethod(injectableMethod: QualifiedMethodDescriptor): Builder {
      injectableMethods.put(injectableMethod.method, injectableMethod)
      return this
    }

    fun addInjectableStaticField(injectableField: QualifiedFieldDescriptor): Builder {
      injectableStaticFields.put(injectableField.name, injectableField)
      return this
    }

    fun addInjectableStaticMethod(injectableMethod: QualifiedMethodDescriptor): Builder {
      injectableStaticMethods.put(injectableMethod.method, injectableMethod)
      return this
    }

    fun setScope(scopeDescriptor: ScopeDescriptor): Builder {
      this.scope = scopeDescriptor
      return this
    }

    fun build(): InjectionTargetDescriptor {
      // TODO: Allow to inject objects with default constructors when we can ensure they will be used.
      // if (injectableConstructors.isEmpty() && hasDefaultConstructor) {
      //   injectableConstructors.put(MethodDescriptor.forConstructor(),
      //       QualifiedMethodDescriptor.from(MethodDescriptor.forConstructor()))
      // }

      return InjectionTargetDescriptor(this)
    }
  }
}

fun InjectionTargetDescriptor.isInjectableField(fieldName: String): Boolean {
  return injectableFields.containsKey(fieldName)
}

fun InjectionTargetDescriptor.getInjectableFields(): Collection<QualifiedFieldDescriptor> {
  return injectableFields.values
}

fun InjectionTargetDescriptor.isInjectableConstructor(constructor: MethodDescriptor): Boolean {
  return injectableConstructor?.method == constructor
}

fun InjectionTargetDescriptor.getInjectableConstructors(): Collection<QualifiedMethodDescriptor> {
  return injectableConstructors.values
}

fun InjectionTargetDescriptor.isInjectableMethod(method: MethodDescriptor): Boolean {
  return injectableMethods.containsKey(method)
}

fun InjectionTargetDescriptor.getInjectableMethods(): Collection<QualifiedMethodDescriptor> {
  return injectableMethods.values
}

fun InjectionTargetDescriptor.isInjectableStaticField(fieldName: String): Boolean {
  return injectableStaticFields.containsKey(fieldName)
}

fun InjectionTargetDescriptor.isInjectableStaticMethod(method: MethodDescriptor): Boolean {
  return injectableStaticMethods.containsKey(method)
}
