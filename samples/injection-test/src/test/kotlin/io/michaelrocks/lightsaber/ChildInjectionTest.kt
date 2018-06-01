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
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

class ChildInjectionTest {
  private lateinit var lightsaber: Lightsaber

  @Before
  fun createLightsaber() {
    lightsaber = Lightsaber()
  }

  @Test
  fun testCreateSingletonBeforeChildInjector() {
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val parentString = parentInjector.getInstance<String>()
    assertEquals("Parent String", parentString)
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    assertSame(parentString, parentInjector.getInstance<String>())
    assertSame(parentString, childInjector.getInstance<String>())
  }

  @Test
  fun testCreateSingletonAfterChildInjector() {
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    val parentString = parentInjector.getInstance<String>()
    assertEquals("Parent String", parentString)
    assertSame(parentString, parentInjector.getInstance<String>())
    assertSame(parentString, childInjector.getInstance<String>())
  }

  @Test
  fun testCreateSingletonInChildInjector() {
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    val childString = childInjector.getInstance<String>()
    assertEquals("Parent String", childString)
    assertSame(childString, parentInjector.getInstance<String>())
    assertSame(childString, childInjector.getInstance<String>())
  }

  @Test
  fun testCreateSingletonInTwoChildInjectors() {
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector1 = lightsaber.createChildInjector(parentInjector, ChildComponent())
    val childInjector2 = lightsaber.createChildInjector(parentInjector, ChildComponent())
    val childObject1 = childInjector1.getInstance<Any>()
    val childObject2 = childInjector2.getInstance<Any>()
    assertEquals("Child Object", childObject1)
    assertEquals("Child Object", childObject2)
    assertNotSame(childObject1, childObject2)
  }

  @Test
  fun testCreateUnboundDependencyWithChildComponentDependency() {
    val parentInjector = lightsaber.createInjector(ParentComponent())
    val childInjector = lightsaber.createChildInjector(parentInjector, ChildComponent())
    val target = childInjector.getInstance<PackageDependencyTarget>()
    assertEquals("Child String", target.packageDependency.namedString)
  }

  @Module
  private class ParentModule {
    @Provides
    @Singleton
    fun provideString(): String = StringBuilder("Parent String").toString()
  }

  @Module
  private class ChildModule {
    @Provides
    @Singleton
    fun provideObject(): Any = StringBuilder("Child Object").toString()

    @Provides
    @Singleton
    @Named("Child String")
    fun provideNamedString(): String = StringBuilder("Child String").toString()

    @Provides
    @Named("Package Dependency")
    fun provideNamedPackageDependency(packageDependency: PackageDependency): PackageDependency = packageDependency
  }

  @Component
  private class ParentComponent {
    @Provides
    private fun provideParentModule(): ParentModule = ParentModule()
  }

  @Component(parent = ParentComponent::class)
  private class ChildComponent {
    @Provides
    private fun provideChildModule(): ChildModule = ChildModule()
  }

  @ProvidedBy(ChildModule::class)
  class PackageDependency @Inject private constructor(
      @Named("Child String")
      val namedString: String
  )

  @ProvidedBy(ChildModule::class)
  class PackageDependencyTarget @Inject private constructor(
      @Named("Package Dependency")
      val packageDependency: PackageDependency
  )
}
