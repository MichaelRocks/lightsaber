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

package io.michaelrocks.lightsaber.processor.annotations.proxy

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.MethodMirror
import io.michaelrocks.lightsaber.mockito.*
import io.michaelrocks.lightsaber.processor.annotations.proxy.Annotations.*
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.internalName
import io.michaelrocks.lightsaber.processor.commons.type
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import java.lang.annotation.RetentionPolicy
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*

class AnnotationProxyGeneratorTest {
  companion object {
    @field:EmptyAnnotation
    @field:BooleanAnnotation
    @field:ByteAnnotation
    @field:CharAnnotation
    @field:FloatAnnotation
    @field:DoubleAnnotation
    @field:IntAnnotation
    @field:LongAnnotation
    @field:ShortAnnotation
    @field:StringAnnotation
    @field:EnumAnnotation
    @field:ClassAnnotation
    @field:AnnotationAnnotation
    @field:BooleanArrayAnnotation
    @field:ByteArrayAnnotation
    @field:CharArrayAnnotation
    @field:FloatArrayAnnotation
    @field:DoubleArrayAnnotation
    @field:IntArrayAnnotation
    @field:LongArrayAnnotation
    @field:ShortArrayAnnotation
    @field:StringArrayAnnotation
    @field:EnumArrayAnnotation
    @field:ClassArrayAnnotation
    @field:AnnotationArrayAnnotation
    @field:CompositeAnnotation
    private val ANNOTATION_HOLDER = Object()

    private fun getAnnotationProxyClassName(annotationClass: Class<out Annotation>): String {
      return annotationClass.simpleName + "Proxy"
    }

    private fun getAnnotationDescriptor(annotationClass: Class<out Annotation>): ClassMirror {
      val orderedMethods = TreeMap<Int, Method>()
      for (method in annotationClass.declaredMethods) {
        val orderAnnotation = method.getAnnotation(Order::class.java)
        val order = if (orderAnnotation == null) 0 else orderAnnotation.value
        val oldMethod = orderedMethods.put(order, method)
        assertNull("Method order must be distinct", oldMethod)
      }

      return ClassMirror.Builder()
          .name(annotationClass.internalName)
          .run {
            for (method in orderedMethods.values) {
              addMethod(
                  MethodMirror.Builder()
                      .name(method.name)
                      .type(Type.getMethodType(method.returnType.type))
                      .build()
              )
            }
            build()
          }
    }

    private fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T {
      for (annotation in AnnotationProxyGeneratorTest::class.java.getDeclaredField("ANNOTATION_HOLDER").annotations) {
        if (annotation.annotationClass.java == annotationClass) {
          return annotationClass.cast(annotation)
        }
      }
      fail("Annotation for class $annotationClass not found")
      throw RuntimeException()
    }
  }

  private lateinit var classLoader: ArrayClassLoader

  @Before
  fun createClassLoader() {
    val annotations = Annotations()
    classLoader = ArrayClassLoader(annotations.javaClass.classLoader)
  }

  @Test
  fun testEqualsByReference() {
    val annotation = createAnnotationProxy<EmptyAnnotation>()
    assertEquals(annotation, annotation)
  }

  @Test
  fun testNotEqualsToNull() {
    @Suppress("SENSELESS_COMPARISON")
    assertFalse(createAnnotationProxy<EmptyAnnotation>() == null)
  }

  @Test
  fun testNotEqualsToOtherType() {
    assertNotEquals(Object(), createAnnotationProxy<EmptyAnnotation>())
    assertNotEquals(createAnnotationProxy<IntAnnotation>(42), createAnnotationProxy<EmptyAnnotation>())
  }

  @Test
  fun testEmptyAnnotation() {
    assertAnnotationEquals<EmptyAnnotation>()
  }

  @Test
  fun testBooleanAnnotation() {
    assertAnnotationEquals<BooleanAnnotation>(true)
    assertAnnotationNotEquals<BooleanAnnotation>(false)
  }

  @Test
  fun testByteAnnotation() {
    assertAnnotationEquals<ByteAnnotation>(42.toByte())
    assertAnnotationNotEquals<ByteAnnotation>((-42).toByte())
  }

  @Test
  fun testCharAnnotation() {
    assertAnnotationEquals<CharAnnotation>('x')
    assertAnnotationNotEquals<CharAnnotation>(' ')
  }

