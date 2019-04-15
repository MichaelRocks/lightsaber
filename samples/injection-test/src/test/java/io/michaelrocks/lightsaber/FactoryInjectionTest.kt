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

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

class FactoryInjectionTest {
  @Test
  fun testSingleMethodFactory() {
    val lightsaber = Lightsaber.Builder().build()
    val injector = lightsaber.createInjector(ParentFactoryComponent())
    val factory = injector.getInstance<SingleMethodFactory>()
    val target = factory.createTarget(
        boolean = false,
        byte = Byte.MIN_VALUE,
        char = Char.MIN_SURROGATE,
        double = Double.NEGATIVE_INFINITY,
        float = Float.NEGATIVE_INFINITY,
        int = Int.MIN_VALUE,
        long = Long.MIN_VALUE,
        short = Short.MIN_VALUE,
        string = "String",
        booleanArray = booleanArrayOf(false),
        byteArray = byteArrayOf(Byte.MIN_VALUE),
        charArray = charArrayOf(Char.MIN_SURROGATE),
        doubleArray = doubleArrayOf(Double.NEGATIVE_INFINITY),
        floatArray = floatArrayOf(Float.NEGATIVE_INFINITY),
        intArray = intArrayOf(Int.MIN_VALUE),
        longArray = longArrayOf(Long.MIN_VALUE),
        shortArray = shortArrayOf(Short.MIN_VALUE),
        stringArray = arrayOf("String"),
        intList = listOf(Int.MIN_VALUE),
        stringList = listOf("String"),
        annotatedBoolean = true,
        annotatedByte = Byte.MAX_VALUE,
        annotatedChar = Char.MAX_SURROGATE,
        annotatedDouble = Double.POSITIVE_INFINITY,
        annotatedFloat = Float.POSITIVE_INFINITY,
        annotatedInt = Int.MAX_VALUE,
        annotatedLong = Long.MAX_VALUE,
        annotatedShort = Short.MAX_VALUE,
        annotatedString = "Annotated",
        annotatedBooleanArray = booleanArrayOf(true),
        annotatedByteArray = byteArrayOf(Byte.MAX_VALUE),
        annotatedCharArray = charArrayOf(Char.MAX_SURROGATE),
        annotatedDoubleArray = doubleArrayOf(Double.POSITIVE_INFINITY),
        annotatedFloatArray = floatArrayOf(Float.POSITIVE_INFINITY),
        annotatedIntArray = intArrayOf(Int.MAX_VALUE),
        annotatedLongArray = longArrayOf(Long.MAX_VALUE),
        annotatedShortArray = shortArrayOf(Short.MAX_VALUE),
        annotatedStringArray = arrayOf("Annotated", "String"),
        annotatedIntList = listOf(Int.MAX_VALUE),
        annotatedStringList = listOf("Annotated", "String")
    )

    assertEquals(false, target.boolean)
    assertEquals(Byte.MIN_VALUE, target.byte)
    assertEquals(Char.MIN_SURROGATE, target.char)
    assertEquals(Double.NEGATIVE_INFINITY, target.double, Double.MIN_VALUE)
    assertEquals(Float.NEGATIVE_INFINITY, target.float, Float.MIN_VALUE)
    assertEquals(Int.MIN_VALUE, target.int)
    assertEquals(Long.MIN_VALUE, target.long)
    assertEquals(Short.MIN_VALUE, target.short)
    assertEquals("String", target.string)
    assertArrayEquals(booleanArrayOf(false), target.booleanArray)
    assertArrayEquals(byteArrayOf(Byte.MIN_VALUE), target.byteArray)
    assertArrayEquals(charArrayOf(Char.MIN_SURROGATE), target.charArray)
    assertArrayEquals(doubleArrayOf(Double.NEGATIVE_INFINITY), target.doubleArray, Double.MIN_VALUE)
    assertArrayEquals(floatArrayOf(Float.NEGATIVE_INFINITY), target.floatArray, Float.MIN_VALUE)
    assertArrayEquals(intArrayOf(Int.MIN_VALUE), target.intArray)
    assertArrayEquals(longArrayOf(Long.MIN_VALUE), target.longArray)
    assertArrayEquals(shortArrayOf(Short.MIN_VALUE), target.shortArray)
    assertArrayEquals(arrayOf("String"), target.stringArray)
    assertEquals(listOf(Int.MIN_VALUE), target.intList)
    assertEquals(listOf("String"), target.stringList)
    assertEquals(true, target.annotatedBoolean)
    assertEquals(Byte.MAX_VALUE, target.annotatedByte)
    assertEquals(Char.MAX_SURROGATE, target.annotatedChar)
    assertEquals(Double.POSITIVE_INFINITY, target.annotatedDouble, Double.MIN_VALUE)
    assertEquals(Float.POSITIVE_INFINITY, target.annotatedFloat, Float.MIN_VALUE)
    assertEquals(Int.MAX_VALUE, target.annotatedInt)
    assertEquals(Long.MAX_VALUE, target.annotatedLong)
    assertEquals(Short.MAX_VALUE, target.annotatedShort)
    assertEquals("Annotated", target.annotatedString)
    assertArrayEquals(booleanArrayOf(true), target.annotatedBooleanArray)
    assertArrayEquals(byteArrayOf(Byte.MAX_VALUE), target.annotatedByteArray)
    assertArrayEquals(charArrayOf(Char.MAX_SURROGATE), target.annotatedCharArray)
    assertArrayEquals(doubleArrayOf(Double.POSITIVE_INFINITY), target.annotatedDoubleArray, Double.MIN_VALUE)
    assertArrayEquals(floatArrayOf(Float.POSITIVE_INFINITY), target.annotatedFloatArray, Float.MIN_VALUE)
    assertArrayEquals(intArrayOf(Int.MAX_VALUE), target.annotatedIntArray)
    assertArrayEquals(longArrayOf(Long.MAX_VALUE), target.annotatedLongArray)
    assertArrayEquals(shortArrayOf(Short.MAX_VALUE), target.annotatedShortArray)
    assertArrayEquals(arrayOf("Annotated", "String"), target.annotatedStringArray)
    assertEquals(listOf(Int.MAX_VALUE), target.annotatedIntList)
    assertEquals(listOf("Annotated", "String"), target.annotatedStringList)
    assertEquals(true, target.injectedBoolean)
    assertEquals(42.toByte(), target.injectedByte)
    assertEquals('x', target.injectedChar)
    assertEquals(Double.NaN, target.injectedDouble, Double.MIN_VALUE)
    assertEquals(Float.NaN, target.injectedFloat, Float.MIN_VALUE)
    assertEquals(42, target.injectedInt)
    assertEquals(42L, target.injectedLong)
    assertEquals(42.toShort(), target.injectedShort)
    assertEquals("Injected", target.injectedString)
    assertArrayEquals(booleanArrayOf(true, false), target.injectedBooleanArray)
    assertArrayEquals(byteArrayOf(42), target.injectedByteArray)
    assertArrayEquals(charArrayOf('x'), target.injectedCharArray)
    assertArrayEquals(doubleArrayOf(Double.NaN), target.injectedDoubleArray, Double.MIN_VALUE)
    assertArrayEquals(floatArrayOf(Float.NaN), target.injectedFloatArray, Float.MIN_VALUE)
    assertArrayEquals(intArrayOf(42), target.injectedIntArray)
    assertArrayEquals(longArrayOf(42L), target.injectedLongArray)
    assertArrayEquals(shortArrayOf(42), target.injectedShortArray)
    assertArrayEquals(arrayOf("Injected", "String"), target.injectedStringArray)
    assertEquals(listOf(42), target.injectedIntList)
    assertEquals(listOf("Injected", "String"), target.injectedStringList)
  }

