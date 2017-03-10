/*
 * Copyright 2017 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber

import org.junit.Assert.assertSame
import org.junit.Test
import javax.inject.Inject

class InjectorInjectionTest {
  @Test
  fun testInjectorInjection() {
    val lightsaber = Lightsaber.get()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    assertSame(parentInjector, parentInjector.getInstance<InjectionTarget>().injector)
    assertSame(childInjector, childInjector.getInstance<InjectionTarget>().injector)
  }

  @Component
  private class ParentComponent

  @Component(parent = ParentComponent::class)
  private class ChildComponent

  private class InjectionTarget @Inject private constructor(val injector: Injector)
}
