/*
 * Copyright 2018 Michael Rozumyanskiy
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
    private val errorReporter: ErrorReporter
) {
  private val injectionTargetAnalyzer: InjectionTargetsAnalyzer
  private val componentsAnalyzer: ComponentsAnalyzer

  init {
    val analyzerHelper = AnalyzerHelperImpl(grip.classRegistry, ScopeRegistry(), errorReporter)
    injectionTargetAnalyzer = InjectionTargetsAnalyzerImpl(grip, analyzerHelper, errorReporter)
    componentsAnalyzer = ComponentsAnalyzerImpl(grip, analyzerHelper, errorReporter)
  }

  fun analyze(files: Collection<File>): InjectionContext {
    val analyzerHelper: AnalyzerHelper = AnalyzerHelperImpl(grip.classRegistry, ScopeRegistry(), errorReporter)
    val (injectableTargets, providableTargets) =
        InjectionTargetsAnalyzerImpl(grip, analyzerHelper, errorReporter).let { analyzer ->
          analyzer.analyze(files)
        }
    val components = componentsAnalyzer.analyze(files, providableTargets)
    return InjectionContext(components, injectableTargets, providableTargets)
  }
}
