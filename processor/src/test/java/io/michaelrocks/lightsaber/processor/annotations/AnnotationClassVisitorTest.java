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

package io.michaelrocks.lightsaber.processor.annotations;

import io.michaelrocks.lightsaber.processor.descriptors.EnumValueDescriptor;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.util.Arrays;

import static org.junit.Assert.*;

public class AnnotationClassVisitorTest {
    @Test
    public void testEmptyAnnotation() throws Exception {
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("EmptyAnnotation");
        AnnotationClassGenerator
                .create(visitor, annotationType)
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertTrue(actualAnnotation.getValues().isEmpty());
        assertFalse(actualAnnotation.isResolved());
    }

    @Test
    public void testExplicitValueAnnotation() throws Exception {
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("ExplicitValueAnnotation");
        AnnotationClassGenerator
                .create(visitor, annotationType)
                .addMethod("explicitValue", Type.getType(String.class))
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertTrue(actualAnnotation.getValues().isEmpty());
        assertFalse(actualAnnotation.isResolved());
    }

    @Test
    public void testImplicitValueAnnotation() throws Exception {
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("ImplicitValueAnnotation");
        AnnotationClassGenerator
                .create(visitor, annotationType)
                .addMethod("implicitValue", Type.getType(String.class), "defaultImplicitValue")
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertEquals("defaultImplicitValue", actualAnnotation.getValues().get("implicitValue"));
        assertEquals(1, actualAnnotation.getValues().size());
        assertFalse(actualAnnotation.isResolved());
    }

    @Test
    public void testExplicitAndImplicitValuesAnnotation() throws Exception {
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("ExplicitAndImplicitValuesAnnotation");
        AnnotationClassGenerator
                .create(visitor, annotationType)
                .addMethod("explicitValue", Type.getType(String.class))
                .addMethod("implicitValue", Type.getType(String.class), "defaultImplicitValue")
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertEquals("defaultImplicitValue", actualAnnotation.getValues().get("implicitValue"));
        assertEquals(1, actualAnnotation.getValues().size());
        assertFalse(actualAnnotation.isResolved());
    }

    @Test
    public void testSimpleValuesAnnotation() throws Exception {
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("PrimitiveValuesAnnotation");
        AnnotationClassGenerator
                .create(visitor, annotationType)
                .addMethod("booleanValue", Type.BOOLEAN_TYPE, true)
                .addMethod("byteValue", Type.BYTE_TYPE, (byte) 42)
                .addMethod("charValue", Type.CHAR_TYPE, 'x')
                .addMethod("floatValue", Type.FLOAT_TYPE, (float) Math.E)
                .addMethod("doubleValue", Type.DOUBLE_TYPE, Math.PI)
                .addMethod("intValue", Type.INT_TYPE, 42)
                .addMethod("longValue", Type.LONG_TYPE, 42L)
                .addMethod("shortValue", Type.SHORT_TYPE, (short) 42)
                .addMethod("stringValue", Type.getType(String.class), "x")
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertEquals(true, actualAnnotation.getValues().get("booleanValue"));
        assertEquals((byte) 42, actualAnnotation.getValues().get("byteValue"));
        assertEquals('x', actualAnnotation.getValues().get("charValue"));
        assertEquals((float) Math.E, (float) actualAnnotation.getValues().get("floatValue"), 0);
        assertEquals(Math.PI, (double) actualAnnotation.getValues().get("doubleValue"), 0.0);
        assertEquals(42, actualAnnotation.getValues().get("intValue"));
        assertEquals(42L, actualAnnotation.getValues().get("longValue"));
        assertEquals((short) 42, actualAnnotation.getValues().get("shortValue"));
        assertEquals("x", actualAnnotation.getValues().get("stringValue"));
        assertEquals(9, actualAnnotation.getValues().size());
        assertFalse(actualAnnotation.isResolved());
    }

