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

import io.michaelrocks.lightsaber.QualifiedInjectionTest.Qualifiers.*
import org.junit.Assert.assertEquals
import org.junit.Test
import javax.inject.Inject
import javax.inject.Qualifier
import kotlin.reflect.KClass

class QualifiedInjectionTest {
  @Test
  fun testConstructionInjection() {
    val module = QualifiedModule()
    val injector = Lightsaber.get().createInjector(module)
    val container = injector.getInstance<ConstructorInjectionContainer>()
    validateContainer(module, container)
  }

  @Test
  fun testFieldInjection() {
    val module = QualifiedModule()
    val injector = Lightsaber.get().createInjector(module)
    val container = FieldInjectionContainer()
    injector.injectMembers(container)
    validateContainer(module, container)
  }

  @Test
  fun testMethodInjection() {
    val module = QualifiedModule()
    val injector = Lightsaber.get().createInjector(module)
    val container = MethodInjectionContainer()
    injector.injectMembers(container)
    validateContainer(module, container)
  }

  private fun validateContainer(module: QualifiedModule, container: Container) {
    assertEquals(module.provideNoQualifier(), container.noQualifier)
    assertEquals(module.provideEmptyQualifier(), container.emptyQualifier)
    assertEquals(module.provideBooleanQualifier(), container.booleanQualifier)
    assertEquals(module.provideBooleanQualifierExplicit(), container.booleanQualifierExplicit)
    assertEquals(module.provideByteQualifier(), container.byteQualifier)
    assertEquals(module.provideByteQualifierExplicit(), container.byteQualifierExplicit)
    assertEquals(module.provideCharQualifier(), container.charQualifier)
    assertEquals(module.provideCharQualifierExplicit(), container.charQualifierExplicit)
    assertEquals(module.provideFloatQualifier(), container.floatQualifier)
    assertEquals(module.provideFloatQualifierExplicit(), container.floatQualifierExplicit)
    assertEquals(module.provideDoubleQualifier(), container.doubleQualifier)
    assertEquals(module.provideDoubleQualifierExplicit(), container.doubleQualifierExplicit)
    assertEquals(module.provideIntQualifier(), container.intQualifier)
    assertEquals(module.provideIntQualifierExplicit(), container.intQualifierExplicit)
    assertEquals(module.provideLongQualifier(), container.longQualifier)
    assertEquals(module.provideLongQualifierExplicit(), container.longQualifierExplicit)
    assertEquals(module.provideShortQualifier(), container.shortQualifier)
    assertEquals(module.provideShortQualifierExplicit(), container.shortQualifierExplicit)
    assertEquals(module.provideStringQualifier(), container.stringQualifier)
    assertEquals(module.provideStringQualifierExplicit(), container.stringQualifierExplicit)
    assertEquals(module.provideEnumQualifier(), container.enumQualifier)
    assertEquals(module.provideEnumQualifierExplicit(), container.enumQualifierExplicit)
    assertEquals(module.provideClassQualifier(), container.classQualifier)
    assertEquals(module.provideClassQualifierExplicit(), container.classQualifierExplicit)
    assertEquals(module.provideAnnotationQualifier(), container.annotationQualifier)
    assertEquals(module.provideAnnotationQualifierExplicit(), container.annotationQualifierExplicit)
    assertEquals(module.provideBooleanArrayQualifier(), container.booleanArrayQualifier)
    assertEquals(module.provideBooleanArrayQualifierExplicit(), container.booleanArrayQualifierExplicit)
    assertEquals(module.provideByteArrayQualifier(), container.byteArrayQualifier)
    assertEquals(module.provideByteArrayQualifierExplicit(), container.byteArrayQualifierExplicit)
    assertEquals(module.provideCharArrayQualifier(), container.charArrayQualifier)
    assertEquals(module.provideCharArrayQualifierExplicit(), container.charArrayQualifierExplicit)
    assertEquals(module.provideFloatArrayQualifier(), container.floatArrayQualifier)
    assertEquals(module.provideFloatArrayQualifierExplicit(), container.floatArrayQualifierExplicit)
    assertEquals(module.provideDoubleArrayQualifier(), container.doubleArrayQualifier)
    assertEquals(module.provideDoubleArrayQualifierExplicit(), container.doubleArrayQualifierExplicit)
    assertEquals(module.provideIntArrayQualifier(), container.intArrayQualifier)
    assertEquals(module.provideIntArrayQualifierExplicit(), container.intArrayQualifierExplicit)
    assertEquals(module.provideLongArrayQualifier(), container.longArrayQualifier)
    assertEquals(module.provideLongArrayQualifierExplicit(), container.longArrayQualifierExplicit)
    assertEquals(module.provideShortArrayQualifier(), container.shortArrayQualifier)
    assertEquals(module.provideShortArrayQualifierExplicit(), container.shortArrayQualifierExplicit)
    assertEquals(module.provideStringArrayQualifier(), container.stringArrayQualifier)
    assertEquals(module.provideStringArrayQualifierExplicit(), container.stringArrayQualifierExplicit)
    assertEquals(module.provideEnumArrayQualifier(), container.enumArrayQualifier)
    assertEquals(module.provideEnumArrayQualifierExplicit(), container.enumArrayQualifierExplicit)
    assertEquals(module.provideClassArrayQualifier(), container.classArrayQualifier)
    assertEquals(module.provideClassArrayQualifierExplicit(), container.classArrayQualifierExplicit)
    assertEquals(module.provideAnnotationArrayQualifier(), container.annotationArrayQualifier)
    assertEquals(module.provideAnnotationArrayQualifierExplicit(), container.annotationArrayQualifierExplicit)
  }

