/*
 * Copyright 2019 Michael Rozumyanskiy
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
import javax.inject.Singleton

class InjectorInjectionTest {
  @Test
  fun testInjectorFromParentInjectionTarget() {
    val lightsaber = Lightsaber.Builder().build()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    assertSame(parentInjector, parentInjector.getInstance<ParentInjectionTarget>().injector)
    assertSame(parentInjector, childInjector.getInstance<ParentInjectionTarget>().injector)
  }

  @Test
  fun testInjectorFromChildInjectionTarget() {
    val lightsaber = Lightsaber.Builder().build()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    assertSame(childInjector, childInjector.getInstance<ChildInjectionTarget>().injector)
  }

  @Test
  fun testInjectorFromSingletonParentInjectionTarget() {
    val lightsaber = Lightsaber.Builder().build()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    val parentTarget = parentInjector.getInstance<SingletonParentInjectionTarget>()
    val childTarget = childInjector.getInstance<SingletonParentInjectionTarget>()
    assertSame(parentTarget, childTarget)
    assertSame(parentInjector, parentTarget.injector)
    assertSame(parentInjector, childTarget.injector)
  }

  @Test
  fun testInjectorFromSingletonParentInjectionTargetChildFirst() {
    val lightsaber = Lightsaber.Builder().build()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    val childTarget = childInjector.getInstance<SingletonParentInjectionTarget>()
    val parentTarget = parentInjector.getInstance<SingletonParentInjectionTarget>()
    assertSame(parentTarget, childTarget)
    assertSame(parentInjector, parentTarget.injector)
    assertSame(parentInjector, childTarget.injector)
  }

  @Test
  fun testInjectorFromSingletonChildInjectionTarget() {
    val lightsaber = Lightsaber.Builder().build()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    val childTarget = childInjector.getInstance<SingletonChildInjectionTarget>()
    assertSame(childInjector, childTarget.injector)
  }

  @Component
  private class ParentComponent {
    @Provides
    private fun provideParentModule(): ParentModule = ParentModule()
  }

  @Module
  private class ParentModule

  @Component(parent = ParentComponent::class)
  private class ChildComponent {
    @Provides
    private fun provideChildModule(): ChildModule = ChildModule()
  }

  @Module
  private class ChildModule

  @ProvidedBy(ParentModule::class)
  private class ParentInjectionTarget @Inject private constructor(val injector: Injector)

  @ProvidedBy(ChildModule::class)
  private class ChildInjectionTarget @Inject private constructor(val injector: Injector)

  @ProvidedBy(ParentModule::class)
  @Singleton
  private class SingletonParentInjectionTarget @Inject private constructor(val injector: Injector)

  @ProvidedBy(ChildModule::class)
  @Singleton
  private class SingletonChildInjectionTarget @Inject private constructor(val injector: Injector)
}