  @Test
  fun testMultipleMethodFactory() {
    val lightsaber = Lightsaber.Builder().build()
    val parentInjector = lightsaber.createInjector(ParentFactoryComponent())
    val childInjector = parentInjector.createChildInjector(ChildFactoryComponent())
    val factory = childInjector.getInstance<MultipleMethodFactory>()
    val target1 = factory.createTarget1("String1")
    val target2 = factory.createTarget2("String2")
    val target3 = factory.createTarget3("String3")
    val target4 = factory.createTarget4("String4")

    assertEquals("Child", target1.stringFromInjector)
    assertEquals("String2", target2.stringFromMethod)
    assertEquals("Child", target3.stringFromInjector)
    assertEquals("String3", target3.stringFromMethod)
    assertEquals("Child", target4.stringFromInjector)
    assertEquals("Child", target4.stringFromFieldInjection)
    assertEquals("Child", target4.stringFromMethodInjection)
    assertEquals("String4", target4.stringFromMethod)
  }

  @Component
  private class ParentFactoryComponent {
    @Provides
    private fun provideParentFactoryModule(): ParentFactoryModule = ParentFactoryModule()
  }

  @Module
  private class ParentFactoryModule {
    @Provides
    @Named("Injected")
    private fun provideBoolean(): Boolean = true

