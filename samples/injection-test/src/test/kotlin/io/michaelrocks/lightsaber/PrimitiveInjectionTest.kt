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
import org.junit.Assert.assertNotNull
import org.junit.Test
import javax.inject.Inject

class PrimitiveInjectionTest {
  @Test
  fun testUnboxedIntoUnboxedInjection() {
    val injector = Lightsaber.get().createInjector(UnboxedPrimitiveComponent())
    val container = UnboxedInjectableContainer()
    injector.injectMembers(container)

    val module = UnboxedPrimitiveModule()
    assertEquals(module.provideBoolean(), container.booleanField)
    assertEquals(module.provideByte().toLong(), container.byteField.toLong())
    assertEquals(module.provideChar().toLong(), container.charField.toLong())
    assertEquals(module.provideDouble(), container.doubleField, java.lang.Double.MIN_VALUE)
    assertEquals(module.provideFloat(), container.floatField, java.lang.Float.MIN_VALUE)
    assertEquals(module.provideInt().toLong(), container.intField.toLong())
    assertEquals(module.provideLong(), container.longField)
    assertEquals(module.provideShort().toLong(), container.shortField.toLong())
  }

  @Test
  fun testUnboxedIntoBoxedInjection() {
    val injector = Lightsaber.get().createInjector(UnboxedPrimitiveComponent())
    val container = BoxedInjectableContainer()
    injector.injectMembers(container)

    val module = UnboxedPrimitiveModule()
    assertEquals(module.provideBoolean(), container.booleanField)
    assertEquals(module.provideByte().toLong(), (container.byteField as Byte).toLong())
    assertEquals(module.provideChar().toLong(), (container.characterField as Char).toLong())
    assertEquals(module.provideDouble(), container.doubleField!!, java.lang.Double.MIN_VALUE)
    assertEquals(module.provideFloat(), container.floatField!!, java.lang.Float.MIN_VALUE)
    assertEquals(module.provideInt().toLong(), (container.integerField as Int).toLong())
    assertEquals(module.provideLong(), container.longField as Long)
    assertEquals(module.provideShort().toLong(), (container.shortField as Short).toLong())
  }

  @Test
  fun testBoxedIntoUnboxedInjection() {
    val injector = Lightsaber.get().createInjector(BoxedPrimitiveComponent())
    val container = UnboxedInjectableContainer()
    injector.injectMembers(container)

    val module = BoxedPrimitiveModule()
    assertEquals(module.provideBoolean(), container.booleanField)
    assertEquals(module.provideByte(), java.lang.Byte.valueOf(container.byteField))
    assertEquals(module.provideCharacter(), Character.valueOf(container.charField))
    assertEquals(module.provideDouble(), java.lang.Double.valueOf(container.doubleField))
    assertEquals(module.provideFloat(), java.lang.Float.valueOf(container.floatField))
    assertEquals(module.provideInteger(), Integer.valueOf(container.intField))
    assertEquals(module.provideLong(), java.lang.Long.valueOf(container.longField))
    assertEquals(module.provideShort(), java.lang.Short.valueOf(container.shortField))
  }

  @Test
  fun testBoxedIntoBoxedInjection() {
    val injector = Lightsaber.get().createInjector(BoxedPrimitiveComponent())
    val container = BoxedInjectableContainer()
    injector.injectMembers(container)

    val module = BoxedPrimitiveModule()
    assertEquals(module.provideBoolean(), container.booleanField)
    assertEquals(module.provideByte(), container.byteField)
    assertEquals(module.provideCharacter(), container.characterField)
    assertEquals(module.provideDouble(), container.doubleField)
    assertEquals(module.provideFloat(), container.floatField)
    assertEquals(module.provideInteger(), container.integerField)
    assertEquals(module.provideLong(), container.longField)
    assertEquals(module.provideShort(), container.shortField)
  }

  @Test
  fun testUnboxedForUnboxedConstruction() {
    val injector = Lightsaber.get().createInjector(UnboxedPrimitiveComponent())
    val container = injector.getInstance<UnboxedConstructableContainer>()

    val module = UnboxedPrimitiveModule()
    assertEquals(module.provideBoolean(), container.booleanField)
    assertEquals(module.provideByte().toLong(), container.byteField.toLong())
    assertEquals(module.provideChar().toLong(), container.charField.toLong())
    assertEquals(module.provideDouble(), container.doubleField, java.lang.Double.MIN_VALUE)
    assertEquals(module.provideFloat(), container.floatField, java.lang.Float.MIN_VALUE)
    assertEquals(module.provideInt().toLong(), container.intField.toLong())
    assertEquals(module.provideLong(), container.longField)
    assertEquals(module.provideShort().toLong(), container.shortField.toLong())
  }

