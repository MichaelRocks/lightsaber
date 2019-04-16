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

import org.junit.Assert
import org.junit.Test
import javax.inject.Inject

class LambdaInjectionTest {
  @Test
  fun testLambdaConstructorInjection() {
    val injector = Lightsaber.Builder().build().createInjector(LambdaComponent())
    val target = injector.getInstance<ConstructorInjectionTarget>()
    validateTarget(LambdaModule(), target)
  }

  @Test
  fun testLambdaFieldInjection() {
    val injector = Lightsaber.Builder().build().createInjector(LambdaComponent())
    val target = FieldInjectionTarget()
    injector.injectMembers(target)
    validateTarget(LambdaModule(), target)
  }

  @Test
  fun testLambdaMethodInjection() {
    val injector = Lightsaber.Builder().build().createInjector(LambdaComponent())
    val target = MethodInjectionTarget()
    injector.injectMembers(target)
    validateTarget(LambdaModule(), target)
  }

  private fun validateTarget(module: LambdaModule, target: Target) {
    Assert.assertSame(module.provideGreeting1(), target.greeting1)
    Assert.assertSame(module.provideGreeting2(), target.greeting2)
    Assert.assertSame(module.provideGreeting3(), target.greeting3)
    Assert.assertEquals("Hello, world!", target.greeting1())
    Assert.assertEquals("Hello, world!", target.greeting2("world"))
    Assert.assertEquals("Hello, world!", target.greeting3("Hello", "world"))
  }

  @Module
  private class LambdaModule {
    @Provide
    fun provideGreeting1(): () -> String = { "Hello, world!" }
    @Provide
    fun provideGreeting2(): (String) -> String = { "Hello, $it!" }
    @Provide
    fun provideGreeting3(): (String, String) -> String = { greeting, name -> "$greeting, $name!" }
  }

  @Component
  private class LambdaComponent {
    @Import
    fun importLambdaModule(): LambdaModule = LambdaModule()
  }

  private interface Target {
    val greeting1: () -> String
    val greeting2: (String) -> String
    val greeting3: (String, String) -> String
  }

  @JvmSuppressWildcards
  @ProvidedBy(LambdaModule::class)
  private class ConstructorInjectionTarget @Inject constructor(
      override val greeting1: () -> String,
      override val greeting2: (String) -> String,
      override val greeting3: (String, String) -> String
  ) : Target

  private class FieldInjectionTarget : Target {
    @Inject
    override val greeting1: () -> String = inject()
    @Inject
    override val greeting2: (String) -> String = inject()
    @Inject
    override val greeting3: (String, String) -> String = inject()
  }

  @JvmSuppressWildcards
  private class MethodInjectionTarget : Target {
    @set:Inject
    override var greeting1: () -> String = inject()
    @set:Inject
    override var greeting2: (String) -> String = inject()
    @set:Inject
    override var greeting3: (String, String) -> String = inject()
  }
}
