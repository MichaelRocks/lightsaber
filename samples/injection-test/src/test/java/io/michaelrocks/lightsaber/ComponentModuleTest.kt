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

class ComponentModuleTest {

  @Test
  fun testComponentWithoutModules() {
    val lightsaber = Lightsaber.Builder().build()
    val injector = lightsaber.createInjector(ComponentWithoutModules())
    val container = injector.getInstance<InjectionContainer>()

    assertEquals("Component", container.componentString)
    assertEquals("Module", container.moduleString)
  }

  @Test
  fun testComponentWithModules() {
    val lightsaber = Lightsaber.Builder().build()
    val injector = lightsaber.createInjector(ComponentWithModules())
    val container = injector.getInstance<InjectionContainer>()

    assertEquals("Component", container.componentString)
    assertEquals("Module", container.moduleString)
  }

  @Component
  class ComponentWithoutModules {

    @Provide
    private val componentString = "Component"

    @Provide
    @Named("Module")
    private fun provideModuleString() = "Module"
  }

  @Component
  class ComponentWithModules {

    @Provide
    private fun provideString() = "Component"

    @Import
    private fun importModule() = ComponentModule()
  }

  @Module
  class ComponentModule {

    @Provide
    @Named("Module")
    private fun provideString() = "Module"
  }

  @ProvidedBy(ComponentWithoutModules::class, ComponentWithModules::class)
  class InjectionContainer @Inject private constructor(
    @Inject
    val componentString: String,
    @Inject
    @field:Named("Module")
    val moduleString: String
  )
}