  @Module
  private class QualifiedModule {
    @Provides
    fun provideNoQualifier(): String = "NoQualifier"

    @Provides
    @EmptyQualifier
    fun provideEmptyQualifier(): String = "EmptyQualifier"

    @Provides
    @BooleanQualifier
    fun provideBooleanQualifier(): String = "BooleanQualifier"

    @Provides
    @BooleanQualifier(false)
    fun provideBooleanQualifierExplicit(): String = "BooleanQualifierExplicit"

    @Provides
    @ByteQualifier
    fun provideByteQualifier(): String = "ByteQualifier"

    @Provides
    @ByteQualifier(-42)
    fun provideByteQualifierExplicit(): String = "ByteQualifierExplicit"

    @Provides
    @CharQualifier
    fun provideCharQualifier(): String = "CharQualifier"

    @Provides
    @CharQualifier('y')
    fun provideCharQualifierExplicit(): String = "CharQualifierExplicit"

    @Provides
    @FloatQualifier
    fun provideFloatQualifier(): String = "FloatQualifier"

    @Provides
    @FloatQualifier(-0.0f)
    fun provideFloatQualifierExplicit(): String = "FloatQualifierExplicit"

    @Provides
    @DoubleQualifier
    fun provideDoubleQualifier(): String = "DoubleQualifier"

    @Provides
    @DoubleQualifier(-0.0)
    fun provideDoubleQualifierExplicit(): String = "DoubleQualifierExplicit"

    @Provides
    @IntQualifier
    fun provideIntQualifier(): String = "IntQualifier"

    @Provides
    @IntQualifier(-42)
    fun provideIntQualifierExplicit(): String = "IntQualifierExplicit"

    @Provides
    @LongQualifier
    fun provideLongQualifier(): String = "LongQualifier"

    @Provides
    @LongQualifier(-42L)
    fun provideLongQualifierExplicit(): String = "LongQualifierExplicit"

    @Provides
    @ShortQualifier
    fun provideShortQualifier(): String = "ShortQualifier"

    @Provides
    @ShortQualifier(-42)
    fun provideShortQualifierExplicit(): String = "ShortQualifierExplicit"

    @Provides
    @StringQualifier
    fun provideStringQualifier(): String = "StringQualifier"

    @Provides
    @StringQualifier("ExplicitValue")
    fun provideStringQualifierExplicit(): String = "StringQualifierExplicit"

    @Provides
    @EnumQualifier
    fun provideEnumQualifier(): String = "EnumQualifier"

    @Provides
    @EnumQualifier(AnnotationRetention.BINARY)
    fun provideEnumQualifierExplicit(): String = "EnumQualifierExplicit"

    @Provides
    @ClassQualifier
    fun provideClassQualifier(): String = "ClassQualifier"

    @Provides
    @ClassQualifier(String::class)
    fun provideClassQualifierExplicit(): String = "ClassQualifierExplicit"

    @Provides
    @AnnotationQualifier
    fun provideAnnotationQualifier(): String = "AnnotationQualifier"

    @Provides
    @AnnotationQualifier(IntQualifier(-42))
    fun provideAnnotationQualifierExplicit(): String = "AnnotationQualifierExplicit"

