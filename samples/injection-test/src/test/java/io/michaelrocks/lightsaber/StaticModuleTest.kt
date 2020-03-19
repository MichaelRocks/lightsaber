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

class StaticModuleTest {

  @Test
  fun testInjectionWithStaticModules() {
    val lightsaber = Lightsaber.Builder().build()
    val injector = lightsaber.createInjector(StaticComponent())
    val container = InjectionContainer()
    injector.injectMembers(container)

    assertEquals("Instance Field", container.instanceFieldString)
    assertEquals("Instance Method", container.instanceMethodString)
    assertEquals("Static Field", container.staticFieldString)
    assertEquals("Static Method", container.staticMethodString)
  }

  class InjectionContainer {
    @Inject
    @field:Named("InstanceField")
    val instanceFieldString: String = inject()

    @Inject
    @field:Named("InstanceMethod")
    val instanceMethodString: String = inject()

    @Inject
    @field:Named("StaticField")
    val staticFieldString: String = inject()

    @Inject
    @field:Named("StaticMethod")
    val staticMethodString: String = inject()
  }

  @Component
  class StaticComponent {

    @Import
    private val instanceFieldModule = InstanceFieldModule()

    @Import
    private fun importInstanceMethodModule() = InstanceMethodModule()

    companion object {
      @Import
      @JvmStatic
      private val staticFieldModule = StaticFieldModule()

      @Import
      @JvmStatic
      private fun importStaticMethodModule() = StaticMethodModule()
    }
  }

  @Module
  class InstanceFieldModule {

    @Provide
    @Named("InstanceField")
    private fun provideString() = "Instance Field"
  }

  @Module
  class InstanceMethodModule {

    @Provide
    @Named("InstanceMethod")
    private fun provideString() = "Instance Method"
  }

  @Module
  class StaticFieldModule {

    @Provide
    @Named("StaticField")
    private fun provideString() = "Static Field"
  }

  @Module
  class StaticMethodModule {

    @Provide
    @Named("StaticMethod")
    private fun provideString() = "Static Method"
  }
}
