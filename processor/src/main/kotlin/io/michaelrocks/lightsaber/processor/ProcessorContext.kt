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

package io.michaelrocks.lightsaber.processor

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.Grip
import io.michaelrocks.grip.GripFactory
import io.michaelrocks.lightsaber.LightsaberTypes
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.generation.model.MembersInjector
import io.michaelrocks.lightsaber.processor.generation.model.PackageInvader
import io.michaelrocks.lightsaber.processor.io.FileSink
import io.michaelrocks.lightsaber.processor.io.FileSource
import io.michaelrocks.lightsaber.processor.io.IoFactory
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.*
import org.apache.commons.collections4.CollectionUtils
import org.objectweb.asm.Type
import java.io.File
import java.util.*

class ProcessorContext(
    val inputFile: File,
    val outputFile: File,
    libraries: List<File>
) {

  companion object {
    private val PACKAGE_MODULE_CLASS_NAME = "Lightsaber\$PackageModule"
    private val SINGLETON_SCOPE = Scope.Class(LightsaberTypes.SINGLETON_PROVIDER_TYPE)
  }

  private val logger = getLogger()

  var classFilePath: String? = null
  private val errorsByPath = LinkedHashMap<String, MutableList<Exception>>()

  val fileSourceFactory: FileSource.Factory = IoFactory
  val fileSinkFactory: FileSink.Factory = IoFactory
  val grip: Grip = GripFactory.create(listOf(inputFile) + libraries)
  val classRegistry: ClassRegistry
    get() = grip.classRegistry

  private val modules = HashMap<Type, Module>()
  private val packageModules = HashMap<Type, Module>()
  private val injectableTargets = HashMap<Type, InjectionTarget>()
  private val providableTargets = HashMap<Type, InjectionTarget>()
  private val injectors = HashMap<Type, MembersInjector>()
  private val packageInvaders = HashMap<String, PackageInvader>()

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

  fun findModuleByType(moduleType: Type): Module? {
    return modules[moduleType]
  }

  fun getModules(): Collection<Module> {
    return Collections.unmodifiableCollection(modules.values)
  }

  fun addModule(module: Module) {
    modules.put(module.type, module)
  }

  fun getPackageModules(): Collection<Module> {
    return Collections.unmodifiableCollection(packageModules.values)
  }

  fun addPackageModule(packageModule: Module) {
    packageModules.put(packageModule.type, packageModule)
  }

  val allModules: Collection<Module>
    get() = CollectionUtils.union(modules.values, packageModules.values)

  fun findInjectableTargetByType(injectableTargetType: Type): InjectionTarget? {
    return injectableTargets[injectableTargetType]
  }

  fun getInjectableTargets(): Collection<InjectionTarget> {
    return Collections.unmodifiableCollection(injectableTargets.values)
  }

  fun addInjectableTarget(injectableTarget: InjectionTarget) {
    injectableTargets.put(injectableTarget.type, injectableTarget)
  }

  fun findProvidableTargetByType(providableTargetType: Type): InjectionTarget? {
    return providableTargets[providableTargetType]
  }

  fun getProvidableTargets(): Collection<InjectionTarget> {
    return Collections.unmodifiableCollection(providableTargets.values)
  }

  fun addProvidableTarget(providableTarget: InjectionTarget) {
    providableTargets.put(providableTarget.type, providableTarget)
  }

  fun findInjectorByTargetType(targetType: Type): MembersInjector? {
    return injectors[targetType]
  }

  fun getMembersInjectors(): Collection<MembersInjector> {
    return Collections.unmodifiableCollection(injectors.values)
  }

  fun addMembersInjector(injector: MembersInjector) {
    injectors.put(injector.target.type, injector)
  }

  fun findPackageInvaderByTargetType(targetType: Type): PackageInvader? {
    return findPackageInvaderByPackageName(Types.getPackageName(targetType))
  }

  fun findPackageInvaderByPackageName(packageName: String): PackageInvader? {
    return packageInvaders[packageName]
  }

  fun getPackageInvaders(): Collection<PackageInvader> {
    return Collections.unmodifiableCollection(packageInvaders.values)
  }

  fun addPackageInvader(packageInvader: PackageInvader) {
    packageInvaders.put(packageInvader.packageName, packageInvader)
  }

  fun findScopeByAnnotationType(annotationType: Type): Scope? {
    return when (annotationType) {
      Types.SINGLETON_TYPE -> SINGLETON_SCOPE
      else -> null
    }
  }

  fun isQualifier(annotationType: Type): Boolean {
    return classRegistry.getClassMirror(annotationType).annotations.contains(Types.QUALIFIER_TYPE)
  }

  fun getPackageModuleType(packageName: String): Type {
    val name = if (packageName.isEmpty()) PACKAGE_MODULE_CLASS_NAME else "$packageName/$PACKAGE_MODULE_CLASS_NAME"
    return Type.getObjectType(name)
  }

  fun dump() {
    for (module in getModules()) {
      logger.debug("Module: {}", module.type)
      for (provider in module.providers) {
        if (provider.provisionPoint is ProvisionPoint.AbstractMethod) {
          logger.debug("\tProvides: {}", provider.provisionPoint.method)
        } else if (provider.provisionPoint is ProvisionPoint.Field) {
          logger.debug("\tProvides: {}", provider.provisionPoint.field)
        } else {
          logger.debug("\tProvides: {}", provider.provisionPoint)
        }
      }
    }
    for (module in getPackageModules()) {
      logger.debug("Package module: {}", module.type)
      for (provider in module.providers) {
        when (provider.provisionPoint) {
          is ProvisionPoint.AbstractMethod -> logger.debug("\tProvides: {}", provider.provisionPoint.method)
          is ProvisionPoint.Field -> logger.debug("\tProvides: {}", provider.provisionPoint.field)
          else -> logger.debug("\tProvides: {}", provider.provisionPoint)
        }
      }
    }
    for (injectableTarget in getInjectableTargets()) {
      logger.debug("Injectable: {}", injectableTarget.type)
      for (injectionPoint in injectableTarget.injectionPoints) {
        when (injectionPoint) {
          is InjectionPoint.Field -> logger.debug("\tField: {}", injectionPoint.field)
          is InjectionPoint.Method -> logger.debug("\tMethod: {}", injectionPoint.method)
        }
      }
    }
    for (providableTarget in getProvidableTargets()) {
      logger.debug("Providable: {}", providableTarget.type)
      for (injectionPoint in providableTarget.injectionPoints) {
        when (injectionPoint) {
          is InjectionPoint.Method -> logger.debug("\tConstructor: {}", injectionPoint.method)
        }
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
