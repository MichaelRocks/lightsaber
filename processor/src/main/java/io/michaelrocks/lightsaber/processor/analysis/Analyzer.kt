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
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import java.io.File

class Analyzer(
  private val grip: Grip,
  private val errorReporter: ErrorReporter,
  private val projectName: String
) {

  fun analyze(files: Collection<File>): InjectionContext {
    val analyzerHelper = AnalyzerHelperImpl(grip.classRegistry, ScopeRegistry(), errorReporter)
    val (injectableTargets, providableTargets) = InjectionTargetsAnalyzerImpl(grip, analyzerHelper, errorReporter).analyze(files)
    val bindingRegistry = BindingsAnalyzerImpl(grip, analyzerHelper, errorReporter).analyze(files)
    val factories = FactoriesAnalyzerImpl(grip, analyzerHelper, errorReporter, projectName).analyze(files)
    val moduleProviderParser = ModuleProviderParserImpl(grip, errorReporter)
    val moduleParser = ModuleParserImpl(grip, moduleProviderParser, bindingRegistry, analyzerHelper, projectName)
    val moduleRegistry = ModuleRegistryImpl(grip, moduleParser, errorReporter, providableTargets, factories, files)
    val components = ComponentsAnalyzerImpl(grip, moduleRegistry, errorReporter).analyze(files)
    return InjectionContext(components, injectableTargets, providableTargets, factories, bindingRegistry.bindings)
  }
}