    @Provides
    @Named("Injected")
    private fun provideByte(): Byte = 42

    @Provides
    @Named("Injected")
    private fun provideChar(): Char = 'x'

    @Provides
    @Named("Injected")
    private fun provideDouble(): Double = Double.NaN

    @Provides
    @Named("Injected")
    private fun provideFloat(): Float = Float.NaN

    @Provides
    @Named("Injected")
    private fun provideInt(): Int = 42

    @Provides
    @Named("Injected")
    private fun provideLong(): Long = 42L

    @Provides
    @Named("Injected")
    private fun provideShort(): Short = 42

    @Provides
    @Named("Injected")
    private fun provideString(): String = "Injected"

    @Provides
    @Named("Injected")
    private fun provideBooleanArray(): BooleanArray = booleanArrayOf(true, false)

    @Provides
    @Named("Injected")
    private fun provideByteArray(): ByteArray = byteArrayOf(42)

    @Provides
    @Named("Injected")
    private fun provideCharArray(): CharArray = charArrayOf('x')

    @Provides
    @Named("Injected")
    private fun provideDoubleArray(): DoubleArray = doubleArrayOf(Double.NaN)

    @Provides
    @Named("Injected")
    private fun provideFloatArray(): FloatArray = floatArrayOf(Float.NaN)

    @Provides
    @Named("Injected")
    private fun provideIntArray(): IntArray = intArrayOf(42)

    @Provides
    @Named("Injected")
    private fun provideLongArray(): LongArray = longArrayOf(42L)

    @Provides
    @Named("Injected")
    private fun provideShortArray(): ShortArray = shortArrayOf(42)

    @Provides
    @Named("Injected")
    private fun provideStringArray(): Array<String> = arrayOf("Injected", "String")

    @Provides
    @Named("Injected")
    private fun provideIntList(): List<Int> = listOf(42)

    @Provides
    @Named("Injected")
    private fun provideStringList(): List<String> = listOf("Injected", "String")
  }

  @Component(parent = ParentFactoryComponent::class)
  private class ChildFactoryComponent {
    @Provides
    private fun provideChildFactoryModule(): ChildFactoryModule = ChildFactoryModule()
  }

  @Module
  private class ChildFactoryModule {
    @Provides
    private fun provideString(): String = "Child"
  }

  @Factory
  @ProvidedBy(ParentFactoryModule::class)
  interface SingleMethodFactory {
    fun createTarget(
        boolean: Boolean,
        byte: Byte,
        char: Char,
        double: Double,
        float: Float,
        int: Int,
        long: Long,
        short: Short,
        string: String,
        booleanArray: BooleanArray,
        byteArray: ByteArray,
        charArray: CharArray,
        doubleArray: DoubleArray,
        floatArray: FloatArray,
        intArray: IntArray,
        longArray: LongArray,
        shortArray: ShortArray,
        stringArray: Array<String>,
        intList: List<Int>,
        stringList: List<String>,
        @Named("Annotated") annotatedBoolean: Boolean,
        @Named("Annotated") annotatedByte: Byte,
        @Named("Annotated") annotatedChar: Char,
        @Named("Annotated") annotatedDouble: Double,
        @Named("Annotated") annotatedFloat: Float,
        @Named("Annotated") annotatedInt: Int,
        @Named("Annotated") annotatedLong: Long,
        @Named("Annotated") annotatedShort: Short,
        @Named("Annotated") annotatedString: String,
        @Named("Annotated") annotatedBooleanArray: BooleanArray,
        @Named("Annotated") annotatedByteArray: ByteArray,
        @Named("Annotated") annotatedCharArray: CharArray,
        @Named("Annotated") annotatedDoubleArray: DoubleArray,
        @Named("Annotated") annotatedFloatArray: FloatArray,
        @Named("Annotated") annotatedIntArray: IntArray,
        @Named("Annotated") annotatedLongArray: LongArray,
        @Named("Annotated") annotatedShortArray: ShortArray,
        @Named("Annotated") annotatedStringArray: Array<String>,
        @Named("Annotated") annotatedIntList: List<Int>,
        @Named("Annotated") annotatedStringList: List<String>
    ): SingleMethodTarget
  }

