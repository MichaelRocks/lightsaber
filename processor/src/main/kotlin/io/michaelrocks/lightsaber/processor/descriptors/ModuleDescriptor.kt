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

inline fun ModuleDescriptor(
    moduleType: Type,
    body: ModuleDescriptor.Builder.() -> Unit
): ModuleDescriptor =
    ModuleDescriptor.Builder(moduleType).apply { body() }.build()

data class ModuleDescriptor private constructor(
    val moduleType: Type,
    val configuratorType: Type,
    val providers: List<ProviderDescriptor>
) {
  private constructor(
      builder: ModuleDescriptor.Builder
  ) : this(
      moduleType = builder.moduleType,
      configuratorType = builder.configuratorType,
      providers = Collections.unmodifiableList(builder.providers)
  )

  class Builder(val moduleType: Type) {
    internal val configuratorType: Type
    internal val providers = ArrayList<ProviderDescriptor>()

    init {
      val moduleNameWithDollars = moduleType.internalName.replace('/', '$')
      this.configuratorType =
          Type.getObjectType("io/michaelrocks/lightsaber/InjectorConfigurator\$$moduleNameWithDollars")
    }

    fun addProviderField(providerField: QualifiedFieldDescriptor): Builder {
      val providerType = Type.getObjectType("${moduleType.internalName}\$Provider\$${providers.size}")
      val providableType = QualifiedType(providerField.rawType, providerField.qualifier)
      val provider = ProviderDescriptor(providerType, providableType, providerField, moduleType)
      return addProvider(provider)
    }

    fun addProviderMethod(providerMethod: QualifiedMethodDescriptor, scope: ScopeDescriptor?): Builder {
      val providerType = Type.getObjectType("${moduleType.internalName}\$Provider\$${providers.size}")
      val providableType = QualifiedType(providerMethod.returnType.rawType, providerMethod.resultQualifier)
      val delegatorType = scope?.providerType
      val provider = ProviderDescriptor(providerType, providableType, providerMethod, moduleType, delegatorType)
      return addProvider(provider)
    }

    fun addProvider(provider: ProviderDescriptor): Builder {
      providers.add(provider)
      return this
    }

    fun build(): ModuleDescriptor {
      return ModuleDescriptor(this)
    }
  }
}
