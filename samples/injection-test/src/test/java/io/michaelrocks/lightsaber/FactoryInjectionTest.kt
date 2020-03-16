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
    assertEquals(true, target.injectedDefaultBoolean)
    assertEquals(42.toByte(), target.injectedDefaultByte)
    assertEquals('x', target.injectedDefaultChar)
    assertEquals(Double.NaN, target.injectedDefaultDouble, Double.MIN_VALUE)
    assertEquals(Float.NaN, target.injectedDefaultFloat, Float.MIN_VALUE)
    assertEquals(42, target.injectedDefaultInt)
    assertEquals(42L, target.injectedDefaultLong)
    assertEquals(42.toShort(), target.injectedDefaultShort)
    assertEquals("Default", target.injectedDefaultString)
    assertArrayEquals(booleanArrayOf(true, false), target.injectedDefaultBooleanArray)
    assertArrayEquals(byteArrayOf(42), target.injectedDefaultByteArray)
    assertArrayEquals(charArrayOf('x'), target.injectedDefaultCharArray)
    assertArrayEquals(doubleArrayOf(Double.NaN), target.injectedDefaultDoubleArray, Double.MIN_VALUE)
    assertArrayEquals(floatArrayOf(Float.NaN), target.injectedDefaultFloatArray, Float.MIN_VALUE)
    assertArrayEquals(intArrayOf(42), target.injectedDefaultIntArray)
    assertArrayEquals(longArrayOf(42L), target.injectedDefaultLongArray)
    assertArrayEquals(shortArrayOf(42), target.injectedDefaultShortArray)
    assertArrayEquals(arrayOf("Default", "String"), target.injectedDefaultStringArray)
    assertEquals(listOf(42), target.injectedDefaultIntList)
    assertEquals(listOf("Default", "String"), target.injectedDefaultStringList)
    assertEquals(false, target.injectedAnnotatedBoolean)
    assertEquals(43.toByte(), target.injectedAnnotatedByte)
    assertEquals('y', target.injectedAnnotatedChar)
    assertEquals(Double.MAX_VALUE, target.injectedAnnotatedDouble, Double.MIN_VALUE)
    assertEquals(Float.MAX_VALUE, target.injectedAnnotatedFloat, Float.MIN_VALUE)
    assertEquals(43, target.injectedAnnotatedInt)
    assertEquals(43L, target.injectedAnnotatedLong)
    assertEquals(43.toShort(), target.injectedAnnotatedShort)
    assertEquals("Annotated", target.injectedAnnotatedString)
    assertArrayEquals(booleanArrayOf(false, true), target.injectedAnnotatedBooleanArray)
    assertArrayEquals(byteArrayOf(43), target.injectedAnnotatedByteArray)
    assertArrayEquals(charArrayOf('y'), target.injectedAnnotatedCharArray)
    assertArrayEquals(doubleArrayOf(Double.MAX_VALUE), target.injectedAnnotatedDoubleArray, Double.MIN_VALUE)
    assertArrayEquals(floatArrayOf(Float.MAX_VALUE), target.injectedAnnotatedFloatArray, Float.MIN_VALUE)
    assertArrayEquals(intArrayOf(43), target.injectedAnnotatedIntArray)
    assertArrayEquals(longArrayOf(43L), target.injectedAnnotatedLongArray)
    assertArrayEquals(shortArrayOf(43), target.injectedAnnotatedShortArray)
    assertArrayEquals(arrayOf("Annotated", "String"), target.injectedAnnotatedStringArray)
    assertEquals(listOf(43), target.injectedAnnotatedIntList)
    assertEquals(listOf("Annotated", "String"), target.injectedAnnotatedStringList)
    assertEquals(true, target.injectedBoolean)
    assertEquals(44.toByte(), target.injectedByte)
    assertEquals('z', target.injectedChar)
    assertEquals(Double.MIN_VALUE, target.injectedDouble, Double.MIN_VALUE)
    assertEquals(Float.MIN_VALUE, target.injectedFloat, Float.MIN_VALUE)
    assertEquals(44, target.injectedInt)
    assertEquals(44L, target.injectedLong)
    assertEquals(44.toShort(), target.injectedShort)
    assertEquals("Injected", target.injectedString)
    assertArrayEquals(booleanArrayOf(true, true), target.injectedBooleanArray)
    assertArrayEquals(byteArrayOf(44), target.injectedByteArray)
    assertArrayEquals(charArrayOf('z'), target.injectedCharArray)
    assertArrayEquals(doubleArrayOf(Double.MIN_VALUE), target.injectedDoubleArray, Double.MIN_VALUE)
    assertArrayEquals(floatArrayOf(Float.MIN_VALUE), target.injectedFloatArray, Float.MIN_VALUE)
    assertArrayEquals(intArrayOf(44), target.injectedIntArray)
    assertArrayEquals(longArrayOf(44L), target.injectedLongArray)
    assertArrayEquals(shortArrayOf(44), target.injectedShortArray)
    assertArrayEquals(arrayOf("Injected", "String"), target.injectedStringArray)
    assertEquals(listOf(44), target.injectedIntList)
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

    @Import
    private fun importParentFactoryModule(): ParentFactoryModule = ParentFactoryModule()
  }

  @Module
  private class ParentFactoryModule {

    @Provide
    private fun provideBoolean(): Boolean = true

    @Provide
    private fun provideByte(): Byte = 42

    @Provide
    private fun provideChar(): Char = 'x'

    @Provide
    private fun provideDouble(): Double = Double.NaN

    @Provide
    private fun provideFloat(): Float = Float.NaN

    @Provide
    private fun provideInt(): Int = 42

    @Provide
    private fun provideLong(): Long = 42L

    @Provide
    private fun provideShort(): Short = 42

    @Provide
    private fun provideString(): String = "Default"

    @Provide
    private fun provideBooleanArray(): BooleanArray = booleanArrayOf(true, false)

    @Provide
    private fun provideByteArray(): ByteArray = byteArrayOf(42)

    @Provide
    private fun provideCharArray(): CharArray = charArrayOf('x')

    @Provide
    private fun provideDoubleArray(): DoubleArray = doubleArrayOf(Double.NaN)

    @Provide
    private fun provideFloatArray(): FloatArray = floatArrayOf(Float.NaN)

    @Provide
    private fun provideIntArray(): IntArray = intArrayOf(42)

    @Provide
    private fun provideLongArray(): LongArray = longArrayOf(42L)

    @Provide
    private fun provideShortArray(): ShortArray = shortArrayOf(42)

    @Provide
    private fun provideStringArray(): Array<String> = arrayOf("Default", "String")

    @Provide
    private fun provideIntList(): List<Int> = listOf(42)

    @Provide
    private fun provideStringList(): List<String> = listOf("Default", "String")

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedBoolean(): Boolean = false

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedByte(): Byte = 43

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedChar(): Char = 'y'

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedDouble(): Double = Double.MAX_VALUE

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedFloat(): Float = Float.MAX_VALUE

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedInt(): Int = 43

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedLong(): Long = 43L

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedShort(): Short = 43

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedString(): String = "Annotated"

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedBooleanArray(): BooleanArray = booleanArrayOf(false, true)

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedByteArray(): ByteArray = byteArrayOf(43)

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedCharArray(): CharArray = charArrayOf('y')

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedDoubleArray(): DoubleArray = doubleArrayOf(Double.MAX_VALUE)

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedFloatArray(): FloatArray = floatArrayOf(Float.MAX_VALUE)

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedIntArray(): IntArray = intArrayOf(43)

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedLongArray(): LongArray = longArrayOf(43L)

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedShortArray(): ShortArray = shortArrayOf(43)

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedStringArray(): Array<String> = arrayOf("Annotated", "String")

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedIntList(): List<Int> = listOf(43)

    @Provide
    @Named("Annotated")
    private fun provideAnnotatedStringList(): List<String> = listOf("Annotated", "String")

    @Provide
    @Named("Injected")
    private fun provideInjectedBoolean(): Boolean = true

    @Provide
    @Named("Injected")
    private fun provideInjectedByte(): Byte = 44

    @Provide
    @Named("Injected")
    private fun provideInjectedChar(): Char = 'z'

    @Provide
    @Named("Injected")
    private fun provideInjectedDouble(): Double = Double.MIN_VALUE

    @Provide
    @Named("Injected")
    private fun provideInjectedFloat(): Float = Float.MIN_VALUE

    @Provide
    @Named("Injected")
    private fun provideInjectedInt(): Int = 44

    @Provide
    @Named("Injected")
    private fun provideInjectedLong(): Long = 44L

    @Provide
    @Named("Injected")
    private fun provideInjectedShort(): Short = 44

    @Provide
    @Named("Injected")
    private fun provideInjectedString(): String = "Injected"

    @Provide
    @Named("Injected")
    private fun provideInjectedBooleanArray(): BooleanArray = booleanArrayOf(true, true)

    @Provide
    @Named("Injected")
    private fun provideInjectedByteArray(): ByteArray = byteArrayOf(44)

    @Provide
    @Named("Injected")
    private fun provideInjectedCharArray(): CharArray = charArrayOf('z')

    @Provide
    @Named("Injected")
    private fun provideInjectedDoubleArray(): DoubleArray = doubleArrayOf(Double.MIN_VALUE)

    @Provide
    @Named("Injected")
    private fun provideInjectedFloatArray(): FloatArray = floatArrayOf(Float.MIN_VALUE)

    @Provide
    @Named("Injected")
    private fun provideInjectedIntArray(): IntArray = intArrayOf(44)

    @Provide
    @Named("Injected")
    private fun provideInjectedLongArray(): LongArray = longArrayOf(44L)

    @Provide
    @Named("Injected")
    private fun provideInjectedShortArray(): ShortArray = shortArrayOf(44)

    @Provide
    @Named("Injected")
    private fun provideInjectedStringArray(): Array<String> = arrayOf("Injected", "String")

    @Provide
    @Named("Injected")
    private fun provideInjectedIntList(): List<Int> = listOf(44)

    @Provide
    @Named("Injected")
    private fun provideInjectedStringList(): List<String> = listOf("Injected", "String")
  }

  @Component(parent = ParentFactoryComponent::class)
  private class ChildFactoryComponent {

    @Import
    private fun provideChildFactoryModule(): ChildFactoryModule = ChildFactoryModule()
  }

  @Module
  private class ChildFactoryModule {

    @Provide
    @Named("Child")
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
    @Factory.Parameter val boolean: Boolean,
    @Factory.Parameter val byte: Byte,
    @Factory.Parameter val char: Char,
    @Factory.Parameter val double: Double,
    @Factory.Parameter val float: Float,
    @Factory.Parameter val int: Int,
    @Factory.Parameter val long: Long,
    @Factory.Parameter val short: Short,
    @Factory.Parameter val string: String,
    @Factory.Parameter val booleanArray: BooleanArray,
    @Factory.Parameter val byteArray: ByteArray,
    @Factory.Parameter val charArray: CharArray,
    @Factory.Parameter val doubleArray: DoubleArray,
    @Factory.Parameter val floatArray: FloatArray,
    @Factory.Parameter val intArray: IntArray,
    @Factory.Parameter val longArray: LongArray,
    @Factory.Parameter val shortArray: ShortArray,
    @Factory.Parameter val stringArray: Array<String>,
    @Factory.Parameter val intList: List<Int>,
    @Factory.Parameter val stringList: List<String>,
    @Factory.Parameter @Named("Annotated") val annotatedBoolean: Boolean,
    @Factory.Parameter @Named("Annotated") val annotatedByte: Byte,
    @Factory.Parameter @Named("Annotated") val annotatedChar: Char,
    @Factory.Parameter @Named("Annotated") val annotatedDouble: Double,
    @Factory.Parameter @Named("Annotated") val annotatedFloat: Float,
    @Factory.Parameter @Named("Annotated") val annotatedInt: Int,
    @Factory.Parameter @Named("Annotated") val annotatedLong: Long,
    @Factory.Parameter @Named("Annotated") val annotatedShort: Short,
    @Factory.Parameter @Named("Annotated") val annotatedString: String,
    @Factory.Parameter @Named("Annotated") val annotatedBooleanArray: BooleanArray,
    @Factory.Parameter @Named("Annotated") val annotatedByteArray: ByteArray,
    @Factory.Parameter @Named("Annotated") val annotatedCharArray: CharArray,
    @Factory.Parameter @Named("Annotated") val annotatedDoubleArray: DoubleArray,
    @Factory.Parameter @Named("Annotated") val annotatedFloatArray: FloatArray,
    @Factory.Parameter @Named("Annotated") val annotatedIntArray: IntArray,
    @Factory.Parameter @Named("Annotated") val annotatedLongArray: LongArray,
    @Factory.Parameter @Named("Annotated") val annotatedShortArray: ShortArray,
    @Factory.Parameter @Named("Annotated") val annotatedStringArray: Array<String>,
    @Factory.Parameter @Named("Annotated") val annotatedIntList: List<Int>,
    @Factory.Parameter @Named("Annotated") val annotatedStringList: List<String>,
    val injectedDefaultBoolean: Boolean,
    val injectedDefaultByte: Byte,
    val injectedDefaultChar: Char,
    val injectedDefaultDouble: Double,
    val injectedDefaultFloat: Float,
    val injectedDefaultInt: Int,
    val injectedDefaultLong: Long,
    val injectedDefaultShort: Short,
    val injectedDefaultString: String,
    val injectedDefaultBooleanArray: BooleanArray,
    val injectedDefaultByteArray: ByteArray,
    val injectedDefaultCharArray: CharArray,
    val injectedDefaultDoubleArray: DoubleArray,
    val injectedDefaultFloatArray: FloatArray,
    val injectedDefaultIntArray: IntArray,
    val injectedDefaultLongArray: LongArray,
    val injectedDefaultShortArray: ShortArray,
    val injectedDefaultStringArray: Array<String>,
    val injectedDefaultIntList: List<Int>,
    val injectedDefaultStringList: List<String>,
    @Named("Annotated") val injectedAnnotatedBoolean: Boolean,
    @Named("Annotated") val injectedAnnotatedByte: Byte,
    @Named("Annotated") val injectedAnnotatedChar: Char,
    @Named("Annotated") val injectedAnnotatedDouble: Double,
    @Named("Annotated") val injectedAnnotatedFloat: Float,
    @Named("Annotated") val injectedAnnotatedInt: Int,
    @Named("Annotated") val injectedAnnotatedLong: Long,
    @Named("Annotated") val injectedAnnotatedShort: Short,
    @Named("Annotated") val injectedAnnotatedString: String,
    @Named("Annotated") val injectedAnnotatedBooleanArray: BooleanArray,
    @Named("Annotated") val injectedAnnotatedByteArray: ByteArray,
    @Named("Annotated") val injectedAnnotatedCharArray: CharArray,
    @Named("Annotated") val injectedAnnotatedDoubleArray: DoubleArray,
    @Named("Annotated") val injectedAnnotatedFloatArray: FloatArray,
    @Named("Annotated") val injectedAnnotatedIntArray: IntArray,
    @Named("Annotated") val injectedAnnotatedLongArray: LongArray,
    @Named("Annotated") val injectedAnnotatedShortArray: ShortArray,
    @Named("Annotated") val injectedAnnotatedStringArray: Array<String>,
    @Named("Annotated") val injectedAnnotatedIntList: List<Int>,
    @Named("Annotated") val injectedAnnotatedStringList: List<String>,
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
    @Named("Child") val stringFromInjector: String
  )

  class MultipleMethodTarget2 @Factory.Inject private constructor(
    @Factory.Parameter @Named("FromMethod") val stringFromMethod: String
  )

  class MultipleMethodTarget3 @Factory.Inject private constructor(
    @Named("Child") val stringFromInjector: String,
    @Factory.Parameter @Named("FromMethod") val stringFromMethod: String
  )

  class MultipleMethodTarget4 @Factory.Inject private constructor(
    @Named("Child") val stringFromInjector: String,
    @Factory.Parameter @Named("FromMethod") val stringFromMethod: String
  ) {

    @Inject
    @field:Named("Child")
    val stringFromFieldInjection: String = inject()
    lateinit var stringFromMethodInjection: String
      private set

    @Inject
    private fun injectString(@Named("Child") string: String) {
      stringFromMethodInjection = string
    }
  }
}