  class SingleMethodTarget @Factory.Inject private constructor(
      val boolean: Boolean,
      val byte: Byte,
      val char: Char,
      val double: Double,
      val float: Float,
      val int: Int,
      val long: Long,
      val short: Short,
      val string: String,
      val booleanArray: BooleanArray,
      val byteArray: ByteArray,
      val charArray: CharArray,
      val doubleArray: DoubleArray,
      val floatArray: FloatArray,
      val intArray: IntArray,
      val longArray: LongArray,
      val shortArray: ShortArray,
      val stringArray: Array<String>,
      val intList: List<Int>,
      val stringList: List<String>,
      @Named("Annotated") val annotatedBoolean: Boolean,
      @Named("Annotated") val annotatedByte: Byte,
      @Named("Annotated") val annotatedChar: Char,
      @Named("Annotated") val annotatedDouble: Double,
      @Named("Annotated") val annotatedFloat: Float,
      @Named("Annotated") val annotatedInt: Int,
      @Named("Annotated") val annotatedLong: Long,
      @Named("Annotated") val annotatedShort: Short,
      @Named("Annotated") val annotatedString: String,
      @Named("Annotated") val annotatedBooleanArray: BooleanArray,
      @Named("Annotated") val annotatedByteArray: ByteArray,
      @Named("Annotated") val annotatedCharArray: CharArray,
      @Named("Annotated") val annotatedDoubleArray: DoubleArray,
      @Named("Annotated") val annotatedFloatArray: FloatArray,
      @Named("Annotated") val annotatedIntArray: IntArray,
      @Named("Annotated") val annotatedLongArray: LongArray,
      @Named("Annotated") val annotatedShortArray: ShortArray,
      @Named("Annotated") val annotatedStringArray: Array<String>,
      @Named("Annotated") val annotatedIntList: List<Int>,
      @Named("Annotated") val annotatedStringList: List<String>,
      @Named("Injected") val injectedBoolean: Boolean,
      @Named("Injected") val injectedByte: Byte,
      @Named("Injected") val injectedChar: Char,
      @Named("Injected") val injectedDouble: Double,
      @Named("Injected") val injectedFloat: Float,
      @Named("Injected") val injectedInt: Int,
      @Named("Injected") val injectedLong: Long,
      @Named("Injected") val injectedShort: Short,
      @Named("Injected") val injectedString: String,
      @Named("Injected") val injectedBooleanArray: BooleanArray,
      @Named("Injected") val injectedByteArray: ByteArray,
      @Named("Injected") val injectedCharArray: CharArray,
      @Named("Injected") val injectedDoubleArray: DoubleArray,
      @Named("Injected") val injectedFloatArray: FloatArray,
      @Named("Injected") val injectedIntArray: IntArray,
      @Named("Injected") val injectedLongArray: LongArray,
      @Named("Injected") val injectedShortArray: ShortArray,
      @Named("Injected") val injectedStringArray: Array<String>,
      @Named("Injected") val injectedIntList: List<Int>,
      @Named("Injected") val injectedStringList: List<String>
  )

  @Factory
  @ProvidedBy(ChildFactoryModule::class)
  interface MultipleMethodFactory {
    fun createTarget1(@Named("FromMethod") string: String): MultipleMethodTarget1
    fun createTarget2(@Named("FromMethod") string: String): MultipleMethodTarget2
    fun createTarget3(@Named("FromMethod") string: String): MultipleMethodTarget3
    fun createTarget4(@Named("FromMethod") string: String): MultipleMethodTarget4
  }

  class MultipleMethodTarget1 @Factory.Inject private constructor(
      val stringFromInjector: String
  )

  class MultipleMethodTarget2 @Factory.Inject private constructor(
      @Named("FromMethod") val stringFromMethod: String
  )

  class MultipleMethodTarget3 @Factory.Inject private constructor(
      val stringFromInjector: String,
      @Named("FromMethod") val stringFromMethod: String
  )

  class MultipleMethodTarget4 @Factory.Inject private constructor(
      val stringFromInjector: String,
      @Named("FromMethod") val stringFromMethod: String
  ) {
    @Inject
    val stringFromFieldInjection: String = inject()
    lateinit var stringFromMethodInjection: String
      private set

    @Inject
    private fun injectString(string: String) {
      stringFromMethodInjection = string
    }
  }
}