    @Provides
    @BooleanArrayQualifier
    fun provideBooleanArrayQualifier(): String = "BooleanArrayQualifier"

    @Provides
    @BooleanArrayQualifier(false)
    fun provideBooleanArrayQualifierExplicit(): String = "BooleanArrayQualifierExplicit"

    @Provides
    @ByteArrayQualifier
    fun provideByteArrayQualifier(): String = "ByteArrayQualifier"

    @Provides
    @ByteArrayQualifier(-42)
    fun provideByteArrayQualifierExplicit(): String = "ByteArrayQualifierExplicit"

    @Provides
    @CharArrayQualifier
    fun provideCharArrayQualifier(): String = "CharArrayQualifier"

    @Provides
    @CharArrayQualifier('y')
    fun provideCharArrayQualifierExplicit(): String = "CharArrayQualifierExplicit"

    @Provides
    @FloatArrayQualifier
    fun provideFloatArrayQualifier(): String = "FloatArrayQualifier"

    @Provides
    @FloatArrayQualifier(-0.0f)
    fun provideFloatArrayQualifierExplicit(): String = "FloatArrayQualifierExplicit"

    @Provides
    @DoubleArrayQualifier
    fun provideDoubleArrayQualifier(): String = "DoubleArrayQualifier"

    @Provides
    @DoubleArrayQualifier(-0.0)
    fun provideDoubleArrayQualifierExplicit(): String = "DoubleArrayQualifierExplicit"

    @Provides
    @IntArrayQualifier
    fun provideIntArrayQualifier(): String = "IntArrayQualifier"

    @Provides
    @IntArrayQualifier(-42)
    fun provideIntArrayQualifierExplicit(): String = "IntArrayQualifierExplicit"

    @Provides
    @LongArrayQualifier
    fun provideLongArrayQualifier(): String = "LongArrayQualifier"

    @Provides
    @LongArrayQualifier(-42L)
    fun provideLongArrayQualifierExplicit(): String = "LongArrayQualifierExplicit"

    @Provides
    @ShortArrayQualifier
    fun provideShortArrayQualifier(): String = "ShortArrayQualifier"

    @Provides
    @ShortArrayQualifier(-42)
    fun provideShortArrayQualifierExplicit(): String = "ShortArrayQualifierExplicit"

    @Provides
    @StringArrayQualifier
    fun provideStringArrayQualifier(): String = "StringArrayQualifier"

    @Provides
    @StringArrayQualifier("ExplicitValue")
    fun provideStringArrayQualifierExplicit(): String = "StringArrayQualifierExplicit"

    @Provides
    @EnumArrayQualifier
    fun provideEnumArrayQualifier(): String = "EnumArrayQualifier"

    @Provides
    @EnumArrayQualifier(AnnotationRetention.BINARY)
    fun provideEnumArrayQualifierExplicit(): String = "EnumArrayQualifierExplicit"

    @Provides
    @ClassArrayQualifier
    fun provideClassArrayQualifier(): String = "ClassArrayQualifier"

    @Provides
    @ClassArrayQualifier(String::class)
    fun provideClassArrayQualifierExplicit(): String = "ClassArrayQualifierExplicit"

    @Provides
    @AnnotationArrayQualifier
    fun provideAnnotationArrayQualifier(): String = "AnnotationArrayQualifier"

    @Provides
    @AnnotationArrayQualifier(IntQualifier(-42))
    fun provideAnnotationArrayQualifierExplicit(): String = "AnnotationArrayQualifierExplicit"
  }