  @Test
  fun testUnboxedForBoxedConstruction() {
    val injector = Lightsaber.get().createInjector(UnboxedPrimitiveComponent())
    val container = injector.getInstance<BoxedConstructableContainer>()

    val module = UnboxedPrimitiveModule()
    assertEquals(module.provideBoolean(), container.booleanField)
    assertEquals(module.provideByte().toLong(), (container.byteField as Byte).toLong())
    assertEquals(module.provideChar().toLong(), (container.characterField as Char).toLong())
    assertEquals(module.provideDouble(), container.doubleField!!, java.lang.Double.MIN_VALUE)
    assertEquals(module.provideFloat(), container.floatField!!, java.lang.Float.MIN_VALUE)
    assertEquals(module.provideInt().toLong(), (container.integerField as Int).toLong())
    assertEquals(module.provideLong(), container.longField as Long)
    assertEquals(module.provideShort().toLong(), (container.shortField as Short).toLong())
  }

  @Test
  fun testBoxedForUnboxedConstruction() {
    val injector = Lightsaber.get().createInjector(BoxedPrimitiveComponent())
    val container = injector.getInstance<UnboxedConstructableContainer>()

    val module = BoxedPrimitiveModule()
    assertEquals(module.provideBoolean(), container.booleanField)
    assertEquals(module.provideByte(), java.lang.Byte.valueOf(container.byteField))
    assertEquals(module.provideCharacter(), Character.valueOf(container.charField))
    assertEquals(module.provideDouble(), java.lang.Double.valueOf(container.doubleField))
    assertEquals(module.provideFloat(), java.lang.Float.valueOf(container.floatField))
    assertEquals(module.provideInteger(), Integer.valueOf(container.intField))
    assertEquals(module.provideLong(), java.lang.Long.valueOf(container.longField))
    assertEquals(module.provideShort(), java.lang.Short.valueOf(container.shortField))
  }

  @Test
  fun testBoxedForBoxedConstruction() {
    val injector = Lightsaber.get().createInjector(BoxedPrimitiveComponent())
    val container = injector.getInstance<BoxedConstructableContainer>()

    val module = BoxedPrimitiveModule()
    assertEquals(module.provideBoolean(), container.booleanField)
    assertEquals(module.provideByte(), container.byteField)
    assertEquals(module.provideCharacter(), container.characterField)
    assertEquals(module.provideDouble(), container.doubleField)
    assertEquals(module.provideFloat(), container.floatField)
    assertEquals(module.provideInteger(), container.integerField)
    assertEquals(module.provideLong(), container.longField)
    assertEquals(module.provideShort(), container.shortField)
  }

  @Test
  fun testUnboxedProvision() {
    val injector = Lightsaber.get().createInjector(UnboxedPrimitiveComponent())

    val unboxedResult = injector.getInstance<UnboxedResult>()
    val boxedResult = injector.getInstance<BoxedResult>()

    assertNotNull(unboxedResult)
    assertNotNull(boxedResult)
  }

  @Test
  fun testBoxedProvision() {
    val injector = Lightsaber.get().createInjector(BoxedPrimitiveComponent())

    val unboxedResult = injector.getInstance<UnboxedResult>()
    val boxedResult = injector.getInstance<BoxedResult>()

    assertNotNull(unboxedResult)
    assertNotNull(boxedResult)
  }

  private class UnboxedInjectableContainer {
    @Inject var booleanField: Boolean = false
    @Inject var byteField: Byte = 0
    @Inject var charField: Char = ' '
    @Inject var doubleField: Double = 0.toDouble()
    @Inject var floatField: Float = 0.toFloat()
    @Inject var intField: Int = 0
    @Inject var longField: Long = 0
    @Inject var shortField: Short = 0

