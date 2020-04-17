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

class ProvidedAsInjectionTest {
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
  fun testMultipleIndirectBinding() {
    val lightsaber = Lightsaber.Builder().build()
    val component = BindingComponent()
    val injector = lightsaber.createInjector(component)

    val target11 = injector.getInstance<MultipleTarget1>()
    val target12 = injector.getInstance<MultipleTarget1>()
    val target21 = injector.getInstance<MultipleTarget2>()
    val target22 = injector.getInstance<MultipleTarget2>()
    val targetImpl1 = injector.getInstance<MultipleTargetImpl>()
    val targetImpl2 = injector.getInstance<MultipleTargetImpl>()

    assertTrue(target11 is MultipleTargetImpl)
    assertTrue(target12 is MultipleTargetImpl)
    assertTrue(target21 is MultipleTargetImpl)
    assertTrue(target22 is MultipleTargetImpl)
    assertNotSame(target11, target12)
    assertNotSame(target21, target22)
    assertNotSame(targetImpl1, targetImpl2)

    val targets = setOf(target11, target12, target21, target22, targetImpl1, targetImpl2)
    assertEquals(6, targets.size)
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

  @ProvidedBy(BindingComponent::class)
  @ProvidedAs(DirectTarget::class)
  class DirectTargetImpl @Inject private constructor() : DirectTarget

  interface IndirectTarget

  abstract class AbstractIndirectTarget : IndirectTarget

  @ProvidedBy(BindingComponent::class)
  @ProvidedAs(IndirectTarget::class)
  class IndirectTargetImpl @Inject private constructor() : AbstractIndirectTarget()

  interface MultipleTarget1

  interface MultipleTarget2 : MultipleTarget1

  @ProvidedBy(BindingComponent::class)
  @ProvidedAs(MultipleTarget1::class, MultipleTarget2::class)
  class MultipleTargetImpl @Inject private constructor() : MultipleTarget2

  interface SingletonTarget

  @Singleton
  @ProvidedBy(BindingComponent::class)
  @ProvidedAs(SingletonTarget::class)
  class SingletonTargetImpl @Inject private constructor() : SingletonTarget

  @Component
  class BindingComponent
}