  private interface Container {
    val noQualifier: String
    val emptyQualifier: String
    val booleanQualifier: String
    val booleanQualifierExplicit: String
    val byteQualifier: String
    val byteQualifierExplicit: String
    val charQualifier: String
    val charQualifierExplicit: String
    val floatQualifier: String
    val floatQualifierExplicit: String
    val doubleQualifier: String
    val doubleQualifierExplicit: String
    val intQualifier: String
    val intQualifierExplicit: String
    val longQualifier: String
    val longQualifierExplicit: String
    val shortQualifier: String
    val shortQualifierExplicit: String
    val stringQualifier: String
    val stringQualifierExplicit: String
    val enumQualifier: String
    val enumQualifierExplicit: String
    val classQualifier: String
    val classQualifierExplicit: String
    val annotationQualifier: String
    val annotationQualifierExplicit: String
    val booleanArrayQualifier: String
    val booleanArrayQualifierExplicit: String
    val byteArrayQualifier: String
    val byteArrayQualifierExplicit: String
    val charArrayQualifier: String
    val charArrayQualifierExplicit: String
    val floatArrayQualifier: String
    val floatArrayQualifierExplicit: String
    val doubleArrayQualifier: String
    val doubleArrayQualifierExplicit: String
    val intArrayQualifier: String
    val intArrayQualifierExplicit: String
    val longArrayQualifier: String
    val longArrayQualifierExplicit: String
    val shortArrayQualifier: String
    val shortArrayQualifierExplicit: String
    val stringArrayQualifier: String
    val stringArrayQualifierExplicit: String
    val enumArrayQualifier: String
    val enumArrayQualifierExplicit: String
    val classArrayQualifier: String
    val classArrayQualifierExplicit: String
    val annotationArrayQualifier: String
    val annotationArrayQualifierExplicit: String
  }

  private class ConstructorInjectionContainer @Inject constructor(
      override val noQualifier: String,
      @EmptyQualifier
      override val emptyQualifier: String,
      @BooleanQualifier
      override val booleanQualifier: String,
      @BooleanQualifier(false)
      override val booleanQualifierExplicit: String,
      @ByteQualifier
      override val byteQualifier: String,
      @ByteQualifier(-42)
      override val byteQualifierExplicit: String,
      @CharQualifier
      override val charQualifier: String,
      @CharQualifier('y')
      override val charQualifierExplicit: String,
      @FloatQualifier
      override val floatQualifier: String,
      @FloatQualifier(-0.0f)
      override val floatQualifierExplicit: String,
      @DoubleQualifier
      override val doubleQualifier: String,
      @DoubleQualifier(-0.0)
      override val doubleQualifierExplicit: String,
      @IntQualifier
      override val intQualifier: String,
      @IntQualifier(-42)
      override val intQualifierExplicit: String,
      @LongQualifier
      override val longQualifier: String,
      @LongQualifier(-42L)
      override val longQualifierExplicit: String,
      @ShortQualifier
      override val shortQualifier: String,
      @ShortQualifier(-42)
      override val shortQualifierExplicit: String,
      @StringQualifier
      override val stringQualifier: String,
      @StringQualifier("ExplicitValue")
      override val stringQualifierExplicit: String,
      @EnumQualifier
      override val enumQualifier: String,
      @EnumQualifier(AnnotationRetention.BINARY)
      override val enumQualifierExplicit: String,
      @ClassQualifier
      override val classQualifier: String,
      @ClassQualifier(String::class)
      override val classQualifierExplicit: String,
      @AnnotationQualifier
      override val annotationQualifier: String,
      @AnnotationQualifier(IntQualifier(-42))
      override val annotationQualifierExplicit: String,
      @BooleanArrayQualifier
      override val booleanArrayQualifier: String,
      @BooleanArrayQualifier(false)
      override val booleanArrayQualifierExplicit: String,
      @ByteArrayQualifier
      override val byteArrayQualifier: String,
      @ByteArrayQualifier(-42)
      override val byteArrayQualifierExplicit: String,
      @CharArrayQualifier
      override val charArrayQualifier: String,
      @CharArrayQualifier('y')
      override val charArrayQualifierExplicit: String,
      @FloatArrayQualifier
      override val floatArrayQualifier: String,
      @FloatArrayQualifier(-0.0f)
      override val floatArrayQualifierExplicit: String,
      @DoubleArrayQualifier
      override val doubleArrayQualifier: String,
      @DoubleArrayQualifier(-0.0)
      override val doubleArrayQualifierExplicit: String,
      @IntArrayQualifier
      override val intArrayQualifier: String,
      @IntArrayQualifier(-42)
      override val intArrayQualifierExplicit: String,
      @LongArrayQualifier
      override val longArrayQualifier: String,
      @LongArrayQualifier(-42L)
      override val longArrayQualifierExplicit: String,
      @ShortArrayQualifier
      override val shortArrayQualifier: String,
      @ShortArrayQualifier(-42)
      override val shortArrayQualifierExplicit: String,
      @StringArrayQualifier
      override val stringArrayQualifier: String,
      @StringArrayQualifier("ExplicitValue")
      override val stringArrayQualifierExplicit: String,
      @EnumArrayQualifier
      override val enumArrayQualifier: String,
      @EnumArrayQualifier(AnnotationRetention.BINARY)
      override val enumArrayQualifierExplicit: String,
      @ClassArrayQualifier
      override val classArrayQualifier: String,
      @ClassArrayQualifier(String::class)
      override val classArrayQualifierExplicit: String,
      @AnnotationArrayQualifier
      override val annotationArrayQualifier: String,
      @AnnotationArrayQualifier(IntQualifier(-42))
      override val annotationArrayQualifierExplicit: String) : Container

