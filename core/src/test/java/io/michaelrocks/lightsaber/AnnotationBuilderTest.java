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

package io.michaelrocks.lightsaber;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("unused")
public class AnnotationBuilderTest {
  @EmptyAnnotation
  @BooleanAnnotation
  @ByteAnnotation
  @CharAnnotation
  @FloatAnnotation
  @DoubleAnnotation
  @IntAnnotation
  @LongAnnotation
  @ShortAnnotation
  @StringAnnotation
  @EnumAnnotation
  @ClassAnnotation
  @AnnotationAnnotation
  @BooleanArrayAnnotation
  @ByteArrayAnnotation
  @CharArrayAnnotation
  @FloatArrayAnnotation
  @DoubleArrayAnnotation
  @IntArrayAnnotation
  @LongArrayAnnotation
  @ShortArrayAnnotation
  @StringArrayAnnotation
  @EnumArrayAnnotation
  @ClassArrayAnnotation
  @AnnotationArrayAnnotation
  @CompositeAnnotation
  private static final Object ANNOTATION_HOLDER = new Object();

  @Test
  public void testEqualsByReference() {
    final EmptyAnnotation annotation = new AnnotationBuilder<EmptyAnnotation>(EmptyAnnotation.class).build();
    assertEquals(annotation, annotation);
  }

  @Test
  public void testNotEqualsToNull() {
    final EmptyAnnotation annotation = new AnnotationBuilder<EmptyAnnotation>(EmptyAnnotation.class).build();
    // noinspection SimplifiableJUnitAssertion, ConstantConditions
    assertFalse(annotation.equals(null));
  }

  @Test
  public void testNotEqualsToOtherType() {
    final EmptyAnnotation annotation = new AnnotationBuilder<EmptyAnnotation>(EmptyAnnotation.class).build();
    assertNotEquals(new Object(), annotation);
    assertNotEquals(annotation, new Object());
    assertNotEquals(createIntAnnotation(42), annotation);
    assertNotEquals(annotation, createIntAnnotation(42));
  }

  @Test
  public void testEmptyAnnotation() throws Exception {
    final EmptyAnnotation annotation = new AnnotationBuilder<EmptyAnnotation>(EmptyAnnotation.class).build();
    assertAnnotationEquals(EmptyAnnotation.class, annotation);
  }

  @Test
  public void testBooleanAnnotation() throws Exception {
    assertAnnotationEquals(
        BooleanAnnotation.class,
        new AnnotationBuilder<BooleanAnnotation>(BooleanAnnotation.class).build()
    );
    assertAnnotationEquals(
        BooleanAnnotation.class,
        new AnnotationBuilder<BooleanAnnotation>(BooleanAnnotation.class).addMember("value", true).build()
    );
    assertAnnotationNotEquals(
        BooleanAnnotation.class,
        new AnnotationBuilder<BooleanAnnotation>(BooleanAnnotation.class).addMember("value", false).build()
    );
  }

  @Test
  public void testCharAnnotation() throws Exception {
    assertAnnotationEquals(
        CharAnnotation.class,
        new AnnotationBuilder<CharAnnotation>(CharAnnotation.class).build()
    );
    assertAnnotationEquals(
        CharAnnotation.class,
        new AnnotationBuilder<CharAnnotation>(CharAnnotation.class).addMember("value", 'x').build()
    );
    assertAnnotationNotEquals(
        CharAnnotation.class,
        new AnnotationBuilder<CharAnnotation>(CharAnnotation.class).addMember("value", ' ').build()
    );
  }

  @Test
  public void testByteAnnotation() throws Exception {
    assertAnnotationEquals(
        ByteAnnotation.class,
        new AnnotationBuilder<ByteAnnotation>(ByteAnnotation.class).build()
    );
    assertAnnotationEquals(
        ByteAnnotation.class,
        new AnnotationBuilder<ByteAnnotation>(ByteAnnotation.class).addMember("value", (byte) 42).build()
    );
    assertAnnotationNotEquals(
        ByteAnnotation.class,
        new AnnotationBuilder<ByteAnnotation>(ByteAnnotation.class).addMember("value", (byte) -42).build()
    );
  }

  @Test
  public void testShortAnnotation() throws Exception {
    assertAnnotationEquals(
        ShortAnnotation.class,
        new AnnotationBuilder<ShortAnnotation>(ShortAnnotation.class).build()
    );
    assertAnnotationEquals(
        ShortAnnotation.class,
        new AnnotationBuilder<ShortAnnotation>(ShortAnnotation.class).addMember("value", (short) 42).build()
    );
    assertAnnotationNotEquals(
        ShortAnnotation.class,
        new AnnotationBuilder<ShortAnnotation>(ShortAnnotation.class).addMember("value", (short) -42).build()
    );
  }

