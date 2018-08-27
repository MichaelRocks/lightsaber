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

package io.michaelrocks.lightsaber

import org.junit.Assert.assertEquals
import org.junit.Test
import javax.inject.Inject

class ProvidedByInjectionTest {
  @Test
  fun testProvidedByInjection() {
    val lightsaber = Lightsaber.get()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    assertEquals("ProvidedBy", parentInjector.getInstance<ParentInjectionTarget>().string)
    assertEquals("ProvidedBy", childInjector.getInstance<ParentInjectionTarget>().string)
    assertEquals("ProvidedBy", childInjector.getInstance<ChildInjectionTarget>().parent.string)
  }

  @Component
  private class ParentComponent {
    @Provides
    private fun provideParentModule(): ParentModule = ParentModule()

    @Module
    class ParentModule {
      @Provides
      private fun provideString(): String {
        return "ProvidedBy"
      }
    }
  }

  @Component(parent = ParentComponent::class)
  private class ChildComponent {
    @Provides
    private fun provideChildModule(): ChildModule = ChildModule()

    @Module
    class ChildModule
  }

  @ProvidedBy(ParentComponent.ParentModule::class)
  private class ParentInjectionTarget @Inject private constructor(val string: String)

  @ProvidedBy(ChildComponent.ChildModule::class)
  private class ChildInjectionTarget @Inject private constructor(val parent: ParentInjectionTarget)
}