  @Test
  fun testFloatAnnotation() {
    assertAnnotationEquals<FloatAnnotation>(2.7182818284590452354f)
    assertAnnotationNotEquals<FloatAnnotation>(Float.NaN)
  }

  @Test
  fun testDoubleAnnotation() {
    assertAnnotationEquals<DoubleAnnotation>(3.14159265358979323846)
    assertAnnotationNotEquals<DoubleAnnotation>(Double.NaN)
  }

  @Test
  fun testIntAnnotation() {
    assertAnnotationEquals<IntAnnotation>(42)
    assertAnnotationNotEquals<IntAnnotation>(-42)
  }

  @Test
  fun testLongAnnotation() {
    assertAnnotationEquals<LongAnnotation>(42L)
    assertAnnotationNotEquals<LongAnnotation>(-43L)
  }

  @Test
  fun testShortAnnotation() {
    assertAnnotationEquals<ShortAnnotation>(42.toShort())
    assertAnnotationNotEquals<ShortAnnotation>((-42).toShort())
  }

  @Test
  fun testStringAnnotation() {
    assertAnnotationEquals<StringAnnotation>("Value")
    assertAnnotationNotEquals<StringAnnotation>("Value2")
  }

  @Test
  fun testEnumAnnotation() {
    assertAnnotationEquals<EnumAnnotation>(RetentionPolicy.RUNTIME)
    assertAnnotationNotEquals<EnumAnnotation>(RetentionPolicy.CLASS)
  }

  @Test
  fun testClassAnnotation() {
    assertAnnotationEquals<ClassAnnotation>(Any::class.java)
    assertAnnotationNotEquals<ClassAnnotation>(String::class.java)
  }

  @Test
  fun testAnnotationAnnotation() {
    assertAnnotationEquals<AnnotationAnnotation>(createAnnotationProxy<IntAnnotation>(42))
    assertAnnotationNotEquals<AnnotationAnnotation>(createAnnotationProxy<IntAnnotation>(-42))
  }

  @Test
  fun testBooleanArrayAnnotation() {
    assertAnnotationEquals<BooleanArrayAnnotation>(booleanArrayOf(true))
    assertAnnotationNotEquals<BooleanArrayAnnotation>(booleanArrayOf(false))
  }

  @Test
  fun testByteArrayAnnotation() {
    assertAnnotationEquals<ByteArrayAnnotation>(byteArrayOf(42))
    assertAnnotationNotEquals<ByteArrayAnnotation>(byteArrayOf(-42))
  }

  @Test
  fun testCharArrayAnnotation() {
    assertAnnotationEquals<CharArrayAnnotation>(charArrayOf('x'))
    assertAnnotationNotEquals<CharArrayAnnotation>(charArrayOf('y'))
  }

  @Test
  fun testFloatArrayAnnotation() {
    assertAnnotationEquals<FloatArrayAnnotation>(floatArrayOf(2.7182818284590452354f))
    assertAnnotationNotEquals<FloatArrayAnnotation>(floatArrayOf(Float.NaN))
  }

  @Test
  fun testDoubleArrayAnnotation() {
    assertAnnotationEquals<DoubleArrayAnnotation>(doubleArrayOf(3.14159265358979323846))
    assertAnnotationNotEquals<DoubleArrayAnnotation>(doubleArrayOf(Double.NaN))
  }

  @Test
  fun testIntArrayAnnotation() {
    assertAnnotationEquals<IntArrayAnnotation>(intArrayOf(42))
    assertAnnotationNotEquals<IntArrayAnnotation>(intArrayOf(-42))
  }

  @Test
  fun testLongArrayAnnotation() {
    assertAnnotationEquals<LongArrayAnnotation>(longArrayOf(42L))
    assertAnnotationNotEquals<LongArrayAnnotation>(longArrayOf(-42L))
  }

  @Test
  fun testShortArrayAnnotation() {
    assertAnnotationEquals<ShortArrayAnnotation>(shortArrayOf(42))
    assertAnnotationNotEquals<ShortArrayAnnotation>(shortArrayOf(-42))
  }

  @Test
  fun testStringArrayAnnotation() {
    assertAnnotationEquals<StringArrayAnnotation>(arrayOf("Value"))
    assertAnnotationNotEquals<StringArrayAnnotation>(arrayOf("Value2"))
  }

