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

package io.michaelrocks.lightsaber

import org.junit.Assert.assertEquals
import org.junit.Test
import javax.inject.Inject

class ProvidedByInjectionTest {
  @Test
  fun testProvidedByInjection() {
    val lightsaber = Lightsaber.Builder().build()
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = parentInjector.createChildInjector(ChildComponent())
    assertEquals("ProvidedBy", parentInjector.getInstance<ParentInjectionTarget>().string)
    assertEquals("ProvidedBy", childInjector.getInstance<ParentInjectionTarget>().string)
    assertEquals("ProvidedBy", childInjector.getInstance<ChildInjectionTarget>().parent.string)
  }

  @Test
  fun testMultipleProvidedByInjection() {
    val lightsaber = Lightsaber.Builder().build()
    val injector1 = lightsaber.createInjector(Component1())
    val injector2 = lightsaber.createInjector(Component2())

    val target1 = injector1.getInstance<MultipleTarget>()
    val target2 = injector2.getInstance<MultipleTarget>()

    assertEquals("Component1", target1.string)
    assertEquals("Component2", target2.string)
  }

  @Test
  fun testMultipleProvidedByFactoryInjection() {
    val lightsaber = Lightsaber.Builder().build()
    val injector1 = lightsaber.createInjector(Component1())
    val injector2 = lightsaber.createInjector(Component2())

    val factory1 = injector1.getInstance<MultipleFactory>()
    val factory2 = injector2.getInstance<MultipleFactory>()
    val target1 = factory1.create("Factory1")
    val target2 = factory2.create("Factory2")

    assertEquals("Component1", target1.string)
    assertEquals("Factory1", target1.assistedString)
    assertEquals("Component2", target2.string)
    assertEquals("Factory2", target2.assistedString)
  }

  @Component
  private class ParentComponent {

    @Import
    private fun importParentModule(): ParentModule = ParentModule()

    @Module
    class ParentModule {

      @Provide
      private fun provideString(): String {
        return "ProvidedBy"
      }
    }
  }

  @Component(parent = ParentComponent::class)
  private class ChildComponent {

    @Import
    private fun importChildModule(): ChildModule = ChildModule()

    @Module
    class ChildModule
  }

  @ProvidedBy(ParentComponent.ParentModule::class)
  private class ParentInjectionTarget @Inject private constructor(val string: String)

  @ProvidedBy(ChildComponent.ChildModule::class)
  private class ChildInjectionTarget @Inject private constructor(val parent: ParentInjectionTarget)

  @Component
  class Component1 {

    @Provide
    private val string: String = "Component1"
  }

  @Component
  class Component2 {

    @Provide
    private val string: String = "Component2"
  }

  @ProvidedBy(Component1::class, Component2::class)
  class MultipleTarget @Inject private constructor(
    val string: String
  )

  class MultipleFactoryTarget @Factory.Inject private constructor(
    val string: String,
    @Factory.Parameter val assistedString: String
  )

  @Factory
  @ProvidedBy(Component1::class, Component2::class)
  interface MultipleFactory {

    fun create(assistedString: String): MultipleFactoryTarget
  }
}
