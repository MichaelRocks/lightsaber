/*
 * Copyright 2015 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.processor.annotations.proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Annotations {
  @Retention(RetentionPolicy.RUNTIME)
  public @interface EmptyAnnotation {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface BooleanAnnotation {
    boolean value() default true;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface ByteAnnotation {
    byte value() default 42;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface CharAnnotation {
    char value() default 'x';
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface FloatAnnotation {
    float value() default 2.7182818284590452354f;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface DoubleAnnotation {
    double value() default 3.14159265358979323846;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface IntAnnotation {
    int value() default 42;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface LongAnnotation {
    long value() default 42L;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface ShortAnnotation {
    short value() default 42;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface StringAnnotation {
    String value() default "Value";
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface EnumAnnotation {
    RetentionPolicy value() default RetentionPolicy.RUNTIME;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface ClassAnnotation {
    Class<?> value() default Object.class;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface AnnotationAnnotation {
    IntAnnotation value() default @IntAnnotation;
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface BooleanArrayAnnotation {
    boolean[] value() default { true };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface ByteArrayAnnotation {
    byte[] value() default { 42 };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface CharArrayAnnotation {
    char[] value() default { 'x' };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface FloatArrayAnnotation {
    float[] value() default { 2.7182818284590452354f };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface DoubleArrayAnnotation {
    double[] value() default { 3.14159265358979323846 };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface IntArrayAnnotation {
    int[] value() default { 42 };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface LongArrayAnnotation {
    long[] value() default { 42L };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface ShortArrayAnnotation {
    short[] value() default { 42 };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface StringArrayAnnotation {
    String[] value() default { "Value" };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface EnumArrayAnnotation {
    RetentionPolicy[] value() default { RetentionPolicy.RUNTIME };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface ClassArrayAnnotation {
    Class<?>[] value() default { Object.class };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface AnnotationArrayAnnotation {
    IntAnnotation[] value() default { @IntAnnotation };
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface CompositeAnnotation {
    @Order(0) boolean booleanValue() default true;

    @Order(1) byte byteValue() default 42;

    @Order(2) char charValue() default 'x';

    @Order(3) float floatValue() default 2.7182818284590452354f;

    @Order(4) double doubleValue() default 3.14159265358979323846;

    @Order(5) int intValue() default 42;

    @Order(6) long longValue() default 42L;

    @Order(7) short shortValue() default 42;

    @Order(8) String stringValue() default "Value";

    @Order(9) RetentionPolicy enumValue() default RetentionPolicy.RUNTIME;

    @Order(10) Class<?> classValue() default Object.class;

    @Order(11) IntAnnotation annotationValue() default @IntAnnotation;

    @Order(12) boolean[] booleanArrayValue() default { true };

    @Order(13) byte[] byteArrayValue() default { 42 };

    @Order(14) char[] charArrayValue() default { 'x' };

    @Order(15) float[] floatArrayValue() default { 2.7182818284590452354f };

    @Order(16) double[] doubleArrayValue() default { 3.14159265358979323846 };

    @Order(17) int[] intArrayValue() default { 42 };

    @Order(18) long[] longArrayValue() default { 42L };

    @Order(19) short[] shortArrayValue() default { 42 };

    @Order(20) String[] stringArrayValue() default { "Value" };

    @Order(21) RetentionPolicy[] enumArrayValue() default { RetentionPolicy.RUNTIME };

    @Order(22) Class<?>[] classArrayValue() default { Object.class };

    @Order(23) IntAnnotation[] annotationArrayValue() default { @IntAnnotation };
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public @interface Order {
    int value();
  }
}
