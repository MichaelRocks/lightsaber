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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class AnnotationDescriptorTest {
    @Test
    public void testEqualsWithIntArrays() throws Exception {
        final AnnotationDescriptor annotation1 = createAnnotation("EqualsWithIntArrays", new int[] { 42, 43, 44 });
        final AnnotationDescriptor annotation2 = createAnnotation("EqualsWithIntArrays", new int[] { 42, 43, 44 });
        assertEquals(1, annotation1.getValues().size());
        assertEquals(annotation1, annotation2);
        assertEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testEqualsWithAnnotationArrays() throws Exception {
        final AnnotationDescriptor annotation1 = createAnnotation("EqualsWithAnnotationArrays", new Object[] {
                createAnnotation("EqualsWithIntArrays", new int[] { 42, 43, 44 }),
                createAnnotation("EqualsWithIntArrays", new int[] { 45, 46, 47 }),
                createAnnotation("EqualsWithIntArrays", new int[] { 48, 49, 50 })
        });
        final AnnotationDescriptor annotation2 = createAnnotation("EqualsWithAnnotationArrays", new Object[] {
                createAnnotation("EqualsWithIntArrays", new int[] { 42, 43, 44 }),
                createAnnotation("EqualsWithIntArrays", new int[] { 45, 46, 47 }),
                createAnnotation("EqualsWithIntArrays", new int[] { 48, 49, 50 })
        });
        assertEquals(1, annotation1.getValues().size());
        assertEquals(annotation1, annotation2);
        assertEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testEqualsWithDifferentOrder() throws Exception {
        final Map<String, Object> values1 = new HashMap<>();
        values1.put("intValue", 42);
        values1.put("stringValue", "42");
        final AnnotationDescriptor annotation1 = createAnnotation("EqualsWithAnnotationArrays", values1);
        final Map<String, Object> values2 = new HashMap<>();
        values2.put("intValue", 42);
        values2.put("stringValue", "42");
        final AnnotationDescriptor annotation2 = createAnnotation("EqualsWithAnnotationArrays", values2);
        assertEquals(2, annotation1.getValues().size());
        assertEquals(annotation1, annotation2);
        assertEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testNotEqualsToNull() throws Exception {
        final AnnotationDescriptor annotation = createAnnotation("NotEqualsToNull");
        // noinspection ObjectEqualsNull
        assertFalse(annotation.equals(null));
    }

    @Test
    public void testNotEqualsToString() throws Exception {
        final AnnotationDescriptor annotation = createAnnotation("NotEqualsToString");
        assertNotEquals("NotEqualsToString", annotation);
    }

    @Test
    public void testNotEqualsByType() throws Exception {
        final AnnotationDescriptor annotation1 = createAnnotation("NotEqualsByType1");
        final AnnotationDescriptor annotation2 = createAnnotation("NotEqualsByType2");
        assertNotEquals(annotation1, annotation2);
        assertNotEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testNotEqualsWithStrings() throws Exception {
        final AnnotationDescriptor annotation1 = createAnnotation("NotEqualsWithStrings", "Value1");
        final AnnotationDescriptor annotation2 = createAnnotation("NotEqualsWithStrings", "Value2");
        assertNotEquals(annotation1, annotation2);
        assertNotEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testNotEqualsWithIntArrays() throws Exception {
        final AnnotationDescriptor annotation1 = createAnnotation("NotEqualsWithIntArrays", new int[] { 42 });
        final AnnotationDescriptor annotation2 = createAnnotation("NotEqualsWithIntArrays", new int[] { -42 });
        assertNotEquals(annotation1, annotation2);
        assertNotEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testNotEqualsWithAnnotationArrays() throws Exception {
        final AnnotationDescriptor annotation1 = createAnnotation("NotEqualsWithAnnotationArrays", new Object[] {
                createAnnotation("EqualsWithIntArrays", new int[] { 42, 43, 44 }),
                createAnnotation("EqualsWithIntArrays", new int[] { 45, 46, 47 }),
                createAnnotation("EqualsWithIntArrays", new int[] { 48, 49, 50 }),
                createAnnotation("EqualsWithIntArrays", new int[] { 1 })
        });
        final AnnotationDescriptor annotation2 = createAnnotation("NotEqualsWithAnnotationArrays", new Object[] {
                createAnnotation("EqualsWithIntArrays", new int[] { 42, 43, 44 }),
                createAnnotation("EqualsWithIntArrays", new int[] { 45, 46, 47 }),
                createAnnotation("EqualsWithIntArrays", new int[] { 48, 49, 50 }),
                createAnnotation("EqualsWithIntArrays", new int[] { -1 })
        });
        assertNotEquals(annotation1, annotation2);
        assertNotEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testToString() throws Exception {
        final UUID nameUuid = UUID.randomUUID();
        final String name = "ToString"
                + Long.toHexString(nameUuid.getMostSignificantBits())
                + Long.toHexString(nameUuid.getLeastSignificantBits());
        final String value = UUID.randomUUID().toString();
        final AnnotationDescriptor annotation = createAnnotation(name, value);
        final String annotationDescription = annotation.toString();
        assertNotNull(annotationDescription);
        assertTrue(annotationDescription.contains(name));
        assertTrue(annotationDescription.contains(value));
    }

    private static AnnotationDescriptor createAnnotation(final String annotationName) {
        return new AnnotationDescriptor(getAnnotationType(annotationName), Collections.<String, Object>emptyMap(), false);
    }

    private static AnnotationDescriptor createAnnotation(final String annotationName, final Object defaultValue) {
        return createAnnotation(annotationName, "value", defaultValue);
    }

    private static AnnotationDescriptor createAnnotation(final String annotationName, final String methodName,
            final Object defaultValue) {
        return createAnnotation(annotationName, Collections.singletonMap(methodName, defaultValue));
    }

    private static AnnotationDescriptor createAnnotation(final String annotationName,
            final Map<String, Object> values) {
        return new AnnotationDescriptor(getAnnotationType(annotationName), values, false);
    }

    private static Type getAnnotationType(final String annotationName) {
        return Type.getObjectType(annotationName);
    }
}