  @Test
  public void testIntAnnotation() throws Exception {
    assertAnnotationEquals(
        IntAnnotation.class,
        new AnnotationBuilder<IntAnnotation>(IntAnnotation.class).build()
    );
    assertAnnotationEquals(
        IntAnnotation.class,
        new AnnotationBuilder<IntAnnotation>(IntAnnotation.class).addMember("value", 42).build()
    );
    assertAnnotationNotEquals(
        IntAnnotation.class,
        new AnnotationBuilder<IntAnnotation>(IntAnnotation.class).addMember("value", -42).build()
    );
  }

  @Test
  public void testLongAnnotation() throws Exception {
    assertAnnotationEquals(
        LongAnnotation.class,
        new AnnotationBuilder<LongAnnotation>(LongAnnotation.class).build()
    );
    assertAnnotationEquals(
        LongAnnotation.class,
        new AnnotationBuilder<LongAnnotation>(LongAnnotation.class).addMember("value", 42L).build()
    );
    assertAnnotationNotEquals(
        LongAnnotation.class,
        new AnnotationBuilder<LongAnnotation>(LongAnnotation.class).addMember("value", -42L).build()
    );
  }

  @Test
  public void testFloatAnnotation() throws Exception {
    assertAnnotationEquals(
        FloatAnnotation.class,
        new AnnotationBuilder<FloatAnnotation>(FloatAnnotation.class).build()
    );
    assertAnnotationEquals(
        FloatAnnotation.class,
        new AnnotationBuilder<FloatAnnotation>(FloatAnnotation.class).addMember("value", 2.7182818284590452354f).build()
    );
    assertAnnotationNotEquals(
        FloatAnnotation.class,
        new AnnotationBuilder<FloatAnnotation>(FloatAnnotation.class).addMember("value", Float.NaN).build()
    );
  }

  @Test
  public void testDoubleAnnotation() throws Exception {
    assertAnnotationEquals(
        DoubleAnnotation.class,
        new AnnotationBuilder<DoubleAnnotation>(DoubleAnnotation.class).build()
    );
    assertAnnotationEquals(
        DoubleAnnotation.class,
        new AnnotationBuilder<DoubleAnnotation>(DoubleAnnotation.class).addMember("value", 3.14159265358979323846).build()
    );
    assertAnnotationNotEquals(
        DoubleAnnotation.class,
        new AnnotationBuilder<DoubleAnnotation>(DoubleAnnotation.class).addMember("value", Double.NaN).build()
    );
  }

  @Test
  public void testStringAnnotation() throws Exception {
    assertAnnotationEquals(
        StringAnnotation.class,
        new AnnotationBuilder<StringAnnotation>(StringAnnotation.class).build()
    );
    assertAnnotationEquals(
        StringAnnotation.class,
        new AnnotationBuilder<StringAnnotation>(StringAnnotation.class).addMember("value", "Value").build()
    );
    assertAnnotationNotEquals(
        StringAnnotation.class,
        new AnnotationBuilder<StringAnnotation>(StringAnnotation.class).addMember("value", "Value2").build()
    );
  }

  @Test
  public void testClassAnnotation() throws Exception {
    assertAnnotationEquals(
        ClassAnnotation.class,
        new AnnotationBuilder<ClassAnnotation>(ClassAnnotation.class).build()
    );
    assertAnnotationEquals(
        ClassAnnotation.class,
        new AnnotationBuilder<ClassAnnotation>(ClassAnnotation.class).addMember("value", Object.class).build()
    );
    assertAnnotationNotEquals(
        ClassAnnotation.class,
        new AnnotationBuilder<ClassAnnotation>(ClassAnnotation.class).addMember("value", String.class).build()
    );
  }

  @Test
  public void testEnumAnnotation() throws Exception {
    assertAnnotationEquals(
        EnumAnnotation.class,
        new AnnotationBuilder<EnumAnnotation>(EnumAnnotation.class).build()
    );
    assertAnnotationEquals(
        EnumAnnotation.class,
        new AnnotationBuilder<EnumAnnotation>(EnumAnnotation.class).addMember("value", RetentionPolicy.RUNTIME).build()
    );
    assertAnnotationNotEquals(
        EnumAnnotation.class,
        new AnnotationBuilder<EnumAnnotation>(EnumAnnotation.class).addMember("value", RetentionPolicy.CLASS).build()
    );
  }

  @Test
  public void testAnnotationAnnotation() throws Exception {
    assertAnnotationEquals(
        AnnotationAnnotation.class,
        new AnnotationBuilder<AnnotationAnnotation>(AnnotationAnnotation.class).build()
    );
    assertAnnotationEquals(
        AnnotationAnnotation.class,
        new AnnotationBuilder<AnnotationAnnotation>(AnnotationAnnotation.class)
            .addMember("value", createIntAnnotation(42))
            .build()
    );
    assertAnnotationNotEquals(
        AnnotationAnnotation.class,
        new AnnotationBuilder<AnnotationAnnotation>(AnnotationAnnotation.class)
            .addMember("value", createIntAnnotation(-42))
            .build()
    );
  }