  private class FieldInjectionContainer : Container {
    @Inject
    override lateinit var noQualifier: String

    @Inject
    @EmptyQualifier
    override lateinit var emptyQualifier: String

    @Inject
    @BooleanQualifier
    override lateinit var booleanQualifier: String

    @Inject
    @BooleanQualifier(false)
    override lateinit var booleanQualifierExplicit: String

    @Inject
    @ByteQualifier
    override lateinit var byteQualifier: String

    @Inject
    @ByteQualifier(-42)
    override lateinit var byteQualifierExplicit: String

    @Inject
    @CharQualifier
    override lateinit var charQualifier: String

    @Inject
    @CharQualifier('y')
    override lateinit var charQualifierExplicit: String

    @Inject
    @FloatQualifier
    override lateinit var floatQualifier: String

    @Inject
    @FloatQualifier(-0.0f)
    override lateinit var floatQualifierExplicit: String

    @Inject
    @DoubleQualifier
    override lateinit var doubleQualifier: String

    @Inject
    @DoubleQualifier(-0.0)
    override lateinit var doubleQualifierExplicit: String

    @Inject
    @IntQualifier
    override lateinit var intQualifier: String

    @Inject
    @IntQualifier(-42)
    override lateinit var intQualifierExplicit: String

    @Inject
    @LongQualifier
    override lateinit var longQualifier: String

    @Inject
    @LongQualifier(-42L)
    override lateinit var longQualifierExplicit: String

    @Inject
    @ShortQualifier
    override lateinit var shortQualifier: String

    @Inject
    @ShortQualifier(-42)
    override lateinit var shortQualifierExplicit: String

    @Inject
    @StringQualifier
    override lateinit var stringQualifier: String

    @Inject
    @StringQualifier("ExplicitValue")
    override lateinit var stringQualifierExplicit: String

    @Inject
    @EnumQualifier
    override lateinit var enumQualifier: String

    @Inject
    @EnumQualifier(AnnotationRetention.BINARY)
    override lateinit var enumQualifierExplicit: String

    @Inject
    @ClassQualifier
    override lateinit var classQualifier: String

    @Inject
    @ClassQualifier(String::class)
    override lateinit var classQualifierExplicit: String

    @Inject
    @AnnotationQualifier
    override lateinit var annotationQualifier: String

    @Inject
    @AnnotationQualifier(IntQualifier(-42))
    override lateinit var annotationQualifierExplicit: String

    @Inject
    @BooleanArrayQualifier
    override lateinit var booleanArrayQualifier: String

    @Inject
    @BooleanArrayQualifier(false)
    override lateinit var booleanArrayQualifierExplicit: String

    @Inject
    @ByteArrayQualifier
    override lateinit var byteArrayQualifier: String

    @Inject
    @ByteArrayQualifier(-42)
    override lateinit var byteArrayQualifierExplicit: String

    @Inject
    @CharArrayQualifier
    override lateinit var charArrayQualifier: String

    @Inject
    @CharArrayQualifier('y')
    override lateinit var charArrayQualifierExplicit: String

    @Inject
    @FloatArrayQualifier
    override lateinit var floatArrayQualifier: String

    @Inject
    @FloatArrayQualifier(-0.0f)
    override lateinit var floatArrayQualifierExplicit: String

    @Inject
    @DoubleArrayQualifier
    override lateinit var doubleArrayQualifier: String

    @Inject
    @DoubleArrayQualifier(-0.0)
    override lateinit var doubleArrayQualifierExplicit: String

    @Inject
    @IntArrayQualifier
    override lateinit var intArrayQualifier: String

    @Inject
    @IntArrayQualifier(-42)
    override lateinit var intArrayQualifierExplicit: String

    @Inject
    @LongArrayQualifier
    override lateinit var longArrayQualifier: String

    @Inject
    @LongArrayQualifier(-42L)
    override lateinit var longArrayQualifierExplicit: String

