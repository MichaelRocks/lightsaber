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

import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

class GenericInjectionTest {
  @Test
  fun testGenericConstructorInjection() {
    val injector = lightsaber.createInjector(GenericComponent())
    val target = injector.getInstance<ConstructorInjectionTarget>()
    validateTarget(GenericModule(), target)
  }

  @Test
  fun testGenericFieldInjection() {
    val injector = lightsaber.createInjector(GenericComponent())
    val target = FieldInjectionTarget()
    injector.injectMembers(target)
    validateTarget(GenericModule(), target)
  }

  @Test
  fun testGenericMethodInjection() {
    val injector = lightsaber.createInjector(GenericComponent())
    val target = MethodInjectionTarget()
    injector.injectMembers(target)
    validateTarget(GenericModule(), target)
  }

  @Test
  fun testGetGenericInstanceWithKey() {
    val injector = lightsaber.createInjector(GenericComponent())
    val token = object : TypeToken<List<@JvmSuppressWildcards String>>() {}
    val type = (token.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    val key = Key.of<List<String>>(type)
    assertEquals(listOf("Hello", "world"), injector.getInstance(key))
  }

  private fun validateTarget(module: GenericModule, target: Target) {
    assertEquals(module.provideStringList(), target.stringList)
    assertEquals(module.provideIntList(), target.intList)
  }

  @Module
  private class GenericModule {
    @Provides
    fun provideStringList(): List<String> = listOf("Hello", "world")
    @Provides
    fun provideIntList(): List<Int> = listOf(42, 43)
    @Provides
    fun provideIntArrayList(): List<IntArray> = listOf(intArrayOf(42, 43))
  }

  @Component
  private class GenericComponent {
    @Provides
    fun provideGenericModule(): GenericModule = GenericModule()
  }

  private interface Target {
    val stringList: List<String>
    val intList: List<Int>
    val intArrayList: List<IntArray>
  }

  private class ConstructorInjectionTarget @Inject constructor(
      override val stringList: List<String>,
      override val intList: List<Int>,
      override val intArrayList: List<IntArray>
  ) : Target

  private class FieldInjectionTarget : Target {
    @Inject
    override val stringList: List<String> = inject()
    @Inject
    override val intList: List<Int> = inject()
    @Inject
    override val intArrayList: List<IntArray> = inject()
  }

  private class MethodInjectionTarget : Target {
    @set:Inject
    override var stringList: List<String> = inject()
    @set:Inject
    override var intList: List<Int> = inject()
    @set:Inject
    override var intArrayList: List<IntArray> = inject()
  }

  open class TypeToken<T>
}
