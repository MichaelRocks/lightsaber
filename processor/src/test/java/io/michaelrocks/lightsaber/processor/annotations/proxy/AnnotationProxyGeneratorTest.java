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

import io.michaelrocks.lightsaber.processor.annotations.AnnotationDescriptor;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationDescriptorBuilder;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.Type;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

@SuppressWarnings("unused")
@ParametersAreNonnullByDefault
public class AnnotationProxyGeneratorTest {
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
    @AnnotationArrayAnnotation
    @CompositeAnnotation
    private static final Object ANNOTATION_HOLDER = new Object();

    private ArrayClassLoader classLoader;

    @Before
    public void createClassLoader() {
        classLoader = new ArrayClassLoader(getClass().getClassLoader());
    }

    @Test
    public void testEqualsByReference() throws Exception {
        final EmptyAnnotation annotation = createAnnotationProxy(EmptyAnnotation.class);
        assertEquals(annotation, annotation);
    }

    @Test
    public void testNotEqualsToNull() throws Exception {
        // noinspection ObjectEqualsNull
        assertFalse(createAnnotationProxy(EmptyAnnotation.class).equals(null));
    }

    @Test
    public void testNotEqualsToOtherType() throws Exception {
        assertNotEquals(new Object(), createAnnotationProxy(EmptyAnnotation.class));
        assertNotEquals(createAnnotationProxy(IntAnnotation.class, 42), createAnnotationProxy(EmptyAnnotation.class));
    }

    @Test
    public void testEmptyAnnotation() throws Exception {
        assertAnnotationEquals(EmptyAnnotation.class);
    }

    @Test
    public void testBooleanAnnotation() throws Exception {
        assertAnnotationEquals(BooleanAnnotation.class, true);
        assertAnnotationNotEquals(BooleanAnnotation.class, false);
    }

    @Test
    public void testByteAnnotation() throws Exception {
        assertAnnotationEquals(ByteAnnotation.class, (byte) 42);
        assertAnnotationNotEquals(ByteAnnotation.class, (byte) -42);
    }

    @Test
    public void testCharAnnotation() throws Exception {
        assertAnnotationEquals(CharAnnotation.class, 'x');
        assertAnnotationNotEquals(CharAnnotation.class, ' ');
    }

    @Test
    public void testFloatAnnotation() throws Exception {
        assertAnnotationEquals(FloatAnnotation.class, 2.7182818284590452354f);
        assertAnnotationNotEquals(FloatAnnotation.class, Float.NaN);
    }

    @Test
    public void testDoubleAnnotation() throws Exception {
        assertAnnotationEquals(DoubleAnnotation.class, 3.14159265358979323846);
        assertAnnotationNotEquals(DoubleAnnotation.class, Double.NaN);
    }

    @Test
    public void testIntAnnotation() throws Exception {
        assertAnnotationEquals(IntAnnotation.class, 42);
        assertAnnotationNotEquals(IntAnnotation.class, -42);
    }

    @Test
    public void testLongAnnotation() throws Exception {
        assertAnnotationEquals(LongAnnotation.class, 42L);
        assertAnnotationNotEquals(LongAnnotation.class, -43L);
    }

    @Test
    public void testShortAnnotation() throws Exception {
        assertAnnotationEquals(ShortAnnotation.class, (short) 42);
        assertAnnotationNotEquals(ShortAnnotation.class, (short) -42);
    }

    @Test
    public void testStringAnnotation() throws Exception {
        assertAnnotationEquals(StringAnnotation.class, "Value");
        assertAnnotationNotEquals(StringAnnotation.class, "Value2");
    }

    @Test
    public void testEnumAnnotation() throws Exception {
        assertAnnotationEquals(EnumAnnotation.class, RetentionPolicy.RUNTIME);
        assertAnnotationNotEquals(EnumAnnotation.class, RetentionPolicy.CLASS);
    }

    @Test
    public void testAnnotationAnnotation() throws Exception {
        assertAnnotationEquals(AnnotationAnnotation.class, createAnnotationProxy(IntAnnotation.class, 42));
        assertAnnotationNotEquals(AnnotationAnnotation.class, createAnnotationProxy(IntAnnotation.class, -42));
    }