    @Inject fun unboxedMethod(booleanArg: Boolean, byteArg: Byte, charArg: Char, doubleArg: Double,
        floatArg: Float, intArg: Int, longArg: Long, shortArg: Short) {
      assertEquals(booleanField, booleanArg)
      assertEquals(byteField.toLong(), byteArg.toLong())
      assertEquals(charField.toLong(), charArg.toLong())
      assertEquals(doubleField, doubleArg, java.lang.Double.MIN_VALUE)
      assertEquals(floatField, floatArg, java.lang.Float.MIN_VALUE)
      assertEquals(intField.toLong(), intArg.toLong())
      assertEquals(longField, longArg)
      assertEquals(shortField.toLong(), shortArg.toLong())
    }

    @Inject fun boxedMethod(booleanArg: Boolean?, byteArg: Byte?, characterArg: Char?,
        doubleArg: Double?, floatArg: Float?, integerArg: Int?, longArg: Long?,
        shortArg: Short?) {
      assertEquals(booleanField, booleanArg)
      assertEquals(byteField.toLong(), (byteArg as Byte).toLong())
      assertEquals(charField.toLong(), (characterArg as Char).toLong())
      assertEquals(doubleField, doubleArg!!, java.lang.Double.MIN_VALUE)
      assertEquals(floatField, floatArg!!, java.lang.Float.MIN_VALUE)
      assertEquals(intField.toLong(), (integerArg as Int).toLong())
      assertEquals(longField, longArg as Long)
      assertEquals(shortField.toLong(), (shortArg as Short).toLong())
    }
  }

  private class BoxedInjectableContainer {
    @Inject var booleanField: Boolean? = null
    @Inject var byteField: Byte? = null
    @Inject var characterField: Char? = null
    @Inject var doubleField: Double? = null
    @Inject var floatField: Float? = null
    @Inject var integerField: Int? = null
    @Inject var longField: Long? = null
    @Inject var shortField: Short? = null

    @Inject fun unboxedMethod(booleanArg: Boolean, byteArg: Byte, charArg: Char, doubleArg: Double,
        floatArg: Float, intArg: Int, longArg: Long, shortArg: Short) {
      assertEquals(booleanField, booleanArg)
      assertEquals(byteField, java.lang.Byte.valueOf(byteArg))
      assertEquals(characterField, Character.valueOf(charArg))
      assertEquals(doubleField, java.lang.Double.valueOf(doubleArg))
      assertEquals(floatField, java.lang.Float.valueOf(floatArg))
      assertEquals(integerField, Integer.valueOf(intArg))
      assertEquals(longField, java.lang.Long.valueOf(longArg))
      assertEquals(shortField, java.lang.Short.valueOf(shortArg))
    }

    @Inject fun boxedMethod(
        booleanArg: Boolean?,
        byteArg: Byte?,
        characterArg: Char?,
        doubleArg: Double?,
        floatArg: Float?,
        integerArg: Int?,
        longArg: Long?,
        shortArg: Short?
    ) {
      assertEquals(booleanField, booleanArg)
      assertEquals(byteField, byteArg)
      assertEquals(characterField, characterArg)
      assertEquals(doubleField, doubleArg)
      assertEquals(floatField, floatArg)
      assertEquals(integerField, integerArg)
      assertEquals(longField, longArg)
      assertEquals(shortField, shortArg)
    }
  }

  @Module
  private class UnboxedPrimitiveModule {
    @Provides fun provideBoolean(): Boolean = true

    @Provides fun provideByte(): Byte = 42

    @Provides fun provideChar(): Char = 'x'

    @Provides fun provideDouble(): Double = Math.PI

    @Provides fun provideFloat(): Float = Math.E.toFloat()

    @Provides fun provideInt(): Int = 42424242

    @Provides fun provideLong(): Long = 4242424242424242L

    @Provides fun provideShort(): Short = 4242

    @Provides fun consumeUnboxed(
        booleanArg: Boolean,
        byteArg: Byte,
        charArg: Char,
        doubleArg: Double,
        floatArg: Float,
        intArg: Int,
        longArg: Long,
        shortArg: Short
    ): UnboxedResult {
      assertEquals(provideBoolean(), booleanArg)
      assertEquals(provideByte().toLong(), byteArg.toLong())
      assertEquals(provideChar().toLong(), charArg.toLong())
      assertEquals(provideDouble(), doubleArg, java.lang.Double.MIN_VALUE)
      assertEquals(provideFloat(), floatArg, java.lang.Float.MIN_VALUE)
      assertEquals(provideInt().toLong(), intArg.toLong())
      assertEquals(provideLong(), longArg)
      assertEquals(provideShort().toLong(), shortArg.toLong())
      return UnboxedResult()
    }

