/*
 * Copyright 2020 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.processor.analysis

import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.and
import io.michaelrocks.grip.annotatedWith
import io.michaelrocks.grip.fields
import io.michaelrocks.grip.from
import io.michaelrocks.grip.isStatic
import io.michaelrocks.grip.methodType
import io.michaelrocks.grip.methods
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getMethodType
import io.michaelrocks.grip.mirrors.getObjectTypeByInternalName
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.grip.not
import io.michaelrocks.grip.or
import io.michaelrocks.grip.returns
import io.michaelrocks.lightsaber.processor.ProcessingException
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.contains
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.commons.toFieldDescriptor
import io.michaelrocks.lightsaber.processor.commons.toMethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.Binding
import io.michaelrocks.lightsaber.processor.model.Converter
import io.michaelrocks.lightsaber.processor.model.Dependency
import io.michaelrocks.lightsaber.processor.model.Factory
import io.michaelrocks.lightsaber.processor.model.Injectee
import io.michaelrocks.lightsaber.processor.model.InjectionPoint
import io.michaelrocks.lightsaber.processor.model.InjectionTarget
import io.michaelrocks.lightsaber.processor.model.Module
import io.michaelrocks.lightsaber.processor.model.Provider
import io.michaelrocks.lightsaber.processor.model.ProvisionPoint
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SYNTHETIC

interface ModuleParser {
  fun parseModule(
    type: Type.Object,
    importeeModuleTypes: Collection<Type.Object>,
    providableTargets: Collection<InjectionTarget>,
    factories: Collection<Factory>,
    moduleRegistry: ModuleRegistry
  ): Module
}

class ModuleParserImpl(
  private val grip: Grip,
  private val moduleProviderParser: ModuleProviderParser,
  private val bindingRegistry: BindingRegistry,
  private val analyzerHelper: AnalyzerHelper,
  private val projectName: String
) : ModuleParser {

  private val logger = getLogger()

  private val bridgeRegistry = BridgeRegistry()

  override fun parseModule(
    type: Type.Object,
    importeeModuleTypes: Collection<Type.Object>,
    providableTargets: Collection<InjectionTarget>,
    factories: Collection<Factory>,
    moduleRegistry: ModuleRegistry
  ): Module {
    return parseModule(grip.classRegistry.getClassMirror(type), importeeModuleTypes, providableTargets, factories, moduleRegistry)
  }

  private fun parseModule(
    mirror: ClassMirror,
    importeeModuleTypes: Collection<Type.Object>,
    providableTargets: Collection<InjectionTarget>,
    factories: Collection<Factory>,
    moduleRegistry: ModuleRegistry
  ): Module {
    if (mirror.signature.typeVariables.isNotEmpty()) {
      throw ModuleParserException("Module cannot have a type parameters: ${mirror.type.className}")
    }

    val isComponentDefaultModule = when {
      Types.COMPONENT_TYPE in mirror.annotations -> true
      Types.MODULE_TYPE in mirror.annotations -> false
      else -> throw ModuleParserException("Class ${mirror.type.className} is neither a component nor a module")
    }

    val moduleProviders = moduleProviderParser.parseModuleProviders(mirror, moduleRegistry, importeeModuleTypes, isComponentDefaultModule)

    bridgeRegistry.clear()
    mirror.methods.forEach { bridgeRegistry.reserveMethod(it.toMethodDescriptor()) }

    val providers = createProviders(mirror, providableTargets, factories)
    return Module(mirror.type, moduleProviders, providers, factories)
  }

  private fun createProviders(
    module: ClassMirror,
    providableTargets: Collection<InjectionTarget>,
    factories: Collection<Factory>
  ): Collection<Provider> {
    val isProvidable = (annotatedWith(Types.PROVIDES_TYPE) or annotatedWith(Types.PROVIDE_TYPE)) and not(isStatic())
    val methodsQuery = grip select methods from module where (isProvidable and methodType(not(returns(Type.Primitive.Void))))
    val fieldsQuery = grip select fields from module where isProvidable

    logger.debug("Module: {}", module.type.className)
    val constructorProviders = providableTargets.map { target ->
      logger.debug("  Constructor: {}", target.injectionPoints.first())
      newConstructorProvider(target.type, target)
    }

    val methodProviders = methodsQuery.execute()[module.type].orEmpty().mapIndexed { index, method ->
      logger.debug("  Method: {}", method)
      newMethodProvider(module.type, method, index)
    }

    val fieldProviders = fieldsQuery.execute()[module.type].orEmpty().mapIndexed { index, field ->
      logger.debug("  Field: {}", field)
      newFieldProvider(module.type, field, index)
    }

    val directProviders = constructorProviders + methodProviders + fieldProviders
    val moduleBindings = directProviders.flatMap { provider ->
      bindingRegistry.findBindingsByDependency(provider.dependency).map { binding ->
        provider to binding
      }
    }

    val bindingProviders = moduleBindings.mapIndexed { index, (provider, binding) ->
      logger.debug("  Binding: {} -> {}", binding.dependency, binding.ancestor)
      newBindingProvider(module.type, provider, binding, index)
    }

    val factoryProviders = factories.map { factory ->
      logger.debug("  Factory: {}", factory)
      newFactoryProvider(module.type, factory)
    }

    val providerCount = constructorProviders.size + methodProviders.size + fieldProviders.size + bindingProviders.size + factoryProviders.size
    return ArrayList<Provider>(providerCount).apply {
      addAll(constructorProviders)
      addAll(methodProviders)
      addAll(fieldProviders)
      addAll(bindingProviders)
      addAll(factoryProviders)
    }
  }

  private fun newConstructorProvider(container: Type.Object, target: InjectionTarget): Provider {
    val mirror = grip.classRegistry.getClassMirror(target.type)
    val providerType = getObjectTypeByInternalName("${target.type.internalName}\$ConstructorProvider\$$projectName")
    val dependency = Dependency(GenericType.Raw(target.type), analyzerHelper.findQualifier(mirror))
    val injectionPoint = target.injectionPoints.first() as InjectionPoint.Method
    val provisionPoint = ProvisionPoint.Constructor(dependency, injectionPoint)
    val scope = analyzerHelper.findScope(mirror)
    return Provider(providerType, provisionPoint, container, scope)
  }

  private fun newMethodProvider(container: Type.Object, method: MethodMirror, index: Int): Provider {
    val providerType = getObjectTypeByInternalName("${container.internalName}\$MethodProvider\$$index\$$projectName")
    val dependency = Dependency(method.signature.returnType, analyzerHelper.findQualifier(method))
    val injectionPoint = analyzerHelper.convertToInjectionPoint(method, container)
    val provisionPoint = ProvisionPoint.Method(dependency, injectionPoint, null).withBridge()
    val scope = analyzerHelper.findScope(method)
    return Provider(providerType, provisionPoint, container, scope)
  }

  private fun newFieldProvider(container: Type.Object, field: FieldMirror, index: Int): Provider {
    val providerType = getObjectTypeByInternalName("${container.internalName}\$FieldProvider\$$index\$$projectName")
    val dependency = Dependency(field.signature.type, analyzerHelper.findQualifier(field))
    val provisionPoint = ProvisionPoint.Field(container, dependency, null, field).withBridge()
    val scope = analyzerHelper.findScope(field)
    return Provider(providerType, provisionPoint, container, scope)
  }

  private fun newBindingProvider(container: Type.Object, provider: Provider, binding: Binding, index: Int): Provider {
    val bindingType = binding.dependency.type.rawType as Type.Object
    val providerType = getObjectTypeByInternalName("${bindingType.internalName}\$BindingProvider\$$index\$$projectName")
    val provisionPoint = ProvisionPoint.Binding(container, binding.ancestor, binding.dependency)
    return Provider(providerType, provisionPoint, container, provider.scope)
  }

  private fun newFactoryProvider(container: Type.Object, factory: Factory): Provider {
    val mirror = grip.classRegistry.getClassMirror(factory.type)
    val providerType = getObjectTypeByInternalName("${factory.type.internalName}\$FactoryProvider\$$projectName")
    val constructorMirror = MethodMirror.Builder()
      .access(ACC_PUBLIC)
      .name(MethodDescriptor.CONSTRUCTOR_NAME)
      .type(getMethodType(Type.Primitive.Void, Types.INJECTOR_TYPE))
      .build()
    val constructorInjectee = Injectee(Dependency(GenericType.Raw(Types.INJECTOR_TYPE)), Converter.Instance)
    val injectionPoint = InjectionPoint.Method(factory.implementationType, constructorMirror, listOf(constructorInjectee))
    val provisionPoint = ProvisionPoint.Constructor(factory.dependency, injectionPoint)
    val scope = analyzerHelper.findScope(mirror)
    return Provider(providerType, provisionPoint, container, scope)
  }

  private fun ProvisionPoint.Method.withBridge(): ProvisionPoint.Method {
    val method = injectionPoint.method
    if (ACC_PRIVATE !in method.access) {
      return this
    }

    val bridgeMethod = bridgeRegistry.addBridge(method)
    val bridgeInjectionPoint = injectionPoint.copy(method = bridgeMethod)
    val bridgeProvisionPoint = ProvisionPoint.Method(dependency, bridgeInjectionPoint, null)
    return copy(bridge = bridgeProvisionPoint)
  }

  private fun ProvisionPoint.Field.withBridge(): ProvisionPoint.Field {
    val field = field
    if (ACC_PRIVATE !in field.access) {
      return this
    }

    val bridgeMethod = bridgeRegistry.addBridge(field)
    val bridgeInjectionPoint = InjectionPoint.Method(containerType, bridgeMethod, listOf())
    val bridgeProvisionPoint = ProvisionPoint.Method(dependency, bridgeInjectionPoint, null)
    return copy(bridge = bridgeProvisionPoint)
  }

  private fun BridgeRegistry.addBridge(method: MethodMirror): MethodMirror {
    val bridge = addBridge(method.toMethodDescriptor())
    return createBridgeMirror(bridge)
  }

  private fun BridgeRegistry.addBridge(field: FieldMirror): MethodMirror {
    val bridge = addBridge(field.toFieldDescriptor())
    return createBridgeMirror(bridge)
  }

  private fun createBridgeMirror(bridge: MethodDescriptor): MethodMirror {
    return MethodMirror.Builder()
      .access(ACC_PUBLIC or ACC_SYNTHETIC)
      .name(bridge.name)
      .type(bridge.type)
      .build()
  }
}

class ModuleParserException(message: String) : ProcessingException(message)
