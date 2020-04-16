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

class ImportedByInjectionTest {
  @Test
  fun testImportedByInjection() {
    val lightsaber = Lightsaber.Builder().build()
    val injector1 = lightsaber.createInjector(ImportedByComponent1())
    val injector2 = lightsaber.createInjector(ImportedByComponent2())
    val target1 = injector1.getInstance<InjectionTarget>()
    val target2 = injector2.getInstance<InjectionTarget>()

    assertEquals("Parent", target1.parentString)
    assertEquals("Child", target1.childString)
    assertEquals("Parent", target2.parentString)
    assertEquals("Child", target2.childString)
  }

  @Component
  private class ImportedByComponent1

  @Component
  private class ImportedByComponent2 {

    @Provide
    @field:Named("Parent")
    val string = "Parent"
  }

  @Module
  @ImportedBy(ImportedByComponent1::class)
  class ParentModule {

    @Provide
    @Named("Parent")
    private fun provideString(): String {
      return "Parent"
    }
  }

  @Module
  @ImportedBy(ParentModule::class, ImportedByComponent2::class)
  class ChildModule {

    @Provide
    @Named("Child")
    private fun provideString(): String {
      return "Child"
    }
  }

  @ProvidedBy(ParentModule::class, ImportedByComponent2::class)
  private class InjectionTarget @Inject private constructor(
    @Named("Parent") val parentString: String,
    @Named("Child") val childString: String
  )
}
