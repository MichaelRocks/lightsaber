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

package io.michaelrocks.lightsaber.processor

import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.annotations.AnnotationRegistryImpl
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.getType
import io.michaelrocks.lightsaber.processor.descriptors.*
import io.michaelrocks.lightsaber.processor.files.ClassRegistry
import io.michaelrocks.lightsaber.processor.files.ClassRegistryImpl
import io.michaelrocks.lightsaber.processor.files.FileRegistry
import io.michaelrocks.lightsaber.processor.files.FileRegistryImpl
import io.michaelrocks.lightsaber.processor.logging.getLogger
import org.apache.commons.collections4.CollectionUtils
import org.objectweb.asm.Type
import java.util.*
import javax.inject.Singleton

class ProcessorContext {
  companion object {
    private val PACKAGE_MODULE_CLASS_NAME = "Lightsaber\$PackageModule"
    private val SINGLETON_SCOPE_DESCRIPTOR =
        ScopeDescriptor(getType<Singleton>(), LightsaberTypes.SINGLETON_PROVIDER_TYPE)
  }

  private val logger = getLogger()

  var classFilePath: String? = null
  private val errorsByPath = LinkedHashMap<String, MutableList<Exception>>()

  val fileRegistry: FileRegistry = FileRegistryImpl()
  val annotationRegistry = AnnotationRegistryImpl(fileRegistry)
  val classRegistry: ClassRegistry = ClassRegistryImpl(fileRegistry, annotationRegistry)

  private val modules = HashMap<Type, ModuleDescriptor>()
  private val packageModules = HashMap<Type, ModuleDescriptor>()
  private val injectableTargets = HashMap<Type, InjectionTargetDescriptor>()
  private val providableTargets = HashMap<Type, InjectionTargetDescriptor>()
  private val injectors = HashMap<Type, InjectorDescriptor>()
  private val packageInvaders = HashMap<String, PackageInvaderDescriptor>()

  fun hasErrors(): Boolean {
    return !errorsByPath.isEmpty()
  }

  val errors: Map<String, List<Exception>>
    get() = Collections.unmodifiableMap(errorsByPath)

  fun reportError(errorMessage: String) {
    reportError(ProcessingException(errorMessage, classFilePath))
  }

  fun reportError(error: Exception) {
    var errors: MutableList<Exception>? = errorsByPath.get(classFilePath.orEmpty())
    if (errors == null) {
      errors = ArrayList<Exception>()
      errorsByPath.put(classFilePath.orEmpty(), errors)
    }
    errors.add(error)
  }

  fun findModuleByType(moduleType: Type): ModuleDescriptor? {
    return modules[moduleType]
  }

  fun getModules(): Collection<ModuleDescriptor> {
    return Collections.unmodifiableCollection(modules.values)
  }

  fun addModule(module: ModuleDescriptor) {
    modules.put(module.moduleType, module)
  }

  fun getPackageModules(): Collection<ModuleDescriptor> {
    return Collections.unmodifiableCollection(packageModules.values)
  }

  fun addPackageModule(packageModule: ModuleDescriptor) {
    packageModules.put(packageModule.moduleType, packageModule)
  }

  val allModules: Collection<ModuleDescriptor>
    get() = CollectionUtils.union(modules.values, packageModules.values)

  fun findInjectableTargetByType(injectableTargetType: Type): InjectionTargetDescriptor? {
    return injectableTargets[injectableTargetType]
  }

  fun getInjectableTargets(): Collection<InjectionTargetDescriptor> {
    return Collections.unmodifiableCollection(injectableTargets.values)
  }

  fun addInjectableTarget(injectableTarget: InjectionTargetDescriptor) {
    injectableTargets.put(injectableTarget.targetType, injectableTarget)
  }

  fun findProvidableTargetByType(providableTargetType: Type): InjectionTargetDescriptor? {
    return providableTargets[providableTargetType]
  }

  fun getProvidableTargets(): Collection<InjectionTargetDescriptor> {
    return Collections.unmodifiableCollection(providableTargets.values)
  }

  fun addProvidableTarget(providableTarget: InjectionTargetDescriptor) {
    providableTargets.put(providableTarget.targetType, providableTarget)
  }

  fun findInjectorByTargetType(targetType: Type): InjectorDescriptor? {
    return injectors[targetType]
  }

  fun getInjectors(): Collection<InjectorDescriptor> {
    return Collections.unmodifiableCollection(injectors.values)
  }

  fun addInjector(injector: InjectorDescriptor) {
    injectors.put(injector.injectableTarget.targetType, injector)
  }

  fun findPackageInvaderByTargetType(targetType: Type): PackageInvaderDescriptor? {
    return findPackageInvaderByPackageName(Types.getPackageName(targetType))
  }

  fun findPackageInvaderByPackageName(packageName: String): PackageInvaderDescriptor? {
    return packageInvaders[packageName]
  }

  fun getPackageInvaders(): Collection<PackageInvaderDescriptor> {
    return Collections.unmodifiableCollection(packageInvaders.values)
  }

  fun addPackageInvader(packageInvader: PackageInvaderDescriptor) {
    packageInvaders.put(packageInvader.packageName, packageInvader)
  }

  fun findScopeByAnnotationType(annotationType: Type): ScopeDescriptor? {
    if (SINGLETON_SCOPE_DESCRIPTOR.scopeAnnotationType == annotationType) {
      return SINGLETON_SCOPE_DESCRIPTOR
    }
    return null
  }

  val scopes: Collection<ScopeDescriptor>
    get() = setOf(SINGLETON_SCOPE_DESCRIPTOR)

  fun isQualifier(annotationType: Type): Boolean {
    return classRegistry.findClass(annotationType).annotations.any { it.type == Types.QUALIFIER_TYPE }
  }

  fun getPackageModuleType(packageName: String): Type {
    val name = if (packageName.isEmpty()) PACKAGE_MODULE_CLASS_NAME else "$packageName/$PACKAGE_MODULE_CLASS_NAME"
    return Type.getObjectType(name)
  }

  fun dump() {
    for (module in getModules()) {
      logger.debug("Module: {}", module.moduleType)
      for (provider in module.providers) {
        if (provider.providerMethod != null) {
          logger.debug("\tProvides: {}", provider.providerMethod)
        } else {
          logger.debug("\tProvides: {}", provider.providerField)
        }
      }
    }
    for (module in getPackageModules()) {
      logger.debug("Package module: {}", module.moduleType)
      for (provider in module.providers) {
        if (provider.providerMethod != null) {
          logger.debug("\tProvides: {}", provider.providerMethod)
        } else {
          logger.debug("\tProvides: {}", provider.providerField)
        }
      }
    }
    for (injectableTarget in getInjectableTargets()) {
      logger.debug("Injectable: {}", injectableTarget.targetType)
      for (injectableField in injectableTarget.injectableFields) {
        logger.debug("\tField: {}", injectableField)
      }
      for (injectableMethod in injectableTarget.injectableMethods) {
        logger.debug("\tMethod: {}", injectableMethod)
      }
    }
    for (providableTarget in getProvidableTargets()) {
      logger.debug("Providable: {}", providableTarget.targetType)
      for (injectableConstructor in providableTarget.injectableConstructors) {
        logger.debug("\tConstructor: {}", injectableConstructor)
      }
    }
    for (packageInvader in getPackageInvaders()) {
      logger.debug("Package invader: {} for package {}",
          packageInvader.type, packageInvader.packageName)
      for (entry in packageInvader.classFields.entries) {
        logger.debug("\tClass field: {} for class {}", entry.value.name, entry.key)
      }
    }
  }
}
