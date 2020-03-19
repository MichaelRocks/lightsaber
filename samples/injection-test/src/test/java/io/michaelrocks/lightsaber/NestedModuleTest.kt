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
import javax.inject.Named

class NestedModuleTest {

  @Test
  fun testInjectionWithStaticModules() {
    val lightsaber = Lightsaber.Builder().build()
    val injector = lightsaber.createInjector(NestedComponent())
    val container = InjectionContainer()
    injector.injectMembers(container)

    assertEquals("Outer", container.outerString)
    assertEquals("Inner Field", container.innerFieldString)
    assertEquals("Inner Method", container.innerMethodString)
  }

  class InjectionContainer {
    @Inject
    @field:Named("Outer")
    val outerString: String = inject()

    @Inject
    @field:Named("InnerField")
    val innerFieldString: String = inject()

    @Inject
    @field:Named("InnerMethod")
    val innerMethodString: String = inject()
  }

  @Component
  class NestedComponent {

    @Import
    private fun importOuterModule() = OuterModule()
  }

  @Module
  class OuterModule {

    @Import
    private val innerFieldModule = InnerFieldModule()

    @Import
    private fun importInnerMethodModule() = InnerMethodModule()

    @Provide
    @Named("Outer")
    private fun provideString() = "Outer"
  }

  @Module
  class InnerFieldModule {

    @Provide
    @Named("InnerField")
    private fun provideString() = "Inner Field"
  }

  @Module
  class InnerMethodModule {

    @Provide
    @Named("InnerMethod")
    private fun provideString() = "Inner Method"
  }
}
