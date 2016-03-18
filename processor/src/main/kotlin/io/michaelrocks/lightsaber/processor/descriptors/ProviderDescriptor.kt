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

import io.michaelrocks.grip.mirrors.AnnotationMirror
import org.objectweb.asm.Type
import java.util.*

class ProviderDescriptor private constructor(
    val providerType: Type,
    val qualifiedProvidableType: QualifiedType,
    val providerField: QualifiedFieldDescriptor?,
    val providerMethod: QualifiedMethodDescriptor?,
    val moduleType: Type,
    val delegatorType: Type?
) {
  constructor(
      providerType: Type,
      providableType: QualifiedType,
      providerField: QualifiedFieldDescriptor,
      moduleType: Type
  ) : this(providerType, providableType, providerField, null, moduleType, null)

  constructor(
      providerType: Type,
      providableType: QualifiedType,
      providerMethod: QualifiedMethodDescriptor,
      moduleType: Type,
      delegatorType: Type?
  ) : this(providerType, providableType, null, providerMethod, moduleType, delegatorType)
}

val ProviderDescriptor.qualifier: AnnotationMirror?
  get() = qualifiedProvidableType.qualifier

val ProviderDescriptor.providableType: Type
  get() = qualifiedProvidableType.type

val ProviderDescriptor.dependencies: List<QualifiedType>
  get() {
    if (providerMethod == null) {
      return emptyList()
    }

    val dependencies = ArrayList<QualifiedType>(providerMethod.argumentTypes.size)
    for (argumentType in providerMethod.argumentTypes) {
      val dependencyType = argumentType.parameterType ?: argumentType.rawType
      dependencies.add(QualifiedType(dependencyType))
    }
    return dependencies
  }

val ProviderDescriptor.isConstructorProvider: Boolean
  get() = providerMethod != null && providerMethod.isConstructor
