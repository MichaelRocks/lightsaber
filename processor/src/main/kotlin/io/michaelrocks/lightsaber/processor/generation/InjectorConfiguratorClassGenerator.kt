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

package io.michaelrocks.lightsaber.processor.generation

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.commons.*
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.InjectorConfigurator
import io.michaelrocks.lightsaber.processor.generation.model.KeyRegistry
import io.michaelrocks.lightsaber.processor.model.*
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

class InjectorConfiguratorClassGenerator(
    private val classRegistry: ClassRegistry,
    private val keyRegistry: KeyRegistry,
    private val injectorConfigurator: InjectorConfigurator
) {
  companion object {
    private val CONFIGURE_INJECTOR_METHOD =
        MethodDescriptor.forMethod("configureInjector",
            Type.VOID_TYPE, LightsaberTypes.LIGHTSABER_INJECTOR_TYPE, Types.OBJECT_TYPE)
    private val REGISTER_PROVIDER_METHOD =
        MethodDescriptor.forMethod("registerProvider", Type.VOID_TYPE, Types.KEY_TYPE, Types.PROVIDER_TYPE)

    private val DELEGATE_PROVIDER_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.PROVIDER_TYPE)

    private val INVALID_LOCAL = -1
  }

  fun generate(): ByteArray {
    val classWriter =
        StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
        V1_6,
        ACC_PUBLIC or ACC_SUPER,
        injectorConfigurator.type.internalName,
        null,
        Types.OBJECT_TYPE.internalName,
        arrayOf(LightsaberTypes.INJECTOR_CONFIGURATOR_TYPE.internalName))

    classVisitor.newDefaultConstructor()
    classVisitor.newMethod(ACC_PUBLIC, CONFIGURE_INJECTOR_METHOD) { configureInjector() }

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun GeneratorAdapter.configureInjector() {
    loadArg(1)
    checkCast(injectorConfigurator.component.type)
    injectorConfigurator.component.providers.forEach { provider ->
      dup()
      configureInjectorWithModule(provider)
    }
    pop()
  }

  private fun GeneratorAdapter.configureInjectorWithModule(moduleProvider: ModuleProvider) {
    val moduleLocal = getModule(moduleProvider.provisionPoint)

    moduleProvider.module.providers.forEach { provider ->
      registerProvider(provider) {
        if (moduleLocal == INVALID_LOCAL) {
          check(provider.isConstructorProvider)
          newConstructorProvider(provider)
        } else {
          check(!provider.isConstructorProvider)
          newModuleProvider(provider) {
            loadLocal(moduleLocal)
          }
        }
      }
    }
  }

  private fun GeneratorAdapter.getModule(provisionPoint: ModuleProvisionPoint): Int {
    return when (provisionPoint) {
      is ModuleProvisionPoint.Method -> getModule(provisionPoint)
      is ModuleProvisionPoint.Field -> getModule(provisionPoint)
      is ModuleProvisionPoint.Null -> INVALID_LOCAL
    }
  }

  private fun GeneratorAdapter.getModule(provisionPoint: ModuleProvisionPoint.Method): Int {
    return newLocal(provisionPoint.method.type.returnType) {
      invokeVirtual(injectorConfigurator.component.type, provisionPoint.method.toMethodDescriptor())
    }
  }

  private fun GeneratorAdapter.getModule(provisionPoint: ModuleProvisionPoint.Field): Int {
    return newLocal(provisionPoint.field.type) {
      getField(injectorConfigurator.component.type, provisionPoint.field.toFieldDescriptor())
    }
  }

  private fun GeneratorAdapter.registerProvider(provider: Provider, providerCreator: () -> Unit) {
    loadArg(0)
    getKey(keyRegistry, provider.dependency)

    when (provider.scope) {
      is Scope.Class -> newDelegator(provider, provider.scope.scopeType, providerCreator)
      is Scope.None -> providerCreator()
    }

    invokeVirtual(LightsaberTypes.LIGHTSABER_INJECTOR_TYPE, REGISTER_PROVIDER_METHOD)
  }

  private fun GeneratorAdapter.newDelegator(provider: Provider, scopeType: Type, providerCreator: () -> Unit) {
    newInstance(scopeType)
    dup()
    providerCreator()
    invokeConstructor(scopeType, DELEGATE_PROVIDER_CONSTRUCTOR)
  }

  private fun GeneratorAdapter.newModuleProvider(provider: Provider, moduleRetriever: () -> Unit) {
    newInstance(provider.type)
    dup()
    moduleRetriever()
    loadArg(0)
    val constructor = MethodDescriptor.forConstructor(provider.moduleType, Types.INJECTOR_TYPE)
    invokeConstructor(provider.type, constructor)
  }

  private fun GeneratorAdapter.newConstructorProvider(provider: Provider) {
    newInstance(provider.type)
    dup()
    loadArg(0)
    val constructor = MethodDescriptor.forConstructor(Types.INJECTOR_TYPE)
    invokeConstructor(provider.type, constructor)
  }
}