  @Test
  fun testEnumArrayAnnotation() {
    assertAnnotationEquals<EnumArrayAnnotation>(arrayOf(RetentionPolicy.RUNTIME))
    assertAnnotationNotEquals<EnumArrayAnnotation>(arrayOf(RetentionPolicy.CLASS))
  }

  @Test
  fun testClassArrayAnnotation() {
    assertAnnotationEquals<ClassArrayAnnotation>(arrayOf<Class<*>>(Any::class.java))
    assertAnnotationNotEquals<ClassArrayAnnotation>(arrayOf<Class<*>>(String::class.java))
  }

  @Test
  fun testAnnotationArrayAnnotation() {
    assertAnnotationEquals<AnnotationArrayAnnotation>(arrayOf(createAnnotationProxy<IntAnnotation>(42)))
    assertAnnotationNotEquals<AnnotationArrayAnnotation>(arrayOf(createAnnotationProxy<IntAnnotation>(-42)))
  }

  @Test
  fun testCompositeAnnotation() {
    assertAnnotationEquals<CompositeAnnotation>(
        true,
        42.toByte(),
        'x',
        2.7182818284590452354f,
        3.14159265358979323846,
        42,
        42L,
        42.toShort(),
        "Value",
        RetentionPolicy.RUNTIME,
        Any::class.java,
        createAnnotationProxy<IntAnnotation>(42),
        booleanArrayOf(true),
        byteArrayOf(42.toByte()),
        charArrayOf('x'),
        floatArrayOf(2.7182818284590452354f),
        doubleArrayOf(3.14159265358979323846),
        intArrayOf(42),
        longArrayOf(42L),
        shortArrayOf(42.toShort()),
        arrayOf("Value"),
        arrayOf(RetentionPolicy.RUNTIME),
        arrayOf<Class<*>>(Any::class.java),
        arrayOf(createAnnotationProxy<IntAnnotation>(42)))
  }

  @Test
  fun testEmptyToString() {
    val annotation = createAnnotationProxy<EmptyAnnotation>()
    val expected = "@${EmptyAnnotation::class.java.name}()"
    val actual = annotation.toString()
    assertEquals(expected, actual)
  }

  @Test
  fun testCompositeToString() {
    val annotation = createAnnotationProxy<CompositeAnnotation>(
        true,
        42.toByte(),
        'x',
        Float.NaN,
        Double.POSITIVE_INFINITY,
        42,
        42L,
        42.toShort(),
        "Value",
        RetentionPolicy.RUNTIME,
        Any::class.java,
        createAnnotationProxy<IntAnnotation>(42),
        booleanArrayOf(true, false),
        byteArrayOf(42, 43),
        charArrayOf('x', 'y'),
        floatArrayOf(Float.NaN, Float.POSITIVE_INFINITY),
        doubleArrayOf(Double.POSITIVE_INFINITY, Double.NaN),
        intArrayOf(42, 43),
        longArrayOf(42L, 43L),
        shortArrayOf(42, 43),
        arrayOf("Value1", "Value2"),
        arrayOf(RetentionPolicy.RUNTIME, RetentionPolicy.CLASS),
        arrayOf<Class<*>>(Any::class.java),
        arrayOf(createAnnotationProxy<IntAnnotation>(42), createAnnotationProxy<IntAnnotation>(43)))

    val expected = "@${CompositeAnnotation::class.java.name}(" +
        "booleanValue=true, " +
        "byteValue=42, " +
        "charValue=x, " +
        "floatValue=NaN, " +
        "doubleValue=Infinity, " +
        "intValue=42, " +
        "longValue=42, " +
        "shortValue=42, " +
        "stringValue=Value, " +
        "enumValue=RUNTIME, " +
        "classValue=class java.lang.Object, " +
        "annotationValue=@${IntAnnotation::class.java.name}(value=42), " +
        "booleanArrayValue=[true, false], " +
        "byteArrayValue=[42, 43], " +
        "charArrayValue=[x, y], " +
        "floatArrayValue=[NaN, Infinity], " +
        "doubleArrayValue=[Infinity, NaN], " +
        "intArrayValue=[42, 43], " +
        "longArrayValue=[42, 43], " +
        "shortArrayValue=[42, 43], " +
        "stringArrayValue=[Value1, Value2], " +
        "enumArrayValue=[RUNTIME, CLASS], " +
        "classArrayValue=[class java.lang.Object], " +
        "annotationArrayValue=[" +
        "@${IntAnnotation::class.java.name}(value=42), @${IntAnnotation::class.java.name}(value=43)]" +
        ")"
    val actual = annotation.toString()
    assertEquals(expected, actual)
  }

