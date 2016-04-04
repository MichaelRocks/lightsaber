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
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.*
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.generation.model.GenerationConfiguration
import io.michaelrocks.lightsaber.processor.generation.model.InjectorConfigurator
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.Scope
import io.michaelrocks.lightsaber.processor.model.isConstructorProvider
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type

class InjectorConfiguratorClassGenerator(
    private val classRegistry: ClassRegistry,
    private val annotationCreator: AnnotationCreator,
    private val generationConfiguration: GenerationConfiguration,
    private val injectorConfigurator: InjectorConfigurator
) {
  companion object {
    private val KEY_CONSTRUCTOR = MethodDescriptor.forConstructor(Types.CLASS_TYPE, Types.ANNOTATION_TYPE)
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

    generateConstructor(classVisitor)
    generateConfigureInjectorMethod(classVisitor)

    classVisitor.visitEnd()
    return classWriter.toByteArray()
  }

  private fun generateConstructor(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, MethodDescriptor.forDefaultConstructor())
    generator.visitCode()
    generator.loadThis()
    generator.invokeConstructor(Types.OBJECT_TYPE, MethodDescriptor.forDefaultConstructor())
    generator.returnValue()
    generator.endMethod()
  }

  private fun generateConfigureInjectorMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC, CONFIGURE_INJECTOR_METHOD)
    generator.visitCode()

    val moduleLocal: Int
    if (isModuleArgumentUsed()) {
      generator.loadArg(1)
      generator.checkCast(injectorConfigurator.module.type)
      moduleLocal = generator.newLocal(injectorConfigurator.module.type)
      generator.storeLocal(moduleLocal)
    } else {
      moduleLocal = INVALID_LOCAL
    }

    for (provider in injectorConfigurator.module.providers) {
      generateRegisterProviderInvocation(generator, provider, moduleLocal)
    }

    generator.returnValue()
    generator.endMethod()
  }

  private fun isModuleArgumentUsed(): Boolean = injectorConfigurator.module.providers.any { !it.isConstructorProvider }

  private fun generateRegisterProviderInvocation(generator: GeneratorAdapter, provider: Provider, moduleLocal: Int) {
    generator.loadArg(0)
    generateKeyConstruction(generator, provider)

    when (provider.scope) {
      is Scope.Class -> generateDelegatorConstruction(generator, provider, moduleLocal, provider.scope.scopeType)
      is Scope.None -> generateProviderConstruction(generator, provider, moduleLocal)
    }

    generator.invokeVirtual(LightsaberTypes.LIGHTSABER_INJECTOR_TYPE, REGISTER_PROVIDER_METHOD)
  }

  private fun generateKeyConstruction(generator: GeneratorAdapter, provider: Provider) {
    generator.newInstance(Types.KEY_TYPE)
    generator.dup()
    val providableType = provider.dependency.type.rawType
    val packageInvader = generationConfiguration.findPackageInvaderByTargetType(injectorConfigurator.module.type)!!
    val classField = packageInvader.getClassField(providableType.box()) ?:
        error("Cannot find class field for type: %s".format(providableType))

    generator.getStatic(packageInvader.type, classField)
    val qualifier = provider.dependency.qualifier
    if (qualifier == null) {
      generator.pushNull()
    } else {
      annotationCreator.newAnnotation(generator, qualifier)
    }
    generator.invokeConstructor(Types.KEY_TYPE, KEY_CONSTRUCTOR)
  }

  private fun generateDelegatorConstruction(generator: GeneratorAdapter, provider: Provider,
      moduleLocal: Int, scopeType: Type) {
    generator.newInstance(scopeType)
    generator.dup()
    generateProviderConstruction(generator, provider, moduleLocal)
    generator.invokeConstructor(scopeType, DELEGATE_PROVIDER_CONSTRUCTOR)
  }

  private fun generateProviderConstruction(generator: GeneratorAdapter, provider: Provider,
      moduleLocal: Int) {
    generator.newInstance(provider.type)
    generator.dup()
    if (moduleLocal == INVALID_LOCAL) {
      generator.loadArg(0)
      val constructor = MethodDescriptor.forConstructor(Types.INJECTOR_TYPE)
      generator.invokeConstructor(provider.type, constructor)
    } else {
      generator.loadLocal(moduleLocal)
      generator.loadArg(0)
      val constructor = MethodDescriptor.forConstructor(provider.moduleType, Types.INJECTOR_TYPE)
      generator.invokeConstructor(provider.type, constructor)
    }
  }
}
