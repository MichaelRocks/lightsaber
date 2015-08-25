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

import org.junit.Test;
import org.objectweb.asm.Type;

import static org.junit.Assert.*;

public class AnnotationDataBuilderTest {
    @Test
    public void testEmptyAnnotation() throws Exception {
        final AnnotationData annotation = newAnnotationBuilder("EmptyAnnotation")
                .build();
        assertEquals("EmptyAnnotation", annotation.getType().getInternalName());
        assertTrue(annotation.getValues().isEmpty());
        assertFalse(annotation.isResolved());
    }

    @Test
    public void testDefaultValueAnnotation() throws Exception {
        final AnnotationData annotation = newAnnotationBuilder("DefaultValueAnnotation")
                .addDefaultValue("DefaultValue")
                .build();
        assertEquals("DefaultValueAnnotation", annotation.getType().getInternalName());
        assertEquals(1, annotation.getValues().size());
        assertEquals("DefaultValue", annotation.getValues().get("value"));
        assertFalse(annotation.isResolved());
    }

    @Test
    public void testNamedValueAnnotation() throws Exception {
        final AnnotationData annotation = newAnnotationBuilder("NamedValueAnnotation")
                .addDefaultValue("namedValue", "NamedValue")
                .build();
        assertEquals("NamedValueAnnotation", annotation.getType().getInternalName());
        assertEquals(1, annotation.getValues().size());
        assertEquals("NamedValue", annotation.getValues().get("namedValue"));
        assertFalse(annotation.isResolved());
    }

    @Test
    public void testVariousValuesAnnotation() throws Exception {
        final AnnotationData innerAnnotation = newAnnotationBuilder("InnerAnnotation").build();
        final AnnotationData annotation = newAnnotationBuilder("VariousValuesAnnotation")
                .addDefaultValue("booleanValue", true)
                .addDefaultValue("byteValue", (byte) 42)
                .addDefaultValue("charValue", 'x')
                .addDefaultValue("floatValue", (float) Math.E)
                .addDefaultValue("doubleValue", Math.PI)
                .addDefaultValue("intValue", 42)
                .addDefaultValue("longValue", 42L)
                .addDefaultValue("shortValue", (short) 42)
                .addDefaultValue("stringValue", "x")
                .addDefaultValue("annotationValue", innerAnnotation)
                .addDefaultValue("booleanArrayValue", new boolean[] { true })
                .addDefaultValue("byteArrayValue", new byte[] { (byte) 42 })
                .addDefaultValue("charArrayValue", new char[] { 'x' })
                .addDefaultValue("floatArrayValue", new float[] { (float) Math.E })
                .addDefaultValue("doubleArrayValue", new double[] { Math.PI })
                .addDefaultValue("intArrayValue", new int[] { 42 })
                .addDefaultValue("longArrayValue", new long[] { 42L })
                .addDefaultValue("shortArrayValue", new short[] { (short) 42 })
                .addDefaultValue("stringArrayValue", new String[] { "x" })
                .addDefaultValue("annotationArrayValue", new Object[] { innerAnnotation })
                .build();
        assertEquals("VariousValuesAnnotation", annotation.getType().getInternalName());
        assertEquals(20, annotation.getValues().size());
        assertEquals(true, annotation.getValues().get("booleanValue"));
        assertEquals((byte) 42, annotation.getValues().get("byteValue"));
        assertEquals('x', annotation.getValues().get("charValue"));
        assertEquals((float) Math.E, annotation.getValues().get("floatValue"));
        assertEquals(Math.PI, annotation.getValues().get("doubleValue"));
        assertEquals(42, annotation.getValues().get("intValue"));
        assertEquals(42L, annotation.getValues().get("longValue"));
        assertEquals((short) 42, annotation.getValues().get("shortValue"));
        assertEquals("x", annotation.getValues().get("stringValue"));
        assertEquals(innerAnnotation, annotation.getValues().get("annotationValue"));
        assertArrayEquals(new boolean[] { true }, (boolean[]) annotation.getValues().get("booleanArrayValue"));
        assertArrayEquals(new byte[] { (byte) 42 }, (byte[]) annotation.getValues().get("byteArrayValue"));
        assertArrayEquals(new char[] { 'x' }, (char[]) annotation.getValues().get("charArrayValue"));
        assertArrayEquals(new float[] { (float) Math.E }, (float[]) annotation.getValues().get("floatArrayValue"), 0f);
        assertArrayEquals(new double[] { Math.PI }, (double[]) annotation.getValues().get("doubleArrayValue"), 0f);
        assertArrayEquals(new int[] { 42 }, (int[]) annotation.getValues().get("intArrayValue"));
        assertArrayEquals(new long[] { 42L }, (long[]) annotation.getValues().get("longArrayValue"));
        assertArrayEquals(new short[] { (short) 42 }, (short[]) annotation.getValues().get("shortArrayValue"));
        assertArrayEquals(new String[] { "x" }, (String[]) annotation.getValues().get("stringArrayValue"));
        assertArrayEquals(new Object[] { innerAnnotation },
                (Object[]) annotation.getValues().get("annotationArrayValue"));
        assertFalse(annotation.isResolved());
    }

    @Test
    public void testResolvedAnnotation() throws Exception {
        final AnnotationData annotation = newAnnotationBuilder("ResolvedAnnotation")
                .setResolved(true)
                .build();
        assertEquals("ResolvedAnnotation", annotation.getType().getInternalName());
        assertTrue(annotation.getValues().isEmpty());
        assertTrue(annotation.isResolved());
    }

    private static AnnotationDataBuilder newAnnotationBuilder(final String annotationName) {
        final Type type = Type.getObjectType(annotationName);
        return new AnnotationDataBuilder(type);
    }
}