    @Inject
    @ShortArrayQualifier
    override lateinit var shortArrayQualifier: String

    @Inject
    @ShortArrayQualifier(-42)
    override lateinit var shortArrayQualifierExplicit: String

    @Inject
    @StringArrayQualifier
    override lateinit var stringArrayQualifier: String

    @Inject
    @StringArrayQualifier("ExplicitValue")
    override lateinit var stringArrayQualifierExplicit: String

    @Inject
    @EnumArrayQualifier
    override lateinit var enumArrayQualifier: String

    @Inject
    @EnumArrayQualifier(AnnotationRetention.BINARY)
    override lateinit var enumArrayQualifierExplicit: String

    @Inject
    @ClassArrayQualifier
    override lateinit var classArrayQualifier: String

    @Inject
    @ClassArrayQualifier(String::class)
    override lateinit var classArrayQualifierExplicit: String

    @Inject
    @AnnotationArrayQualifier
    override lateinit var annotationArrayQualifier: String

    @Inject
    @AnnotationArrayQualifier(IntQualifier(-42))
    override lateinit var annotationArrayQualifierExplicit: String
  }

  private class MethodInjectionContainer : Container {
    @set:Inject
    override lateinit var noQualifier: String

    @set:Inject
    @setparam:EmptyQualifier
    override lateinit var emptyQualifier: String

    @set:Inject
    @setparam:BooleanQualifier
    override lateinit var booleanQualifier: String

    @set:Inject
    @setparam:BooleanQualifier(false)
    override lateinit var booleanQualifierExplicit: String

    @set:Inject
    @setparam:ByteQualifier
    override lateinit var byteQualifier: String

    @set:Inject
    @setparam:ByteQualifier(-42)
    override lateinit var byteQualifierExplicit: String

    @set:Inject
    @setparam:CharQualifier
    override lateinit var charQualifier: String

    @set:Inject
    @setparam:CharQualifier('y')
    override lateinit var charQualifierExplicit: String

    @set:Inject
    @setparam:FloatQualifier
    override lateinit var floatQualifier: String

    @set:Inject
    @setparam:FloatQualifier(-0.0f)
    override lateinit var floatQualifierExplicit: String

    @set:Inject
    @setparam:DoubleQualifier
    override lateinit var doubleQualifier: String

    @set:Inject
    @setparam:DoubleQualifier(-0.0)
    override lateinit var doubleQualifierExplicit: String

    @set:Inject
    @setparam:IntQualifier
    override lateinit var intQualifier: String

    @set:Inject
    @setparam:IntQualifier(-42)
    override lateinit var intQualifierExplicit: String

    @set:Inject
    @setparam:LongQualifier
    override lateinit var longQualifier: String

    @set:Inject
    @setparam:LongQualifier(-42L)
    override lateinit var longQualifierExplicit: String

    @set:Inject
    @setparam:ShortQualifier
    override lateinit var shortQualifier: String

    @set:Inject
    @setparam:ShortQualifier(-42)
    override lateinit var shortQualifierExplicit: String

    @set:Inject
    @setparam:StringQualifier
    override lateinit var stringQualifier: String

    @set:Inject
    @setparam:StringQualifier("ExplicitValue")
    override lateinit var stringQualifierExplicit: String

    @set:Inject
    @setparam:EnumQualifier
    override lateinit var enumQualifier: String

    @set:Inject
    @setparam:EnumQualifier(AnnotationRetention.BINARY)
    override lateinit var enumQualifierExplicit: String

    @set:Inject
    @setparam:ClassQualifier
    override lateinit var classQualifier: String

    @set:Inject
    @setparam:ClassQualifier(String::class)
    override lateinit var classQualifierExplicit: String

    @set:Inject
    @setparam:AnnotationQualifier
    override lateinit var annotationQualifier: String

    @set:Inject
    @setparam:AnnotationQualifier(IntQualifier(-42))
    override lateinit var annotationQualifierExplicit: String

    @set:Inject
    @setparam:BooleanArrayQualifier
    override lateinit var booleanArrayQualifier: String

    @set:Inject
    @setparam:BooleanArrayQualifier(false)
    override lateinit var booleanArrayQualifierExplicit: String

    @set:Inject
    @setparam:ByteArrayQualifier
    override lateinit var byteArrayQualifier: String

