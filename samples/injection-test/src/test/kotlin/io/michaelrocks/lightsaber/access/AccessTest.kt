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

package io.michaelrocks.lightsaber.access

import io.michaelrocks.lightsaber.Key
import io.michaelrocks.lightsaber.Lightsaber
import org.junit.Test

@InternalQualifier
class AccessTest {
  @Test
  fun testInjectionAccess() {
    val module = AccessModule()
    val injector = Lightsaber.get().createInjector(module)
    val target = injector.getInstance(Key.of<InternalDependency>(InternalDependency::class.java))
    target.action()
  }

  @Test
  fun testInjectionAccessWithQualifier() {
    val module = AccessModule()
    val injector = Lightsaber.get().createInjector(module)
    val qualifier = javaClass.getAnnotation(InternalQualifier::class.java)
    val target = injector.getInstance(Key.of<InternalDependency>(InternalDependency::class.java, qualifier))
    target.action()
  }
}
