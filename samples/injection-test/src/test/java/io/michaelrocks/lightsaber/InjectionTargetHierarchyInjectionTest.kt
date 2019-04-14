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

import org.junit.Assert.assertEquals
import org.junit.Test
import javax.inject.Inject

class InjectionTargetHierarchyInjectionTest {
  @Test
  fun testFieldTargets() {
    val lightsaber = Lightsaber.Builder().build()
    val injector = lightsaber.createInjector(InjectionTargetHierarchyComponent())
    val target = FieldTarget4()
    injector.injectMembers(target)
    assertEquals("InjectionTargetHierarchy", target.string1)
    assertEquals("InjectionTargetHierarchy", target.string2)
    assertEquals("InjectionTargetHierarchy", target.string4)
  }

  @Test
  fun testMethodTargets() {
    val lightsaber = Lightsaber.Builder().build()
    val injector = lightsaber.createInjector(InjectionTargetHierarchyComponent())
    val target = MethodTarget4()
    injector.injectMembers(target)
    assertEquals("InjectionTargetHierarchy", target.string1)
    assertEquals("InjectionTargetHierarchy", target.string2)
    assertEquals("InjectionTargetHierarchy", target.string4)
  }

  @Component
  private class InjectionTargetHierarchyComponent {
    @Provides
    private fun provideInjectionTargetHierarchyModule(): InjectionTargetHierarchyModule {
      return InjectionTargetHierarchyModule()
    }
  }

  @Module
  class InjectionTargetHierarchyModule {
    @Provides
    private fun provideString(): String {
      return "InjectionTargetHierarchy"
    }
  }

  private abstract class FieldTarget1 {
    @Inject
    val string1: String = inject()
  }

  private abstract class FieldTarget2 : FieldTarget1() {
    @Inject
    val string2: String = inject()
  }

  private abstract class FieldTarget3 : FieldTarget2()

  private class FieldTarget4 : FieldTarget3() {
    @Inject
    val string4: String = inject()
  }

  private abstract class MethodTarget1 {
    var string1: String? = null

    @Inject
    fun method1(string: String) {
      string1 = string
    }
  }

  private abstract class MethodTarget2 : MethodTarget1() {
    var string2: String? = null

    @Inject
    fun method2(string: String) {
      string2 = string
    }
  }

  private abstract class MethodTarget3 : MethodTarget2()

  private class MethodTarget4 : MethodTarget3() {
    var string4: String? = null

    @Inject
    fun method4(string: String) {
      string4 = string
    }
  }
}
