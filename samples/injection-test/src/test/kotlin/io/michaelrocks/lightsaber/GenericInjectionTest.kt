/*
 * Copyright 2016 Michael Rozumyanskiy
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

import org.junit.Assert
import org.junit.Test
import javax.inject.Inject

class GenericInjectionTest {
  @Test
  fun testGenericConstructorInjection() {
    val module = GenericModule()
    val injector = lightsaber.createInjector(module)
    val target = injector.getInstance<ConstructorInjectionTarget>()
    validateTarget(module, target)
  }

  @Test
  fun testGenericFieldInjection() {
    val module = GenericModule()
    val injector = lightsaber.createInjector(module)
    val target = FieldInjectionTarget()
    injector.injectMembers(target)
    validateTarget(module, target)
  }

  @Test
  fun testGenericMethodInjection() {
    val module = GenericModule()
    val injector = lightsaber.createInjector(module)
    val target = MethodInjectionTarget()
    injector.injectMembers(target)
    validateTarget(module, target)
  }

  private fun validateTarget(module: GenericModule, target: Target) {
    Assert.assertEquals(module.provideStringList(), target.stringList)
    Assert.assertEquals(module.provideIntList(), target.intList)
  }

  @Module
  private class GenericModule {
    @Provides
    fun provideStringList(): List<String> = listOf("Hello", "world")
    @Provides
    fun provideIntList(): List<Int> = listOf(42, 43)
  }

  private interface Target {
    val stringList: List<String>
    val intList: List<Int>
  }

  private class ConstructorInjectionTarget @Inject constructor(
      override val stringList: List<String>,
      override val intList: List<Int>
  ) : Target

  private class FieldInjectionTarget : Target {
    @Inject
    override val stringList: List<String> = inject()
    @Inject
    override val intList: List<Int> = inject()
  }

  private class MethodInjectionTarget : Target {
    @set:Inject
    override var stringList: List<String> = inject()
    @set:Inject
    override var intList: List<Int> = inject()
  }
}
