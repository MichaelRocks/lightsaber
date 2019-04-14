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
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test
import javax.inject.Inject

class LazyInjectionTest {
  @Test
  fun testLazyConstructorInjection() {
    val injector = Lightsaber.Builder().build().createInjector(LazyComponent())
    val target = injector.getInstance<ConstructorInjectionTarget>()
    validateTarget(LazyModule(), target)
  }

  @Test
  fun testLazyFieldInjection() {
    val injector = Lightsaber.Builder().build().createInjector(LazyComponent())
    val target = FieldInjectionTarget()
    injector.injectMembers(target)
    validateTarget(LazyModule(), target)
  }

  @Test
  fun testLazyMethodInjection() {
    val injector = Lightsaber.Builder().build().createInjector(LazyComponent())
    val target = MethodInjectionTarget()
    injector.injectMembers(target)
    validateTarget(LazyModule(), target)
  }

  private fun validateTarget(module: LazyModule, target: Target) {
    assertEquals(module.provideString(), target.string)
    assertEquals(module.provideString(), target.lazyString1.get())
    assertEquals(module.provideString(), target.lazyString2.get())
    assertNotSame(target.string, target.lazyString1.get())
    assertNotSame(target.string, target.lazyString2.get())
    assertNotSame(target.lazyString1.get(), target.lazyString2.get())
    assertSame(target.lazyString1.get(), target.lazyString1.get())
  }

  @Module
  private class LazyModule {
    @Provides
    fun provideString(): String = StringBuilder("String").toString()
  }

  @Component
  private class LazyComponent {
    @Provides
    fun provideLazyModule(): LazyModule = LazyModule()
  }

  private interface Target {
    val string: String
    val lazyString1: Lazy<String>
    val lazyString2: Lazy<String>
  }

  @ProvidedBy(LazyModule::class)
  private class ConstructorInjectionTarget @Inject constructor(
      override val string: String,
      override val lazyString1: Lazy<String>,
      override val lazyString2: Lazy<String>
  ) : Target

  private class FieldInjectionTarget : Target {
    @Inject
    override lateinit var string: String
    @Inject
    override lateinit var lazyString1: Lazy<String>
    @Inject
    override lateinit var lazyString2: Lazy<String>
  }

  private class MethodInjectionTarget : Target {
    @set:Inject
    override var string: String = inject()
    @set:Inject
    override var lazyString1: Lazy<String> = inject()
    @set:Inject
    override var lazyString2: Lazy<String> = inject()
  }
}