    @set:Inject
    @setparam:ByteArrayQualifier(-42)
    override lateinit var byteArrayQualifierExplicit: String

    @set:Inject
    @setparam:CharArrayQualifier
    override lateinit var charArrayQualifier: String

    @set:Inject
    @setparam:CharArrayQualifier('y')
    override lateinit var charArrayQualifierExplicit: String

    @set:Inject
    @setparam:FloatArrayQualifier
    override lateinit var floatArrayQualifier: String

    @set:Inject
    @setparam:FloatArrayQualifier(-0.0f)
    override lateinit var floatArrayQualifierExplicit: String

    @set:Inject
    @setparam:DoubleArrayQualifier
    override lateinit var doubleArrayQualifier: String

    @set:Inject
    @setparam:DoubleArrayQualifier(-0.0)
    override lateinit var doubleArrayQualifierExplicit: String

    @set:Inject
    @setparam:IntArrayQualifier
    override lateinit var intArrayQualifier: String

    @set:Inject
    @setparam:IntArrayQualifier(-42)
    override lateinit var intArrayQualifierExplicit: String

    @set:Inject
    @setparam:LongArrayQualifier
    override lateinit var longArrayQualifier: String

    @set:Inject
    @setparam:LongArrayQualifier(-42L)
    override lateinit var longArrayQualifierExplicit: String

    @set:Inject
    @setparam:ShortArrayQualifier
    override lateinit var shortArrayQualifier: String

    @set:Inject
    @setparam:ShortArrayQualifier(-42)
    override lateinit var shortArrayQualifierExplicit: String

    @set:Inject
    @setparam:StringArrayQualifier
    override lateinit var stringArrayQualifier: String

    @set:Inject
    @setparam:StringArrayQualifier("ExplicitValue")
    override lateinit var stringArrayQualifierExplicit: String

    @set:Inject
    @setparam:EnumArrayQualifier
    override lateinit var enumArrayQualifier: String

    @set:Inject
    @setparam:EnumArrayQualifier(AnnotationRetention.BINARY)
    override lateinit var enumArrayQualifierExplicit: String

    @set:Inject
    @setparam:ClassArrayQualifier
    override lateinit var classArrayQualifier: String

    @set:Inject
    @setparam:ClassArrayQualifier(String::class)
    override lateinit var classArrayQualifierExplicit: String

    @set:Inject
    @setparam:AnnotationArrayQualifier
    override lateinit var annotationArrayQualifier: String

    @set:Inject
    @setparam:AnnotationArrayQualifier(IntQualifier(-42))
    override lateinit var annotationArrayQualifierExplicit: String
  }

  interface Qualifiers {
    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class EmptyQualifier

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class BooleanQualifier(val value: Boolean = true)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ByteQualifier(val value: Byte = 42)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class CharQualifier(val value: Char = 'x')

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class FloatQualifier(val value: Float = Math.E.toFloat())

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class DoubleQualifier(val value: Double = Math.PI)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class IntQualifier(val value: Int = 42)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class LongQualifier(val value: Long = 42L)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ShortQualifier(val value: Short = 42)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class StringQualifier(val value: String = "Value")

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class EnumQualifier(val value: AnnotationRetention = AnnotationRetention.RUNTIME)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ClassQualifier(val value: KClass<*> = Any::class)

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class AnnotationQualifier(val value: IntQualifier = IntQualifier())

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class BooleanArrayQualifier(vararg val value: Boolean = booleanArrayOf(true))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ByteArrayQualifier(vararg val value: Byte = byteArrayOf(42))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class CharArrayQualifier(vararg val value: Char = charArrayOf('x'))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class FloatArrayQualifier(vararg val value: Float = floatArrayOf(0.0f))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class DoubleArrayQualifier(vararg val value: Double = doubleArrayOf(0.0))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class IntArrayQualifier(vararg val value: Int = intArrayOf(42))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class LongArrayQualifier(vararg val value: Long = longArrayOf(42L))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ShortArrayQualifier(vararg val value: Short = shortArrayOf(42))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class StringArrayQualifier(vararg val value: String = arrayOf("Value"))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class EnumArrayQualifier(vararg val value: AnnotationRetention = arrayOf(AnnotationRetention.RUNTIME))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ClassArrayQualifier(vararg val value: KClass<*> = arrayOf(Any::class))

    @Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class AnnotationArrayQualifier(vararg val value: IntQualifier = arrayOf(IntQualifier()))
  }
}