    @Test
    public void testBooleanArrayAnnotation() throws Exception {
        assertAnnotationEquals(BooleanArrayAnnotation.class, new Object[] { new boolean[] { true } });
        assertAnnotationNotEquals(BooleanArrayAnnotation.class, new Object[] { new boolean[] { false } });
    }

    @Test
    public void testByteArrayAnnotation() throws Exception {
        assertAnnotationEquals(ByteArrayAnnotation.class, new Object[] { new byte[] { 42 } });
        assertAnnotationNotEquals(ByteArrayAnnotation.class, new Object[] { new byte[] { -42 } });
    }

    @Test
    public void testCharArrayAnnotation() throws Exception {
        assertAnnotationEquals(CharArrayAnnotation.class, new Object[] { new char[] { 'x' } });
        assertAnnotationNotEquals(CharArrayAnnotation.class, new Object[] { new char[] { 'y' } });
    }

    @Test
    public void testFloatArrayAnnotation() throws Exception {
        assertAnnotationEquals(FloatArrayAnnotation.class, new Object[] { new float[] { 2.7182818284590452354f } });
        assertAnnotationNotEquals(FloatArrayAnnotation.class, new Object[] { new float[] { Float.NaN } });
    }

    @Test
    public void testDoubleArrayAnnotation() throws Exception {
        assertAnnotationEquals(DoubleArrayAnnotation.class, new Object[] { new double[] { 3.14159265358979323846 } });
        assertAnnotationNotEquals(DoubleArrayAnnotation.class, new Object[] { new double[] { Double.NaN } });
    }

    @Test
    public void testIntArrayAnnotation() throws Exception {
        assertAnnotationEquals(IntArrayAnnotation.class, new Object[] { new int[] { 42 } });
        assertAnnotationNotEquals(IntArrayAnnotation.class, new Object[] { new int[] { -42 } });
    }

    @Test
    public void testLongArrayAnnotation() throws Exception {
        assertAnnotationEquals(LongArrayAnnotation.class, new Object[] { new long[] { 42L } });
        assertAnnotationNotEquals(LongArrayAnnotation.class, new Object[] { new long[] { -42L } });
    }

    @Test
    public void testShortArrayAnnotation() throws Exception {
        assertAnnotationEquals(ShortArrayAnnotation.class, new Object[] { new short[] { 42 } });
        assertAnnotationNotEquals(ShortArrayAnnotation.class, new Object[] { new short[] { -42 } });
    }

    @Test
    public void testStringArrayAnnotation() throws Exception {
        assertAnnotationEquals(StringArrayAnnotation.class, new Object[] { new String[] { "Value" } });
        assertAnnotationNotEquals(StringArrayAnnotation.class, new Object[] { new String[] { "Value2" } });
    }

    @Test
    public void testEnumArrayAnnotation() throws Exception {
        assertAnnotationEquals(EnumArrayAnnotation.class,
                new Object[] { new RetentionPolicy[] { RetentionPolicy.RUNTIME } });
        assertAnnotationNotEquals(EnumArrayAnnotation.class,
                new Object[] { new RetentionPolicy[] { RetentionPolicy.CLASS } });
    }

    @Test
    public void testAnnotationArrayAnnotation() throws Exception {
        assertAnnotationEquals(AnnotationArrayAnnotation.class,
                new Object[] { new IntAnnotation[] { createAnnotationProxy(IntAnnotation.class, 42) } });
        assertAnnotationNotEquals(AnnotationArrayAnnotation.class,
                new Object[] { new IntAnnotation[] { createAnnotationProxy(IntAnnotation.class, -42) } });
    }

