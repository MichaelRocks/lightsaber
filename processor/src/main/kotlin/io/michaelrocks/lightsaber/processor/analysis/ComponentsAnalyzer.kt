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

package io.michaelrocks.lightsaber.processor.analysis

import io.michaelrocks.grip.*
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.FieldMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.grip.mirrors.signature.GenericType
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.analysis.ComponentsAnalyzer.Result
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.cast
import io.michaelrocks.lightsaber.processor.logging.getLogger
import io.michaelrocks.lightsaber.processor.model.*
import org.objectweb.asm.Type
import java.io.File
import java.util.*

private val PACKAGE_COMPONENT_TYPE = Types.BOXED_VOID_TYPE
private val PACKAGE_MODULE_CLASS_NAME = "Lightsaber\$PackageModule"

interface ComponentsAnalyzer {
  fun analyze(files: Collection<File>, providableTargets: Collection<InjectionTarget>): Result

  data class Result(
      val packageComponent: Component,
      val components: Collection<Component>
  )
}

class ComponentsAnalyzerImpl(
    private val grip: Grip,
    private val analyzerHelper: AnalyzerHelper,
    private val errorReporter: ErrorReporter
) : ComponentsAnalyzer {
  private val logger = getLogger()

  private val moduleRegistry: ModuleRegistry = ModuleRegistryImpl(grip, analyzerHelper, errorReporter)

  override fun analyze(files: Collection<File>, providableTargets: Collection<InjectionTarget>): Result {
    val components = analyzeComponents(files)
    val packageComponent = composePackageComponent(providableTargets, components)
    return Result(packageComponent, components)
  }

  private fun analyzeComponents(files: Collection<File>): Collection<Component> {
    val componentsQuery = grip select classes from files where annotatedWith(Types.COMPONENT_TYPE)
    return componentsQuery.execute().keys.map { it.toComponent() }
  }

  private fun Type.toComponent(): Component =
      convertToComponent(grip.classRegistry.getClassMirror(this))

  private fun convertToComponent(mirror: ClassMirror): Component {
    if (mirror.signature.typeParameters.isNotEmpty()) {
      errorReporter.reportError("Component cannot have a type parameters: $mirror")
      return Component(mirror.type, false, emptyList(), emptyList())
    }

    val annotation = mirror.annotations[Types.COMPONENT_TYPE]
    if (annotation == null) {
      errorReporter.reportError("Class $mirror is not a component")
      return Component(mirror.type, false, emptyList(), emptyList())
    }

    val root = annotation.values["root"] as Boolean

    val methodsQuery = grip select methods from mirror where
        (annotatedWith(Types.PROVIDES_TYPE) and type(not(returns(Type.VOID_TYPE))) and not(isStatic()))
    val fieldsQuery = grip select fields from mirror where
        (annotatedWith(Types.PROVIDES_TYPE) and not(isStatic()))

    logger.debug("Component: {}", mirror)
    val methods = methodsQuery.execute()[mirror.type].orEmpty().mapIndexed { index, method ->
      logger.debug("  Method: {}", method)
      ModuleProvider(method.toModule(), ModuleProvisionPoint.Method(method))
    }

    val fields = fieldsQuery.execute()[mirror.type].orEmpty().mapIndexed { index, field ->
      logger.debug("  Field: {}", field)
      ModuleProvider(field.toModule(), ModuleProvisionPoint.Field(field))
    }

    val subcomponents = mirror
        .annotations[Types.COMPONENT_TYPE]!!
        .values["subcomponents"]!!
        .cast<List<Type>>()

    return Component(mirror.type, root, methods + fields, subcomponents)
  }

  private fun MethodMirror.toModule(): Module =
      signature.returnType.toModule()

  private fun FieldMirror.toModule(): Module =
      signature.type.toModule()

  private fun GenericType.toModule(): Module {
    if (this !is GenericType.RawType) {
      errorReporter.reportError("Module provider cannot have a generic type: $this")
      return Module(Types.OBJECT_TYPE, emptyList())
    }

    return moduleRegistry.getOrCreateModule(type)
  }

  private fun composePackageComponent(
      providableTargets: Iterable<InjectionTarget>,
      components: Collection<Component>
  ): Component {
    val providers = composePackageModules(providableTargets)
        .map { ModuleProvider(it, ModuleProvisionPoint.Null) }
    val subcomponents = components.filter { it.root }.map { it.type }
    return Component(PACKAGE_COMPONENT_TYPE, true, providers, subcomponents)
  }

  private fun composePackageModules(providableTargets: Iterable<InjectionTarget>): List<Module> {
    val injectionTargetsByPackageName = providableTargets.groupByTo(HashMap()) {
      it.type.internalName.substringBeforeLast('/', "")
    }
    return injectionTargetsByPackageName.entries.map {
      val (packageName, injectionTargets) = it
      val providers = injectionTargets.map {
        it.injectionPoints.first().toProvider(grip.classRegistry.getClassMirror(it.type))
      }
      val moduleType = composePackageModuleType(packageName)
      Module(moduleType, providers)
    }
  }

  private fun InjectionPoint.toProvider(container: ClassMirror): Provider {
    val providerType = Type.getObjectType("${containerType.internalName}\$ConstructorProvider")
    val dependency = Dependency(GenericType.RawType(containerType), null)
    val provisionPoint = ProvisionPoint.Constructor(dependency, cast<InjectionPoint.Method>())
    return Provider(providerType, provisionPoint, container.type, analyzerHelper.findScope(container))
  }

  private fun composePackageModuleType(packageName: String): Type {
    val name = if (packageName.isEmpty()) PACKAGE_MODULE_CLASS_NAME else "$packageName/$PACKAGE_MODULE_CLASS_NAME"
    return Type.getObjectType(name)
  }
}