  @Test
  fun testHashCodeCaching() {
    val handler = mock<InvocationHandler>()
    val intAnnotation = newProxyInstance<IntAnnotation>(handler)
    val hashCodeMethod = Any::class.java.getDeclaredMethod("hashCode")
    given(handler.invoke(intAnnotation, hashCodeMethod, null)).thenReturn(0)

    val annotation = createAnnotationProxy<AnnotationAnnotation>(intAnnotation)
    annotation.hashCode()
    annotation.hashCode()
    verify(handler, times(1)).invoke(intAnnotation, hashCodeMethod, null)
    verifyNoMoreInteractions(handler)
  }

  @Test
  fun testToStringCaching() {
    val handler = mock<InvocationHandler>()
    val intAnnotation = newProxyInstance<IntAnnotation>(handler)
    val toStringMethod = Any::class.java.getDeclaredMethod("toString")
    given(handler.invoke(intAnnotation, toStringMethod, null)).thenReturn("ToString")

    val annotation = createAnnotationProxy<AnnotationAnnotation>(intAnnotation)
    annotation.toString()
    annotation.toString()
    verify(handler, times(1)).invoke(intAnnotation, toStringMethod, null)
    verifyNoMoreInteractions(handler)
  }

  private inline fun <reified T : Annotation> assertAnnotationEquals(vararg values: Any) {
    val annotationClass = T::class.java
    val expectedAnnotation = getAnnotation(annotationClass)
    val actualAnnotation = createAnnotationProxy<T>(*values)
    assertEquals(expectedAnnotation, actualAnnotation)
    assertEquals(expectedAnnotation.hashCode().toLong(), actualAnnotation.hashCode().toLong())
    assertEquals(expectedAnnotation.annotationClass.java, actualAnnotation.annotationClass.java)
  }

  private inline fun <reified T : Annotation> assertAnnotationNotEquals(vararg values: Any) {
    val annotationClass = T::class.java
    val expectedAnnotation = getAnnotation(annotationClass)
    val actualAnnotation = createAnnotationProxy<T>(*values)
    assertNotEquals(expectedAnnotation, actualAnnotation)
    assertEquals(expectedAnnotation.annotationClass.java, actualAnnotation.annotationClass.java)
  }

  private inline fun <reified T : Annotation> createAnnotationProxy(vararg values: Any): T {
    val annotationClass = T::class.java
    val annotationProxyClassName = getAnnotationProxyClassName(annotationClass)
    if (!classLoader.hasClass(annotationProxyClassName)) {
      addAnnotationProxy(annotationClass)
    }
    @Suppress("UNCHECKED_CAST")
    val annotationProxyClass = classLoader.loadClass(annotationProxyClassName) as Class<T>
    assertEquals("Annotation proxy must have the only constructor",
        1, annotationProxyClass.declaredConstructors.size.toLong())
    @Suppress("UNCHECKED_CAST")
    val constructor = annotationProxyClass.declaredConstructors[0] as Constructor<T>
    return constructor.newInstance(*values)
  }

  private fun addAnnotationProxy(annotationClass: Class<out Annotation>) {
    val classRegistry = mock<ClassRegistry>()
    given(classRegistry.getClassMirror(notNull())).thenAnswer { invocation ->
      mock<ClassMirror>().apply {
        given(access).thenReturn(Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER)
        given(type).thenReturn(invocation.arguments[0] as Type)
        given(superType).thenReturn(Types.OBJECT_TYPE)
      }
    }
    val annotationDescriptor = getAnnotationDescriptor(annotationClass)
    val annotationProxyType = Type.getObjectType(getAnnotationProxyClassName(annotationClass))
    val annotationProxyGenerator = AnnotationProxyGenerator(classRegistry, annotationDescriptor, annotationProxyType)
    classLoader.addClass(annotationProxyType.internalName, annotationProxyGenerator.generate())
  }

  private inline fun <reified T : Any> newProxyInstance(handler: InvocationHandler): T =
      Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java), handler) as T
}
