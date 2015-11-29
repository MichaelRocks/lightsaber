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

import io.michaelrocks.lightsaber.processor.analysis.Analyzer
import io.michaelrocks.lightsaber.processor.annotations.proxy.AnnotationCreator
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.box
import io.michaelrocks.lightsaber.processor.descriptors.*
import io.michaelrocks.lightsaber.processor.generation.*
import io.michaelrocks.lightsaber.processor.graph.CycleSearcher
import io.michaelrocks.lightsaber.processor.graph.DependencyGraph
import io.michaelrocks.lightsaber.processor.graph.UnresolvedDependenciesSearcher
import io.michaelrocks.lightsaber.processor.injection.InjectionClassFileVisitor
import io.michaelrocks.lightsaber.processor.io.ClassFileReader
import io.michaelrocks.lightsaber.processor.io.ClassFileWriter
import io.michaelrocks.lightsaber.processor.validation.SanityChecker
import org.apache.commons.lang3.exception.ExceptionUtils
import org.objectweb.asm.Type
import java.io.File
import java.io.IOException
import java.util.*

class ClassProcessor(
    private val classFileReader: ClassFileReader,
    private val classFileWriter: ClassFileWriter,
    libraries: List<File>
) {
  private val libraries = ArrayList(libraries)

  private val processorContext = ProcessorContext()
  private val classProducer = ProcessorClassProducer(classFileWriter, processorContext)
  private val annotationCreator = AnnotationCreator(processorContext, classProducer)

  @Throws(IOException::class)
  fun processClasses() {
    performAnalysis()
    composePackageModules()
    composeInjectors()
    composePackageInvaders()
    processorContext.dump()
    validateDependencyGraph()
    generateProviders()
    generateLightsaberConfigurator()
    generateInjectorConfigurators()
    generateInjectors()
    generatePackageInvaders()
    copyAndPatchClasses()
  }

  @Throws(IOException::class)
  private fun performAnalysis() {
    val analyzer = Analyzer(processorContext)
    analyzer.analyze(classFileReader, libraries)
    SanityChecker(processorContext).performSanityChecks()
    checkErrors()
  }

  private fun composePackageModules() {
    val moduleBuilders = HashMap<String, ModuleDescriptor.Builder>()
    for (providableTarget in processorContext.getProvidableTargets()) {
      val providableTargetType = providableTarget.targetType
      val providableTargetConstructor = providableTarget.injectableConstructor!!

      val packageName = providableTargetType.internalName.substringBeforeLast('/', "")
      val moduleBuilder = moduleBuilders[packageName] ?:
          ModuleDescriptor.Builder(processorContext.getPackageModuleType(packageName)).apply {
            moduleBuilders.put(packageName, this)
          }

      val providerType = Type.getObjectType(
          providableTargetType.internalName + "\$Provider")
      val providableType = QualifiedType(providableTarget.targetType)
      val scope = providableTarget.scope
      val delegatorType = scope?.providerType
      val provider = ProviderDescriptor(
          providerType, providableType, providableTargetConstructor, moduleBuilder.moduleType, delegatorType)

      moduleBuilder.addProvider(provider)
    }

    for (moduleBuilder in moduleBuilders.values) {
      processorContext.addPackageModule(moduleBuilder.build())
    }
  }

  private fun composeInjectors() {
    for (injectableTarget in processorContext.getInjectableTargets()) {
      val injectorType = Type.getObjectType(injectableTarget.targetType.internalName + "\$MembersInjector")
      val injector = InjectorDescriptor(injectorType, injectableTarget)
      processorContext.addInjector(injector)
    }
  }

  private fun composePackageInvaders() {
    val builders = HashMap<String, PackageInvaderDescriptor.Builder>()
    for (module in processorContext.allModules) {
      for (provider in module.providers) {
        val providableType = provider.providableType.box()
        val packageName = Types.getPackageName(module.moduleType)
        val builder = builders[packageName] ?: PackageInvaderDescriptor.Builder(packageName).apply {
          builders.put(packageName, this)
        }
        builder.addClass(providableType)
      }
    }

    for (builder in builders.values) {
      processorContext.addPackageInvader(builder.build())
    }
  }

  @Throws(ProcessingException::class)
  private fun validateDependencyGraph() {
    val dependencyGraph = DependencyGraph(processorContext, processorContext.allModules)

    val unresolvedDependenciesSearcher = UnresolvedDependenciesSearcher(dependencyGraph)
    val unresolvedDependencies = unresolvedDependenciesSearcher.findUnresolvedDependencies()
    for (unresolvedDependency in unresolvedDependencies) {
      processorContext.reportError(
          ProcessingException("Unresolved dependency: " + unresolvedDependency))
    }

    val cycleSearcher = CycleSearcher(dependencyGraph)
    val cycles = cycleSearcher.findCycles()
    for (cycle in cycles) {
      processorContext.reportError(
          ProcessingException("Cycled dependency: " + cycle))
    }

    checkErrors()
  }

  @Throws(ProcessingException::class)
  private fun generateProviders() {
    val providersGenerator = ProvidersGenerator(classProducer, processorContext, annotationCreator)
    providersGenerator.generateProviders()
    checkErrors()
  }

  @Throws(ProcessingException::class)
  private fun generateLightsaberConfigurator() {
    val lightsaberRegistryClassGenerator = LightsaberRegistryClassGenerator(classProducer, processorContext)
    lightsaberRegistryClassGenerator.generateLightsaberRegistry()
    checkErrors()
  }

  @Throws(ProcessingException::class)
  private fun generateInjectorConfigurators() {
    val injectorConfiguratorsGenerator = InjectorConfiguratorsGenerator(classProducer, processorContext,
        annotationCreator)
    injectorConfiguratorsGenerator.generateInjectorConfigurators()
    checkErrors()
  }

  @Throws(ProcessingException::class)
  private fun generateInjectors() {
    val typeAgentsGenerator = TypeAgentsGenerator(classProducer, processorContext, annotationCreator)
    typeAgentsGenerator.generateInjectors()
    checkErrors()
  }

  @Throws(ProcessingException::class)
  private fun generatePackageInvaders() {
    val packageInvadersGenerator = PackageInvadersGenerator(classProducer, processorContext)
    packageInvadersGenerator.generatePackageInvaders()
    checkErrors()
  }

  @Throws(IOException::class)
  private fun copyAndPatchClasses() {
    val injectionVisitor = InjectionClassFileVisitor(classFileWriter, processorContext)
    classFileReader.accept(injectionVisitor)
    checkErrors()
  }

  @Throws(ProcessingException::class)
  private fun checkErrors() {
    if (processorContext.hasErrors()) {
      throw ProcessingException(composeErrorMessage())
    }
  }

  private fun composeErrorMessage(): String {
    return buildString {
      for (entry in processorContext.errors.entries) {
        val path = entry.key
        for (error in entry.value) {
          append(System.lineSeparator())
          append(path)
          append(": ")
          append(error.message)
          append(System.lineSeparator())
          append(ExceptionUtils.getStackTrace(error))
        }
      }
    }
  }
}