    @Test
    public void testArrayValuesAnnotation() throws Exception {
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("ArrayValuesAnnotation");
        AnnotationClassGenerator.create(visitor, annotationType)
                .addMethod("booleanArrayValue", Type.getType(boolean[].class), new boolean[] { true, false, true })
                .addMethod("byteArrayValue", Type.getType(byte[].class), new byte[] { 42, 43, 44 })
                .addMethod("charArrayValue", Type.getType(char[].class), new char[] { 'x', 'y', 'z' })
                .addMethod("floatArrayValue", Type.getType(float[].class),
                        new float[] { (float) Math.E, (float) Math.PI, (float) Math.E })
                .addMethod("doubleArrayValue", Type.getType(double[].class), new double[] { Math.PI, Math.E, Math.PI })
                .addMethod("intArrayValue", Type.getType(int[].class), new int[] { 42, 43, 44 })
                .addMethod("longArrayValue", Type.getType(long[].class), new long[] { 42, 43, 44 })
                .addMethod("shortArrayValue", Type.getType(short[].class), new short[] { 42, 43, 44 })
                .addMethod("stringArrayValue", Type.getType(String[].class), new String[] { "x", "y", "z" })
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertArrayEquals(new boolean[] { true, false, true },
                (boolean[]) actualAnnotation.getValues().get("booleanArrayValue"));
        assertArrayEquals(new byte[] { 42, 43, 44 },
                (byte[]) actualAnnotation.getValues().get("byteArrayValue"));
        assertArrayEquals(new char[] { 'x', 'y', 'z' },
                (char[]) actualAnnotation.getValues().get("charArrayValue"));
        assertArrayEquals(new float[] { (float) Math.E, (float) Math.PI, (float) Math.E },
                (float[]) actualAnnotation.getValues().get("floatArrayValue"), 0f);
        assertArrayEquals(new double[] { Math.PI, Math.E, Math.PI },
                (double[]) actualAnnotation.getValues().get("doubleArrayValue"), 0);
        assertArrayEquals(new int[] { 42, 43, 44 },
                (int[]) actualAnnotation.getValues().get("intArrayValue"));
        assertArrayEquals(new long[] { 42, 43, 44 },
                (long[]) actualAnnotation.getValues().get("longArrayValue"));
        assertArrayEquals(new short[] { 42, 43, 44 },
                (short[]) actualAnnotation.getValues().get("shortArrayValue"));
        assertArrayEquals(new String[] { "x", "y", "z" },
                (String[]) actualAnnotation.getValues().get("stringArrayValue"));
        assertEquals(9, actualAnnotation.getValues().size());
        assertFalse(actualAnnotation.isResolved());
    }

    @Test
    public void testEnumAnnotation() throws Exception {
        final Type enumType = Type.getObjectType("TestEnum");
        final EnumValueDescriptor enumValue = new EnumValueDescriptor(enumType, "TEST");
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("EnumAnnotation");
        AnnotationClassGenerator.create(visitor, annotationType)
                .addMethod("enumValue", enumType, enumValue)
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertEquals(enumValue, actualAnnotation.getValues().get("enumValue"));
        assertEquals(1, actualAnnotation.getValues().size());
        assertFalse(actualAnnotation.isResolved());
    }

    @Test
    public void testEnumArrayAnnotation() throws Exception {
        final Type enumType = Type.getObjectType("TestEnum");
        final Type enumArrayType = Type.getType("[" + enumType.getDescriptor());
        final EnumValueDescriptor[] enumValues = new EnumValueDescriptor[] {
                new EnumValueDescriptor(enumType, "TEST1"),
                new EnumValueDescriptor(enumType, "TEST2"),
                new EnumValueDescriptor(enumType, "TEST3")
        };
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("testEnumArrayAnnotation");
        AnnotationClassGenerator.create(visitor, annotationType)
                .addMethod("enumArrayValue", enumArrayType, enumValues)
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertEquals(Arrays.asList(enumValues), actualAnnotation.getValues().get("enumArrayValue"));
        assertEquals(1, actualAnnotation.getValues().size());
        assertFalse(actualAnnotation.isResolved());
    }

    @Test
    public void testNestedAnnotationAnnotation() throws Exception {
        final AnnotationDescriptor nestedAnnotation = AnnotationHelper.createAnnotation("NestedAnnotation", "Nested");
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("NestedAnnotationAnnotation");
        AnnotationClassGenerator.create(visitor, annotationType)
                .addMethod("annotationValue", nestedAnnotation.getType(), nestedAnnotation)
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertEquals(nestedAnnotation, actualAnnotation.getValues().get("annotationValue"));
        assertEquals(1, actualAnnotation.getValues().size());
        assertFalse(actualAnnotation.isResolved());
    }

    @Test
    public void testNestedAnnotationArrayAnnotation() throws Exception {
        final AnnotationDescriptor[] nestedAnnotations = new AnnotationDescriptor[] {
                AnnotationHelper.createAnnotation("NestedAnnotation", "Nested1"),
                AnnotationHelper.createAnnotation("NestedAnnotation", "Nested2"),
                AnnotationHelper.createAnnotation("NestedAnnotation", "Nested3")
        };
        final Type annotationArrayType = Type.getType("[" + nestedAnnotations[0].getType().getDescriptor());
        final AnnotationClassVisitor visitor = new AnnotationClassVisitor();
        final Type annotationType = Type.getObjectType("NestedAnnotationArrayAnnotation");
        AnnotationClassGenerator.create(visitor, annotationType)
                .addMethod("annotationArrayValue", annotationArrayType, nestedAnnotations)
                .generate();
        final AnnotationDescriptor actualAnnotation = visitor.toAnnotation();
        assertEquals(annotationType, actualAnnotation.getType());
        assertEquals(Arrays.asList(nestedAnnotations), actualAnnotation.getValues().get("annotationArrayValue"));
        assertEquals(1, actualAnnotation.getValues().size());
        assertFalse(actualAnnotation.isResolved());
    }
}