  @Test
  public void testBooleanArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        BooleanArrayAnnotation.class,
        new AnnotationBuilder<BooleanArrayAnnotation>(BooleanArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        BooleanArrayAnnotation.class,
        new AnnotationBuilder<BooleanArrayAnnotation>(BooleanArrayAnnotation.class).addMember("value", new boolean[] { true }).build()
    );
    assertAnnotationNotEquals(
        BooleanArrayAnnotation.class,
        new AnnotationBuilder<BooleanArrayAnnotation>(BooleanArrayAnnotation.class).addMember("value", new boolean[] { false }).build()
    );
  }

  @Test
  public void testCharArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        CharArrayAnnotation.class,
        new AnnotationBuilder<CharArrayAnnotation>(CharArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        CharArrayAnnotation.class,
        new AnnotationBuilder<CharArrayAnnotation>(CharArrayAnnotation.class).addMember("value", new char[] { 'x' }).build()
    );
    assertAnnotationNotEquals(
        CharArrayAnnotation.class,
        new AnnotationBuilder<CharArrayAnnotation>(CharArrayAnnotation.class).addMember("value", new char[] { ' ' }).build()
    );
  }

  @Test
  public void testByteArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        ByteArrayAnnotation.class,
        new AnnotationBuilder<ByteArrayAnnotation>(ByteArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        ByteArrayAnnotation.class,
        new AnnotationBuilder<ByteArrayAnnotation>(ByteArrayAnnotation.class).addMember("value", new byte[] { 42 }).build()
    );
    assertAnnotationNotEquals(
        ByteArrayAnnotation.class,
        new AnnotationBuilder<ByteArrayAnnotation>(ByteArrayAnnotation.class).addMember("value", new byte[] { -42 }).build()
    );
  }

  @Test
  public void testShortArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        ShortArrayAnnotation.class,
        new AnnotationBuilder<ShortArrayAnnotation>(ShortArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        ShortArrayAnnotation.class,
        new AnnotationBuilder<ShortArrayAnnotation>(ShortArrayAnnotation.class).addMember("value", new short[] { 42 }).build()
    );
    assertAnnotationNotEquals(
        ShortArrayAnnotation.class,
        new AnnotationBuilder<ShortArrayAnnotation>(ShortArrayAnnotation.class).addMember("value", new short[] { -42 }).build()
    );
  }

  @Test
  public void testIntArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        IntArrayAnnotation.class,
        new AnnotationBuilder<IntArrayAnnotation>(IntArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        IntArrayAnnotation.class,
        new AnnotationBuilder<IntArrayAnnotation>(IntArrayAnnotation.class).addMember("value", new int[] { 42 }).build()
    );
    assertAnnotationNotEquals(
        IntArrayAnnotation.class,
        new AnnotationBuilder<IntArrayAnnotation>(IntArrayAnnotation.class).addMember("value", new int[] { -42 }).build()
    );
  }

  @Test
  public void testLongArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        LongArrayAnnotation.class,
        new AnnotationBuilder<LongArrayAnnotation>(LongArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        LongArrayAnnotation.class,
        new AnnotationBuilder<LongArrayAnnotation>(LongArrayAnnotation.class).addMember("value", new long[] { 42L }).build()
    );
    assertAnnotationNotEquals(
        LongArrayAnnotation.class,
        new AnnotationBuilder<LongArrayAnnotation>(LongArrayAnnotation.class).addMember("value", new long[] { -42L }).build()
    );
  }

  @Test
  public void testFloatArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        FloatArrayAnnotation.class,
        new AnnotationBuilder<FloatArrayAnnotation>(FloatArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        FloatArrayAnnotation.class,
        new AnnotationBuilder<FloatArrayAnnotation>(FloatArrayAnnotation.class).addMember("value", new float[] { 2.7182818284590452354f }).build()
    );
    assertAnnotationNotEquals(
        FloatArrayAnnotation.class,
        new AnnotationBuilder<FloatArrayAnnotation>(FloatArrayAnnotation.class).addMember("value", new float[] { Float.NaN }).build()
    );
  }

  @Test
  public void testDoubleArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        DoubleArrayAnnotation.class,
        new AnnotationBuilder<DoubleArrayAnnotation>(DoubleArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        DoubleArrayAnnotation.class,
        new AnnotationBuilder<DoubleArrayAnnotation>(DoubleArrayAnnotation.class).addMember("value", new double[] { 3.14159265358979323846 }).build()
    );
    assertAnnotationNotEquals(
        DoubleArrayAnnotation.class,
        new AnnotationBuilder<DoubleArrayAnnotation>(DoubleArrayAnnotation.class).addMember("value", new double[] { Double.NaN }).build()
    );
  }

  @Test
  public void testStringArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        StringArrayAnnotation.class,
        new AnnotationBuilder<StringArrayAnnotation>(StringArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        StringArrayAnnotation.class,
        new AnnotationBuilder<StringArrayAnnotation>(StringArrayAnnotation.class).addMember("value", new String[] { "Value" }).build()
    );
    assertAnnotationNotEquals(
        StringArrayAnnotation.class,
        new AnnotationBuilder<StringArrayAnnotation>(StringArrayAnnotation.class).addMember("value", new String[] { "Value2" }).build()
    );
  }

  @Test
  public void testClassArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        ClassArrayAnnotation.class,
        new AnnotationBuilder<ClassArrayAnnotation>(ClassArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        ClassArrayAnnotation.class,
        new AnnotationBuilder<ClassArrayAnnotation>(ClassArrayAnnotation.class).addMember("value", new Class<?>[] { Object.class }).build()
    );
    assertAnnotationNotEquals(
        ClassArrayAnnotation.class,
        new AnnotationBuilder<ClassArrayAnnotation>(ClassArrayAnnotation.class).addMember("value", new Class<?>[] { String.class }).build()
    );
  }

  @Test
  public void testEnumArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        EnumArrayAnnotation.class,
        new AnnotationBuilder<EnumArrayAnnotation>(EnumArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        EnumArrayAnnotation.class,
        new AnnotationBuilder<EnumArrayAnnotation>(EnumArrayAnnotation.class)
            .addMember("value", new RetentionPolicy[] { RetentionPolicy.RUNTIME })
            .build()
    );
    assertAnnotationNotEquals(
        EnumArrayAnnotation.class,
        new AnnotationBuilder<EnumArrayAnnotation>(EnumArrayAnnotation.class)
            .addMember("value", new RetentionPolicy[] { RetentionPolicy.CLASS })
            .build()
    );
  }

  @Test
  public void testArrayAnnotationArrayAnnotation() throws Exception {
    assertAnnotationEquals(
        AnnotationArrayAnnotation.class,
        new AnnotationBuilder<AnnotationArrayAnnotation>(AnnotationArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        AnnotationArrayAnnotation.class,
        new AnnotationBuilder<AnnotationArrayAnnotation>(AnnotationArrayAnnotation.class)
            .addMember("value", new IntAnnotation[] { createIntAnnotation(42) })
            .build()
    );
    assertAnnotationNotEquals(
        AnnotationArrayAnnotation.class,
        new AnnotationBuilder<AnnotationArrayAnnotation>(AnnotationArrayAnnotation.class)
            .addMember("value", new IntAnnotation[] { createIntAnnotation(-42) })
            .build()
    );
  }

  @Test
  public void testCompositeAnnotation() throws Exception {
    assertAnnotationEquals(
        CompositeAnnotation.class,
        new AnnotationBuilder<CompositeAnnotation>(CompositeAnnotation.class).build()
    );
    assertAnnotationEquals(
        CompositeAnnotation.class,
        new AnnotationBuilder<CompositeAnnotation>(CompositeAnnotation.class)
            .addMember("booleanValue", true)
            .addMember("charValue", 'x')
            .addMember("byteValue", (byte) 42)
            .addMember("shortValue", (short) 42)
            .addMember("intValue", 42)
            .addMember("longValue", 42L)
            .addMember("floatValue", 2.7182818284590452354f)
            .addMember("doubleValue", 3.14159265358979323846)
            .addMember("stringValue", "Value")
            .addMember("classValue", Object.class)
            .addMember("enumValue", RetentionPolicy.RUNTIME)
            .addMember("annotationValue", createIntAnnotation(42))
            .addMember("booleanArrayValue", new boolean[] { true })
            .addMember("charArrayValue", new char[] { 'x' })
            .addMember("byteArrayValue", new byte[] { 42 })
            .addMember("shortArrayValue", new short[] { 42 })
            .addMember("intArrayValue", new int[] { 42 })
            .addMember("longArrayValue", new long[] { 42L })
            .addMember("floatArrayValue", new float[] { 2.7182818284590452354f })
            .addMember("doubleArrayValue", new double[] { 3.14159265358979323846 })
            .addMember("stringArrayValue", new String[] { "Value" })
            .addMember("classArrayValue", new Class<?>[] { Object.class })
            .addMember("enumArrayValue", new RetentionPolicy[] { RetentionPolicy.RUNTIME })
            .addMember("annotationArrayValue", new IntAnnotation[] { createIntAnnotation(42) })
            .build()
    );
    assertAnnotationNotEquals(
        CompositeAnnotation.class,
        new AnnotationBuilder<CompositeAnnotation>(CompositeAnnotation.class)
            .addMember("booleanValue", false)
            .addMember("byteValue", (byte) -42)
            .addMember("charValue", ' ')
            .addMember("floatValue", Float.NaN)
            .addMember("doubleValue", Double.NaN)
            .addMember("intValue", -42)
            .addMember("longValue", -42L)
            .addMember("shortValue", (short) -42)
            .addMember("stringValue", "Value2")
            .addMember("enumValue", RetentionPolicy.CLASS)
            .addMember("classValue", String.class)
            .addMember("annotationValue", createIntAnnotation(-42))
            .addMember("booleanArrayValue", new boolean[] { false })
            .addMember("byteArrayValue", new byte[] { -42 })
            .addMember("charArrayValue", new char[] { ' ' })
            .addMember("floatArrayValue", new float[] { Float.NaN })
            .addMember("doubleArrayValue", new double[] { Double.NaN })
            .addMember("intArrayValue", new int[] { -42 })
            .addMember("longArrayValue", new long[] { -42L })
            .addMember("shortArrayValue", new short[] { -42 })
            .addMember("stringArrayValue", new String[] { "Value2" })
            .addMember("enumArrayValue", new RetentionPolicy[] { RetentionPolicy.CLASS })
            .addMember("classArrayValue", new Class<?>[] { String.class })
            .addMember("annotationArrayValue", new IntAnnotation[] { createIntAnnotation(-42) })
            .build()
    );
  }

  @Test
  public void testEmptyToString() {
    final EmptyAnnotation annotation = new AnnotationBuilder<EmptyAnnotation>(EmptyAnnotation.class).build();
    final String expected = "@" + EmptyAnnotation.class.getName() + "()";
    final String actual = annotation.toString();
    assertEquals(expected, actual);
  }

  @Test
  public void testCompositeToString() {
    final CompositeAnnotation annotation = new AnnotationBuilder<CompositeAnnotation>(CompositeAnnotation.class)
        .addMember("booleanValue", true)
        .addMember("charValue", 'x')
        .addMember("byteValue", (byte) 42)
        .addMember("shortValue", (short) 42)
        .addMember("intValue", 42)
        .addMember("longValue", 42L)
        .addMember("floatValue", Float.NaN)
        .addMember("doubleValue", Double.POSITIVE_INFINITY)
        .addMember("stringValue", "Value")
        .addMember("classValue", Object.class)
        .addMember("enumValue", RetentionPolicy.RUNTIME)
        .addMember("annotationValue", createIntAnnotation(42))
        .addMember("booleanArrayValue", new boolean[] { true, false })
        .addMember("charArrayValue", new char[] { 'x', 'y' })
        .addMember("byteArrayValue", new byte[] { 42, 43 })
        .addMember("shortArrayValue", new short[] { 42, 43 })
        .addMember("intArrayValue", new int[] { 42, 43 })
        .addMember("longArrayValue", new long[] { 42L, 43L })
        .addMember("floatArrayValue", new float[] { Float.NaN, Float.POSITIVE_INFINITY })
        .addMember("doubleArrayValue", new double[] { Double.POSITIVE_INFINITY, Double.NaN })
        .addMember("stringArrayValue", new String[] { "Value1", "Value2" })
        .addMember("classArrayValue", new Class<?>[] { Object.class, String.class })
        .addMember("enumArrayValue", new RetentionPolicy[] { RetentionPolicy.RUNTIME, RetentionPolicy.CLASS })
        .addMember("annotationArrayValue", new IntAnnotation[] { createIntAnnotation(42), createIntAnnotation(43) })
        .build();

    final String[] expectedSubstrings = {
        "booleanValue=true",
        "byteValue=42",
        "charValue=x",
        "floatValue=NaN",
        "doubleValue=Infinity",
        "intValue=42",
        "longValue=42",
        "shortValue=42",
        "stringValue=Value",
        "enumValue=RUNTIME",
        "classValue=class java.lang.Object",
        "annotationValue=@" + IntAnnotation.class.getName() + "(value=42)",
        "booleanArrayValue=[true, false]",
        "byteArrayValue=[42, 43]",
        "charArrayValue=[x, y]",
        "floatArrayValue=[NaN, Infinity]",
        "doubleArrayValue=[Infinity, NaN]",
        "intArrayValue=[42, 43]",
        "longArrayValue=[42, 43]",
        "shortArrayValue=[42, 43]",
        "stringArrayValue=[Value1, Value2]",
        "enumArrayValue=[RUNTIME, CLASS]",
        "classArrayValue=[class java.lang.Object, class java.lang.String]",
        "annotationArrayValue",
        "@" + IntAnnotation.class.getName() + "(value=42), @" + IntAnnotation.class.getName() + "(value=43)]"
    };

    final String actualString = annotation.toString();

    assertTrue(actualString.startsWith("@" + CompositeAnnotation.class.getName() + "("));
    assertTrue(actualString.endsWith(")"));

    for (final String expectedSubstring : expectedSubstrings) {
      assertContains(actualString, expectedSubstring);
    }
  }

  @Test
  public void testFloatEquality() {
    @FloatEqualityAnnotation
    class TestDefault {}

    @FloatEqualityAnnotation(0.0f)
    class TestPositiveZero {}

    @SuppressWarnings("DefaultAnnotationParam")
    @FloatEqualityAnnotation(-0.0f)
    class TestNegativeZero {}

    @FloatEqualityAnnotation(Float.NaN)
    class TestNaN {}

    @FloatEqualityAnnotation(Float.POSITIVE_INFINITY)
    class TestPositiveInfinity {}

    @FloatEqualityAnnotation(Float.NEGATIVE_INFINITY)
    class TestNegativeInfinity {}

    assertAnnotationEquals(
        TestDefault.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).build()
    );
    assertAnnotationEquals(
        TestDefault.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", -0.0f).build()
    );
    assertAnnotationNotEquals(
        TestDefault.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", 0.0f).build()
    );
    assertAnnotationNotEquals(
        TestPositiveZero.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", -0.0f).build()
    );
    assertAnnotationEquals(
        TestPositiveZero.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", 0.0f).build()
    );
    assertAnnotationEquals(
        TestNegativeZero.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", -0.0f).build()
    );
    assertAnnotationNotEquals(
        TestNegativeZero.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", 0.0f).build()
    );
    assertAnnotationEquals(
        TestNaN.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", Float.NaN).build()
    );
    assertAnnotationEquals(
        TestPositiveInfinity.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", Float.POSITIVE_INFINITY).build()
    );
    assertAnnotationNotEquals(
        TestPositiveInfinity.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", Float.NEGATIVE_INFINITY).build()
    );
    assertAnnotationEquals(
        TestNegativeInfinity.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", Float.NEGATIVE_INFINITY).build()
    );
    assertAnnotationNotEquals(
        TestNegativeInfinity.class.getAnnotation(FloatEqualityAnnotation.class),
        new AnnotationBuilder<FloatEqualityAnnotation>(FloatEqualityAnnotation.class).addMember("value", Float.POSITIVE_INFINITY).build()
    );
  }

  @Test
  public void testFloatArrayEquality() {
    @FloatEqualityArrayAnnotation
    class TestDefault {}

    @FloatEqualityArrayAnnotation(0.0f)
    class TestPositiveZero {}

    @FloatEqualityArrayAnnotation(-0.0f)
    class TestNegativeZero {}

    @FloatEqualityArrayAnnotation(Float.NaN)
    class TestNaN {}

    @FloatEqualityArrayAnnotation(Float.POSITIVE_INFINITY)
    class TestPositiveInfinity {}

    @FloatEqualityArrayAnnotation(Float.NEGATIVE_INFINITY)
    class TestNegativeInfinity {}

    assertAnnotationEquals(
        TestDefault.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        TestDefault.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class).addMember("value", new float[] { -0.0f }).build()
    );
    assertAnnotationNotEquals(
        TestDefault.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class).addMember("value", new float[] { 0.0f }).build()
    );
    assertAnnotationNotEquals(
        TestPositiveZero.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class).addMember("value", new float[] { -0.0f }).build()
    );
    assertAnnotationEquals(
        TestPositiveZero.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class).addMember("value", new float[] { 0.0f }).build()
    );
    assertAnnotationEquals(
        TestNegativeZero.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class).addMember("value", new float[] { -0.0f }).build()
    );
    assertAnnotationNotEquals(
        TestNegativeZero.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class).addMember("value", new float[] { 0.0f }).build()
    );
    assertAnnotationEquals(
        TestNaN.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class).addMember("value", new float[] { Float.NaN }).build()
    );
    assertAnnotationEquals(
        TestPositiveInfinity.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class)
            .addMember("value", new float[] { Float.POSITIVE_INFINITY })
            .build()
    );
    assertAnnotationNotEquals(
        TestPositiveInfinity.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class)
            .addMember("value", new float[] { Float.NEGATIVE_INFINITY })
            .build()
    );
    assertAnnotationEquals(
        TestNegativeInfinity.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class)
            .addMember("value", new float[] { Float.NEGATIVE_INFINITY })
            .build()
    );
    assertAnnotationNotEquals(
        TestNegativeInfinity.class.getAnnotation(FloatEqualityArrayAnnotation.class),
        new AnnotationBuilder<FloatEqualityArrayAnnotation>(FloatEqualityArrayAnnotation.class)
            .addMember("value", new float[] { Float.POSITIVE_INFINITY })
            .build()
    );
  }

  @Test
  public void testDoubleEquality() {
    @DoubleEqualityAnnotation
    class TestDefault {}

    @DoubleEqualityAnnotation(0.0)
    class TestPositiveZero {}

    @DoubleEqualityAnnotation(-0.0)
    class TestNegativeZero {}

    @DoubleEqualityAnnotation(Double.NaN)
    class TestNaN {}

    @DoubleEqualityAnnotation(Double.POSITIVE_INFINITY)
    class TestPositiveInfinity {}

    @DoubleEqualityAnnotation(Double.NEGATIVE_INFINITY)
    class TestNegativeInfinity {}

    assertAnnotationEquals(
        TestDefault.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).build()
    );
    assertAnnotationEquals(
        TestDefault.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", -0.0).build()
    );
    assertAnnotationNotEquals(
        TestDefault.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", 0.0).build()
    );
    assertAnnotationNotEquals(
        TestPositiveZero.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", -0.0).build()
    );
    assertAnnotationEquals(
        TestPositiveZero.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", 0.0).build()
    );
    assertAnnotationEquals(
        TestNegativeZero.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", -0.0).build()
    );
    assertAnnotationNotEquals(
        TestNegativeZero.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", 0.0).build()
    );
    assertAnnotationEquals(
        TestNaN.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", Double.NaN).build()
    );
    assertAnnotationEquals(
        TestPositiveInfinity.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", Double.POSITIVE_INFINITY).build()
    );
    assertAnnotationNotEquals(
        TestPositiveInfinity.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", Double.NEGATIVE_INFINITY).build()
    );
    assertAnnotationEquals(
        TestNegativeInfinity.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", Double.NEGATIVE_INFINITY).build()
    );
    assertAnnotationNotEquals(
        TestNegativeInfinity.class.getAnnotation(DoubleEqualityAnnotation.class),
        new AnnotationBuilder<DoubleEqualityAnnotation>(DoubleEqualityAnnotation.class).addMember("value", Double.POSITIVE_INFINITY).build()
    );
  }

  @Test
  public void testDoubleArrayEquality() {
    @DoubleEqualityArrayAnnotation
    class TestDefault {}

    @DoubleEqualityArrayAnnotation(0.0)
    class TestPositiveZero {}

    @DoubleEqualityArrayAnnotation(-0.0)
    class TestNegativeZero {}

    @DoubleEqualityArrayAnnotation(Double.NaN)
    class TestNaN {}

    @DoubleEqualityArrayAnnotation(Double.POSITIVE_INFINITY)
    class TestPositiveInfinity {}

    @DoubleEqualityArrayAnnotation(Double.NEGATIVE_INFINITY)
    class TestNegativeInfinity {}

    assertAnnotationEquals(
        TestDefault.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class).build()
    );
    assertAnnotationEquals(
        TestDefault.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class).addMember("value", new double[] { -0.0 }).build()
    );
    assertAnnotationNotEquals(
        TestDefault.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class).addMember("value", new double[] { 0.0 }).build()
    );
    assertAnnotationNotEquals(
        TestPositiveZero.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class).addMember("value", new double[] { -0.0 }).build()
    );
    assertAnnotationEquals(
        TestPositiveZero.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class).addMember("value", new double[] { 0.0 }).build()
    );
    assertAnnotationEquals(
        TestNegativeZero.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class).addMember("value", new double[] { -0.0 }).build()
    );
    assertAnnotationNotEquals(
        TestNegativeZero.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class).addMember("value", new double[] { 0.0 }).build()
    );
    assertAnnotationEquals(
        TestNaN.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class).addMember("value", new double[] { Double.NaN })
            .build()
    );
    assertAnnotationEquals(
        TestPositiveInfinity.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class)
            .addMember("value", new double[] { Double.POSITIVE_INFINITY })
            .build()
    );
    assertAnnotationNotEquals(
        TestPositiveInfinity.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class)
            .addMember("value", new double[] { Double.NEGATIVE_INFINITY })
            .build()
    );
    assertAnnotationEquals(
        TestNegativeInfinity.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class)
            .addMember("value", new double[] { Double.NEGATIVE_INFINITY })
            .build()
    );
    assertAnnotationNotEquals(
        TestNegativeInfinity.class.getAnnotation(DoubleEqualityArrayAnnotation.class),
        new AnnotationBuilder<DoubleEqualityArrayAnnotation>(DoubleEqualityArrayAnnotation.class)
            .addMember("value", new double[] { Double.POSITIVE_INFINITY })
            .build()
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowsIfNotAnnotation() {
    class NotAnnotation {}

    @SuppressWarnings("unchecked")
    final Class<Annotation> annotationClass = (Class) NotAnnotation.class;
    new AnnotationBuilder<Annotation>(annotationClass);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowsIfAnnotationClass() {
    new AnnotationBuilder<Annotation>(Annotation.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowsIfSameMemberAddedTwice() {
    new AnnotationBuilder<BooleanAnnotation>(BooleanAnnotation.class)
        .addMember("value", true)
        .addMember("value", false);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testThrowsIfMemberNotExists() {
    new AnnotationBuilder<EmptyAnnotation>(EmptyAnnotation.class)
        .addMember("value", "Value")
        .build();
  }

  @Test(expected = IncompleteAnnotationException.class)
  public void testThrowsIfUninitializedMembers() {
    new AnnotationBuilder<UninitializedAnnotation>(UninitializedAnnotation.class)
        .addMember("value1", "Value1")
        .build();
  }

  private <T extends Annotation> void assertAnnotationEquals(final Class<T> annotationClass, final T actualAnnotation) throws Exception {
    final T expectedAnnotation = getAnnotation(annotationClass);
    assertEquals(annotationClass, actualAnnotation.annotationType());
    assertAnnotationEquals(expectedAnnotation, actualAnnotation);
  }

  private <T extends Annotation> void assertAnnotationEquals(final T expectedAnnotation, final T actualAnnotation) {
    assertEquals(expectedAnnotation, actualAnnotation);
    assertEquals(actualAnnotation, expectedAnnotation);
    assertEquals(expectedAnnotation.hashCode(), actualAnnotation.hashCode());
    assertEquals(expectedAnnotation.annotationType(), actualAnnotation.annotationType());
  }

  private <T extends Annotation> void assertAnnotationNotEquals(final Class<T> annotationClass, final T actualAnnotation) throws Exception {
    final T unexpectedAnnotation = getAnnotation(annotationClass);
    assertEquals(annotationClass, actualAnnotation.annotationType());
    assertAnnotationNotEquals(unexpectedAnnotation, actualAnnotation);
  }

  private <T extends Annotation> void assertAnnotationNotEquals(final T unexpectedAnnotation, final T actualAnnotation) {
    assertNotEquals(unexpectedAnnotation, actualAnnotation);
    assertEquals(unexpectedAnnotation.annotationType(), actualAnnotation.annotationType());
  }

  private void assertContains(final String string, final String substring) {
    if (!string.contains(substring)) {
      fail("\"" + string + "\" must contain \"" + substring + "\"");
    }
  }

  private IntAnnotation createIntAnnotation(final int value) {
    return new AnnotationBuilder<IntAnnotation>(IntAnnotation.class).addMember("value", value).build();
  }

  private <T extends Annotation> T getAnnotation(final Class<T> annotationClass) throws Exception {
    for (final Annotation annotation : getClass().getDeclaredField("ANNOTATION_HOLDER").getAnnotations()) {
      if (annotation.annotationType().equals(annotationClass)) {
        return annotationClass.cast(annotation);
      }
    }

    fail("Annotation for class " + annotationClass + " not found");
    throw new RuntimeException();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface EmptyAnnotation {
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface BooleanAnnotation {
    boolean value() default true;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ByteAnnotation {
    byte value() default 42;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface CharAnnotation {
    char value() default 'x';
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface FloatAnnotation {
    float value() default 2.7182818284590452354f;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface DoubleAnnotation {
    double value() default 3.14159265358979323846;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface IntAnnotation {
    int value() default 42;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface LongAnnotation {
    long value() default 42L;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ShortAnnotation {
    short value() default 42;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface StringAnnotation {
    String value() default "Value";
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface EnumAnnotation {
    RetentionPolicy value() default RetentionPolicy.RUNTIME;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ClassAnnotation {
    Class<?> value() default Object.class;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface AnnotationAnnotation {
    IntAnnotation value() default @IntAnnotation;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface BooleanArrayAnnotation {
    boolean[] value() default { true };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ByteArrayAnnotation {
    byte[] value() default { 42 };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface CharArrayAnnotation {
    char[] value() default { 'x' };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface FloatArrayAnnotation {
    float[] value() default { 2.7182818284590452354f };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface DoubleArrayAnnotation {
    double[] value() default { 3.14159265358979323846 };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface IntArrayAnnotation {
    int[] value() default { 42 };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface LongArrayAnnotation {
    long[] value() default { 42L };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ShortArrayAnnotation {
    short[] value() default { 42 };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface StringArrayAnnotation {
    String[] value() default { "Value" };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface EnumArrayAnnotation {
    RetentionPolicy[] value() default { RetentionPolicy.RUNTIME };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface ClassArrayAnnotation {
    Class<?>[] value() default { Object.class };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface AnnotationArrayAnnotation {
    IntAnnotation[] value() default { @IntAnnotation };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface CompositeAnnotation {
    boolean booleanValue() default true;

    byte byteValue() default 42;

    char charValue() default 'x';

    float floatValue() default 2.7182818284590452354f;

    double doubleValue() default 3.14159265358979323846;

    int intValue() default 42;

    long longValue() default 42L;

    short shortValue() default 42;

    String stringValue() default "Value";

    RetentionPolicy enumValue() default RetentionPolicy.RUNTIME;

    Class<?> classValue() default Object.class;

    IntAnnotation annotationValue() default @IntAnnotation;

    boolean[] booleanArrayValue() default { true };

    byte[] byteArrayValue() default { 42 };

    char[] charArrayValue() default { 'x' };

    float[] floatArrayValue() default { 2.7182818284590452354f };

    double[] doubleArrayValue() default { 3.14159265358979323846 };

    int[] intArrayValue() default { 42 };

    long[] longArrayValue() default { 42L };

    short[] shortArrayValue() default { 42 };

    String[] stringArrayValue() default { "Value" };

    RetentionPolicy[] enumArrayValue() default { RetentionPolicy.RUNTIME };

    Class<?>[] classArrayValue() default { Object.class };

    IntAnnotation[] annotationArrayValue() default { @IntAnnotation };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface FloatEqualityAnnotation {
    float value() default -0.0f;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface FloatEqualityArrayAnnotation {
    float[] value() default { -0.0f };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface DoubleEqualityAnnotation {
    double value() default -0.0f;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface DoubleEqualityArrayAnnotation {
    double[] value() default { -0.0 };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface UninitializedAnnotation {
    String value1();

    String value2();
  }
}