    @Test
    public void testCompositeAnnotation() throws Exception {
        assertAnnotationEquals(CompositeAnnotation.class,
                true,
                (byte) 42,
                'x',
                2.7182818284590452354f,
                3.14159265358979323846,
                42,
                42L,
                (short) 42,
                "Value",
                RetentionPolicy.RUNTIME,
                createAnnotationProxy(IntAnnotation.class, 42),
                new boolean[] { true },
                new byte[] { (byte) 42 },
                new char[] { 'x' },
                new float[] { 2.7182818284590452354f },
                new double[] { 3.14159265358979323846 },
                new int[] { 42 },
                new long[] { 42L },
                new short[] { (short) 42 },
                new String[] { "Value" },
                new RetentionPolicy[] { RetentionPolicy.RUNTIME },
                new IntAnnotation[] { createAnnotationProxy(IntAnnotation.class, 42) });
    }

    @Test
    public void testEmptyToString() throws Exception {
        final EmptyAnnotation annotation = createAnnotationProxy(EmptyAnnotation.class);
        final String expected = "@" + EmptyAnnotation.class.getName() + "()";
        final String actual = annotation.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void testCompositeToString() throws Exception {
        final CompositeAnnotation annotation = createAnnotationProxy(CompositeAnnotation.class,
                true,
                (byte) 42,
                'x',
                Float.NaN,
                Double.POSITIVE_INFINITY,
                42,
                42L,
                (short) 42,
                "Value",
                RetentionPolicy.RUNTIME,
                createAnnotationProxy(IntAnnotation.class, 42),
                new boolean[] { true, false },
                new byte[] { 42, 43 },
                new char[] { 'x', 'y' },
                new float[] { Float.NaN, Float.POSITIVE_INFINITY },
                new double[] { Double.POSITIVE_INFINITY, Double.NaN },
                new int[] { 42, 43 },
                new long[] { 42L, 43L },
                new short[] { 42, 43 },
                new String[] { "Value1", "Value2" },
                new RetentionPolicy[] { RetentionPolicy.RUNTIME, RetentionPolicy.CLASS },
                new IntAnnotation[] {
                        createAnnotationProxy(IntAnnotation.class, 42),
                        createAnnotationProxy(IntAnnotation.class, 43)
                });

        final String expected = "@" + CompositeAnnotation.class.getName() + "("
                + "booleanValue=true, "
                + "byteValue=42, "
                + "charValue=x, "
                + "floatValue=NaN, "
                + "doubleValue=Infinity, "
                + "intValue=42, "
                + "longValue=42, "
                + "shortValue=42, "
                + "stringValue=Value, "
                + "enumValue=RUNTIME, "
                + "annotationValue=@" + IntAnnotation.class.getName() + "(value=42), "
                + "booleanArrayValue=[true, false], "
                + "byteArrayValue=[42, 43], "
                + "charArrayValue=[x, y], "
                + "floatArrayValue=[NaN, Infinity], "
                + "doubleArrayValue=[Infinity, NaN], "
                + "intArrayValue=[42, 43], "
                + "longArrayValue=[42, 43], "
                + "shortArrayValue=[42, 43], "
                + "stringArrayValue=[Value1, Value2], "
                + "enumArrayValue=[RUNTIME, CLASS], "
                + "annotationArrayValue=["
                + "@" + IntAnnotation.class.getName() + "(value=42), @" + IntAnnotation.class.getName() + "(value=43)]"
                + ")";
        final String actual = annotation.toString();
        assertEquals(expected, actual);
    }

    private <T extends Annotation> void assertAnnotationEquals(final Class<T> annotationClass,
            final Object... values) throws Exception {
        final T expectedAnnotation = getAnnotation(annotationClass);
        final T actualAnnotation = createAnnotationProxy(annotationClass, values);
        assertEquals(expectedAnnotation, actualAnnotation);
        assertEquals(expectedAnnotation.hashCode(), actualAnnotation.hashCode());
        assertEquals(expectedAnnotation.annotationType(), actualAnnotation.annotationType());
    }

    private <T extends Annotation> void assertAnnotationNotEquals(final Class<T> annotationClass,
            final Object... values) throws Exception {
        final T expectedAnnotation = getAnnotation(annotationClass);
        final T actualAnnotation = createAnnotationProxy(annotationClass, values);
        assertNotEquals(expectedAnnotation, actualAnnotation);
        assertEquals(expectedAnnotation.annotationType(), actualAnnotation.annotationType());
    }

    private <T extends Annotation> T createAnnotationProxy(final Class<T> annotationClass,
            final Object... values) throws Exception {
        final String annotationProxyClassName = getAnnotationProxyClassName(annotationClass);
        if (!classLoader.hasClass(annotationProxyClassName)) {
            addAnnotationProxy(annotationClass);
        }
        // noinspection unchecked
        final Class<T> annotationProxyClass = (Class<T>) classLoader.loadClass(annotationProxyClassName);
        assertEquals("Annotation proxy must have the only constructor",
                1, annotationProxyClass.getDeclaredConstructors().length);
        // noinspection unchecked
        final Constructor<T> constructor = (Constructor<T>) annotationProxyClass.getDeclaredConstructors()[0];
        return constructor.newInstance(values);
    }

    private void addAnnotationProxy(final Class<? extends Annotation> annotationClass) {
        final AnnotationDescriptor annotationDescriptor = getAnnotationDescriptor(annotationClass);
        final Type annotationProxyType = Type.getObjectType(getAnnotationProxyClassName(annotationClass));
        final AnnotationProxyGenerator annotationProxyGenerator =
                new AnnotationProxyGenerator(annotationDescriptor, annotationProxyType);
        classLoader.addClass(annotationProxyType.getInternalName(), annotationProxyGenerator.generate());
    }

    private static String getAnnotationProxyClassName(final Class<? extends Annotation> annotationClass) {
        return annotationClass.getSimpleName() + "Proxy";
    }

    private static AnnotationDescriptor getAnnotationDescriptor(final Class<? extends Annotation> annotationClass) {
        final Map<Integer, Method> orderedMethods = new TreeMap<>();
        for (final Method method : annotationClass.getDeclaredMethods()) {
            final Order orderAnnotation = method.getAnnotation(Order.class);
            final int order = orderAnnotation == null ? 0 : orderAnnotation.value();
            final Method oldMethod = orderedMethods.put(order, method);
            assertNull("Method order must be distinct", oldMethod);
        }

        final Type annotationType = Type.getType(annotationClass);
        final AnnotationDescriptorBuilder builder = new AnnotationDescriptorBuilder(annotationType);
        for (final Method method : orderedMethods.values()) {
            builder.addField(method.getName(), Type.getType(method.getReturnType()));
        }
        return builder.build();
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
    public @interface AnnotationArrayAnnotation {
        IntAnnotation[] value() default { @IntAnnotation };
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface CompositeAnnotation {
        @Order( 0) boolean booleanValue() default true;
        @Order( 1) byte byteValue() default 42;
        @Order( 2) char charValue() default 'x';
        @Order( 3) float floatValue() default 2.7182818284590452354f;
        @Order( 4) double doubleValue() default 3.14159265358979323846;
        @Order( 5) int intValue() default 42;
        @Order( 6) long longValue() default 42L;
        @Order( 7) short shortValue() default 42;
        @Order( 8) String stringValue() default "Value";
        @Order( 9) RetentionPolicy enumValue() default RetentionPolicy.RUNTIME;
        @Order(10) IntAnnotation annotationValue() default @IntAnnotation;
        @Order(11) boolean[] booleanArrayValue() default { true };
        @Order(12) byte[] byteArrayValue() default { 42 };
        @Order(13) char[] charArrayValue() default { 'x' };
        @Order(14) float[] floatArrayValue() default { 2.7182818284590452354f };
        @Order(15) double[] doubleArrayValue() default { 3.14159265358979323846 };
        @Order(16) int[] intArrayValue() default { 42 };
        @Order(17) long[] longArrayValue() default { 42L };
        @Order(18) short[] shortArrayValue() default { 42 };
        @Order(19) String[] stringArrayValue() default { "Value" };
        @Order(20) RetentionPolicy[] enumArrayValue() default { RetentionPolicy.RUNTIME };
        @Order(21) IntAnnotation[] annotationArrayValue() default { @IntAnnotation };
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    private @interface Order {
        int value();
    }
}
