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

package io.michaelrocks.lightsaber.access

import io.michaelrocks.lightsaber.ProvidedBy
import javax.inject.Inject

@ProvidedBy(AccessModule::class)
internal class InternalDependencyImpl @Inject private constructor() : InternalDependency {
  override fun action() {
  }

  @Inject
  private fun privateMethod() {
  }

  @Inject
  fun internalMethod() {
  }

  @Inject
  protected fun protectedMethod() {
  }

  @Inject
  fun publicMethod() {
  }
}