    @Provides fun consumeBoxed(
        booleanArg: Boolean?,
        byteArg: Byte?,
        characterArg: Char?,
        doubleArg: Double?,
        floatArg: Float?,
        integerArg: Int?,
        longArg: Long?,
        shortArg: Short?
    ): BoxedResult {
      assertEquals(provideBoolean(), booleanArg)
      assertEquals(provideByte().toLong(), (byteArg as Byte).toLong())
      assertEquals(provideChar().toLong(), (characterArg as Char).toLong())
      assertEquals(provideDouble(), doubleArg!!, java.lang.Double.MIN_VALUE)
      assertEquals(provideFloat(), floatArg!!, java.lang.Float.MIN_VALUE)
      assertEquals(provideInt().toLong(), (integerArg as Int).toLong())
      assertEquals(provideLong(), longArg as Long)
      assertEquals(provideShort().toLong(), (shortArg as Short).toLong())
      return BoxedResult()
    }
  }

  @Module
  private class BoxedPrimitiveModule {
    @Provides fun provideBoolean(): Boolean? = false

    @Provides fun provideByte(): Byte? = -42

    @Provides fun provideCharacter(): Char? = 'X'

    @Provides fun provideDouble(): Double? = -Math.PI

    @Provides fun provideFloat(): Float? = (-Math.E).toFloat()

    @Provides fun provideInteger(): Int? = -42424242

    @Provides fun provideLong(): Long? = -4242424242424242L

    @Provides fun provideShort(): Short? = -4242

    @Provides fun consumeUnboxed(
        booleanArg: Boolean,
        byteArg: Byte,
        charArg: Char,
        doubleArg: Double,
        floatArg: Float,
        intArg: Int,
        longArg: Long,
        shortArg: Short
    ): UnboxedResult {
      assertEquals(provideBoolean(), booleanArg)
      assertEquals(provideByte(), java.lang.Byte.valueOf(byteArg))
      assertEquals(provideCharacter(), Character.valueOf(charArg))
      assertEquals(provideDouble(), java.lang.Double.valueOf(doubleArg))
      assertEquals(provideFloat(), java.lang.Float.valueOf(floatArg))
      assertEquals(provideInteger(), Integer.valueOf(intArg))
      assertEquals(provideLong(), java.lang.Long.valueOf(longArg))
      assertEquals(provideShort(), java.lang.Short.valueOf(shortArg))
      return UnboxedResult()
    }

    @Provides fun consumeBoxed(
        booleanArg: Boolean?,
        byteArg: Byte?,
        characterArg: Char?,
        doubleArg: Double?,
        floatArg: Float?,
        integerArg: Int?,
        longArg: Long?,
        shortArg: Short?
    ): BoxedResult {
      assertEquals(provideBoolean(), booleanArg)
      assertEquals(provideByte(), byteArg)
      assertEquals(provideCharacter(), characterArg)
      assertEquals(provideDouble(), doubleArg)
      assertEquals(provideFloat(), floatArg)
      assertEquals(provideInteger(), integerArg)
      assertEquals(provideLong(), longArg)
      assertEquals(provideShort(), shortArg)
      return BoxedResult()
    }
  }

  @Component(root = true)
  private class UnboxedPrimitiveComponent {
    @Provides
    private fun provideUnboxedPrimitiveModule(): UnboxedPrimitiveModule = UnboxedPrimitiveModule()
  }

  @Component(root = true)
  private class BoxedPrimitiveComponent {
    @Provides
    private fun provideBoxedPrimitiveModule(): BoxedPrimitiveModule = BoxedPrimitiveModule()
  }

  private class UnboxedResult

  private class BoxedResult

  private class UnboxedConstructableContainer @Inject constructor(
      val booleanField: Boolean,
      val byteField: Byte,
      val charField: Char,
      val doubleField: Double,
      val floatField: Float,
      val intField: Int,
      val longField: Long,
      val shortField: Short
  )

  private class BoxedConstructableContainer @Inject constructor(
      val booleanField: Boolean?,
      val byteField: Byte?,
      val characterField: Char?,
      val doubleField: Double?,
      val floatField: Float?,
      val integerField: Int?,
      val longField: Long?,
      val shortField: Short?
  )
}
