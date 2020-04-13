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
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

class BindingInjectionTest {
  @Test
  fun testDirectBinding() {
    val lightsaber = Lightsaber.Builder().build()
    val component = BindingComponent()
    val injector = lightsaber.createInjector(component)

    val target1 = injector.getInstance<DirectTarget>()
    val target2 = injector.getInstance<DirectTarget>()
    val targetImpl1 = injector.getInstance<DirectTargetImpl>()
    val targetImpl2 = injector.getInstance<DirectTargetImpl>()

    assertTrue(target1 is DirectTargetImpl)
    assertTrue(target2 is DirectTargetImpl)
    assertNotSame(target1, target2)
    assertNotSame(targetImpl1, targetImpl2)

    val targets = setOf(target1, target2, targetImpl1, targetImpl2)
    assertEquals(4, targets.size)
  }

  @Test
  fun testIndirectBinding() {
    val lightsaber = Lightsaber.Builder().build()
    val component = BindingComponent()
    val injector = lightsaber.createInjector(component)

    val target1 = injector.getInstance<IndirectTarget>()
    val target2 = injector.getInstance<IndirectTarget>()
    val targetImpl1 = injector.getInstance<IndirectTargetImpl>()
    val targetImpl2 = injector.getInstance<IndirectTargetImpl>()

    assertTrue(target1 is IndirectTargetImpl)
    assertTrue(target2 is IndirectTargetImpl)
    assertNotSame(target1, target2)
    assertNotSame(targetImpl1, targetImpl2)

    val targets = setOf(target1, target2, targetImpl1, targetImpl2)
    assertEquals(4, targets.size)
  }

  @Test
  fun testSingletonBinding() {
    val lightsaber = Lightsaber.Builder().build()
    val component = BindingComponent()
    val injector = lightsaber.createInjector(component)

    val target1 = injector.getInstance<SingletonTarget>()
    val target2 = injector.getInstance<SingletonTarget>()
    val targetImpl1 = injector.getInstance<SingletonTargetImpl>()
    val targetImpl2 = injector.getInstance<SingletonTargetImpl>()

    assertTrue(target1 is SingletonTargetImpl)
    assertTrue(target2 is SingletonTargetImpl)
    assertSame(target1, target2)
    assertSame(targetImpl1, targetImpl2)

    val targets = setOf(target1, target2, targetImpl1, targetImpl2)
    assertEquals(1, targets.size)
  }

  interface DirectTarget

  @ProvidedBy(BindingModule::class)
  @ProvidedAs(DirectTarget::class)
  class DirectTargetImpl @Inject private constructor() : DirectTarget

  interface IndirectTarget

  abstract class AbstractIndirectTarget : IndirectTarget

  @ProvidedBy(BindingModule::class)
  @ProvidedAs(IndirectTarget::class)
  class IndirectTargetImpl @Inject private constructor() : AbstractIndirectTarget()

  interface SingletonTarget

  abstract class AbstractSingletonTarget : SingletonTarget

  @Singleton
  @ProvidedBy(BindingModule::class)
  @ProvidedAs(SingletonTarget::class)
  class SingletonTargetImpl @Inject private constructor() : AbstractSingletonTarget()

  @Component
  class BindingComponent {

    @Import
    private fun importBindingModule() = BindingModule()
  }

  @Module
  class BindingModule
}
