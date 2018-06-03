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

import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

class InjectorInjectionTest {
  @Test
  fun testInjectorInjection() {
    val lightsaber = Lightsaber.get()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    assertSame(parentInjector, parentInjector.getInstance<InjectionTarget>().injector)
    assertSame(childInjector, childInjector.getInstance<InjectionTarget>().injector)
  }

  @Test
  fun testInjectorInjectionWithSingletonTarget() {
    val lightsaber = Lightsaber.get()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    val childTarget = childInjector.getInstance<SingletonInjectionTarget>()
    val parentTarget = parentInjector.getInstance<SingletonInjectionTarget>()
    assertNotSame(childTarget, parentTarget)
    assertSame(childInjector, childTarget.injector)
    assertSame(parentInjector, parentTarget.injector)
  }

  @Component
  private class ParentComponent {
    @Provides
    private fun provideParentModule(): ParentModule = ParentModule()
  }

  @Module
  private class ParentModule

  @Component(parent = ParentComponent::class)
  private class ChildComponent

  @ProvidedBy(ParentModule::class)
  private class InjectionTarget @Inject private constructor(val injector: Injector)

  @ProvidedBy(ParentModule::class)
  @Singleton
  private class SingletonInjectionTarget @Inject private constructor(val injector: Injector)
}
