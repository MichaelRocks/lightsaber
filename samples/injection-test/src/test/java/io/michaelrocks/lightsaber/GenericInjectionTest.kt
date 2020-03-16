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

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.lang.reflect.ParameterizedType
import javax.inject.Inject

class GenericInjectionTest {
  @Test
  fun testGenericConstructorInjection() {
    val injector = Lightsaber.Builder().build().createInjector(GenericComponent())
    val target = injector.getInstance<ConstructorInjectionTarget>()
    validateTarget(GenericModule(), target)
  }

  @Test
  fun testGenericFieldInjection() {
    val injector = Lightsaber.Builder().build().createInjector(GenericComponent())
    val target = FieldInjectionTarget()
    injector.injectMembers(target)
    validateTarget(GenericModule(), target)
  }

  @Test
  fun testGenericMethodInjection() {
    val injector = Lightsaber.Builder().build().createInjector(GenericComponent())
    val target = MethodInjectionTarget()
    injector.injectMembers(target)
    validateTarget(GenericModule(), target)
  }

  @Test
  fun testGetGenericInstanceWithKey() {
    val injector = Lightsaber.Builder().build().createInjector(GenericComponent())
    val token = object : TypeToken<List<@JvmSuppressWildcards String>>() {}
    val type = (token.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
    val key = Key.of<List<String>>(type)
    assertEquals(listOf("Hello", "world"), injector.getInstance(key))
  }

  private fun validateTarget(module: GenericModule, target: Target) {
    assertEquals(module.provideStringList(), target.stringList)
    assertEquals(module.provideIntList(), target.intList)
    assertListEquals(module.provideIntArrayList(), target.intArrayList)
    assertArrayEquals(module.provideIntListArray(), target.intListArray)
  }

  private fun <T> assertListEquals(expected: List<T>, actual: List<T>) {
    assertEquals("Lists have different sizes", expected.size, actual.size)
    for (i in 0..expected.size - 1) {
      val expectedElement = expected[i]
      val actualElement = actual[i]
      if (expectedElement is List<*> && actualElement is List<*>) {
        assertListEquals(expectedElement, actualElement)
      } else if (expectedElement is Array<*> && actualElement is Array<*>) {
        assertArrayEquals(expectedElement, actualElement)
      } else if (expectedElement is BooleanArray && actualElement is BooleanArray) {
        assertArrayEquals(expectedElement, actualElement)
      } else if (expectedElement is ByteArray && actualElement is ByteArray) {
        assertArrayEquals(expectedElement, actualElement)
      } else if (expectedElement is CharArray && actualElement is CharArray) {
        assertArrayEquals(expectedElement, actualElement)
      } else if (expectedElement is DoubleArray && actualElement is DoubleArray) {
        assertArrayEquals(expectedElement, actualElement, Double.MIN_VALUE)
      } else if (expectedElement is FloatArray && actualElement is FloatArray) {
        assertArrayEquals(expectedElement, actualElement, Float.MIN_VALUE)
      } else if (expectedElement is IntArray && actualElement is IntArray) {
        assertArrayEquals(expectedElement, actualElement)
      } else if (expectedElement is LongArray && actualElement is LongArray) {
        assertArrayEquals(expectedElement, actualElement)
      } else if (expectedElement is ShortArray && actualElement is ShortArray) {
        assertArrayEquals(expectedElement, actualElement)
      }
    }
  }

  @Module
  private class GenericModule {

    @Provide
    fun provideStringList(): List<String> = listOf("Hello", "world")

    @Provide
    fun provideIntList(): List<Int> = listOf(42, 43)

    @Provide
    fun provideIntArrayList(): List<IntArray> = listOf(intArrayOf(42, 43))

    @Provide
    fun provideIntListArray(): Array<List<Int>> = arrayOf(listOf(42, 43))

    @Provide
    fun provideIntListArrayArray(): Array<Array<List<Int>>> = arrayOf(arrayOf(listOf(42, 43)))
  }

  @Component
  private class GenericComponent {

    @Import
    fun importGenericModule(): GenericModule = GenericModule()
  }

  private interface Target {
    val stringList: List<String>
    val intList: List<Int>
    val intArrayList: List<IntArray>
    val intListArray: Array<List<Int>>
    val intListArrayArray: Array<Array<List<Int>>>
  }

  @ProvidedBy(GenericModule::class)
  private class ConstructorInjectionTarget @Inject constructor(
    override val stringList: List<String>,
    override val intList: List<Int>,
    override val intArrayList: List<IntArray>,
    override val intListArray: Array<List<Int>>,
    override val intListArrayArray: Array<Array<List<Int>>>
  ) : Target

  private class FieldInjectionTarget : Target {
    @Inject
    override val stringList: List<String> = inject()

    @Inject
    override val intList: List<Int> = inject()

    @Inject
    override val intArrayList: List<IntArray> = inject()

    @Inject
    override val intListArray: Array<List<Int>> = inject()

    @Inject
    override val intListArrayArray: Array<Array<List<Int>>> = inject()
  }

  private class MethodInjectionTarget : Target {
    @set:Inject
    override var stringList: List<String> = inject()

    @set:Inject
    override var intList: List<Int> = inject()

    @set:Inject
    override var intArrayList: List<IntArray> = inject()

    @set:Inject
    override var intListArray: Array<List<Int>> = inject()

    @set:Inject
    override var intListArrayArray: Array<Array<List<Int>>> = inject()
  }

  open class TypeToken<T>
}
