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

import org.junit.Assert.assertSame
import org.junit.Test
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

class CycleInjectionTest {
  @Test
  fun testCycleInjection() {
    val injector = lightsaber.createInjector(CycleComponent())
    val cycleTarget1 = injector.getInstance<CycleTarget1>()
    val cycleTarget2 = injector.getInstance<CycleTarget2>()
    assertSame(cycleTarget2, cycleTarget1.cycleTarget2)
    assertSame(cycleTarget1, cycleTarget2.cycleTarget1Provider.get())
    assertSame(cycleTarget1, cycleTarget2.cycleTarget1Lazy.get())
  }

  @Module
  private class CycleModule

  @Component
  private class CycleComponent {
    @Provides
    fun provideCycleModule(): CycleModule = CycleModule()
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
}
