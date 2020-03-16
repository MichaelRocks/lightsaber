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

import org.junit.Assert.assertSame
import org.junit.Test
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

class CycleInjectionTest {
  @Test
  fun testCycleInjection() {
    val injector = Lightsaber.Builder().build().createInjector(CycleComponent())
    val cycleTarget1 = injector.getInstance<CycleTarget1>()
    val cycleTarget2 = injector.getInstance<CycleTarget2>()
    assertSame(cycleTarget2, cycleTarget1.cycleTarget2)
    assertSame(cycleTarget1, cycleTarget2.cycleTarget1Provider.get())
    assertSame(cycleTarget1, cycleTarget2.cycleTarget1Lazy.get())
  }

  @Test
  fun testFieldCycleInjection() {
    val injector = Lightsaber.Builder().build().createInjector(CycleComponent())
    val fieldCycleTarget1 = injector.getInstance<FieldCycleTarget1>()
    val fieldCycleTarget2 = injector.getInstance<FieldCycleTarget2>()
    assertSame(fieldCycleTarget2, fieldCycleTarget1.fieldCycleTarget2)
    assertSame(fieldCycleTarget1, fieldCycleTarget2.fieldCycleTarget1Provider.get())
    assertSame(fieldCycleTarget1, fieldCycleTarget2.fieldCycleTarget1Lazy.get())
  }

  @Test
  fun testMethodCycleInjection() {
    val injector = Lightsaber.Builder().build().createInjector(CycleComponent())
    val methodCycleTarget1 = injector.getInstance<MethodCycleTarget1>()
    val methodCycleTarget2 = injector.getInstance<MethodCycleTarget2>()
    assertSame(methodCycleTarget2, methodCycleTarget1.methodCycleTarget2)
    assertSame(methodCycleTarget1, methodCycleTarget2.methodCycleTarget1Provider.get())
    assertSame(methodCycleTarget1, methodCycleTarget2.methodCycleTarget1Lazy.get())
  }

  @Module
  private class CycleModule

  @Component
  private class CycleComponent {

    @Import
    fun importCycleModule(): CycleModule = CycleModule()
  }

  @ProvidedBy(CycleModule::class)
  @Singleton
  private class CycleTarget1 @Inject constructor(
    val cycleTarget2: CycleTarget2
  )

  @ProvidedBy(CycleModule::class)
  @Singleton
  private class CycleTarget2 @Inject constructor(
    val cycleTarget1Provider: Provider<CycleTarget1>,
    val cycleTarget1Lazy: Lazy<CycleTarget1>
  )

  @ProvidedBy(CycleModule::class)
  @Singleton
  private class FieldCycleTarget1 @Inject constructor() {

    @Inject val fieldCycleTarget2: FieldCycleTarget2 = inject()
  }

  @ProvidedBy(CycleModule::class)
  @Singleton
  private class FieldCycleTarget2 @Inject constructor() {

    @Inject val fieldCycleTarget1Provider: Provider<FieldCycleTarget1> = inject()
    @Inject val fieldCycleTarget1Lazy: Lazy<FieldCycleTarget1> = inject()
  }

  @ProvidedBy(CycleModule::class)
  @Singleton
  private class MethodCycleTarget1 @Inject constructor() {

    @set:Inject
    lateinit var methodCycleTarget2: MethodCycleTarget2
  }

  @ProvidedBy(CycleModule::class)
  @Singleton
  private class MethodCycleTarget2 @Inject constructor() {

    @set:Inject
    lateinit var methodCycleTarget1Provider: Provider<MethodCycleTarget1>

    @set:Inject
    lateinit var methodCycleTarget1Lazy: Lazy<MethodCycleTarget1>
  }
}
