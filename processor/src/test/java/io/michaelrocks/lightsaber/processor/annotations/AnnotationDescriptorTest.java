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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.util.UUID;

import static org.junit.Assert.*;

public class AnnotationDescriptorTest {
    @Test
    public void testEqualsWithIntArrays() throws Exception {
        final AnnotationDescriptor annotation1 =
                AnnotationHelper.createAnnotationDescriptor("EqualsEmpty");
        final AnnotationDescriptor annotation2 =
                AnnotationHelper.createAnnotationDescriptor("EqualsEmpty");
        assertEquals(0, annotation1.getFields().size());
        assertEquals(annotation1, annotation2);
        assertEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testEqualsWithDefaultField() throws Exception {
        final AnnotationDescriptor annotation1 =
                AnnotationHelper.createAnnotationDescriptor("EqualsWithDefaultField", Type.getType(String.class));
        final AnnotationDescriptor annotation2 =
                AnnotationHelper.createAnnotationDescriptor("EqualsWithDefaultField", Type.getType(String.class));
        assertEquals(1, annotation1.getFields().size());
        assertEquals(annotation1, annotation2);
        assertEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testEqualsWithSimilarOrder() throws Exception {
        final AnnotationDescriptor annotation1 =
                AnnotationHelper.createAnnotationDescriptor("EqualsWithSimilarOrder",
                        Pair.of("value1", Type.getType(String.class)),
                        Pair.of("value2", Type.getType(String.class)));
        final AnnotationDescriptor annotation2 =
                AnnotationHelper.createAnnotationDescriptor("EqualsWithSimilarOrder",
                        Pair.of("value1", Type.getType(String.class)),
                        Pair.of("value2", Type.getType(String.class)));
        assertEquals(2, annotation1.getFields().size());
        assertEquals(annotation1, annotation2);
        assertEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testEqualsWithDifferentOrder() throws Exception {
        final AnnotationDescriptor annotation1 =
                AnnotationHelper.createAnnotationDescriptor("EqualsWithSimilarOrder",
                        Pair.of("value1", Type.getType(String.class)),
                        Pair.of("value2", Type.getType(String.class)));
        final AnnotationDescriptor annotation2 =
                AnnotationHelper.createAnnotationDescriptor("EqualsWithSimilarOrder",
                        Pair.of("value2", Type.getType(String.class)),
                        Pair.of("value1", Type.getType(String.class)));
        assertEquals(2, annotation1.getFields().size());
        assertEquals(annotation1, annotation2);
        assertEquals(annotation1.hashCode(), annotation2.hashCode());
    }

    @Test
    public void testNotEqualsToNull() throws Exception {
        final AnnotationDescriptor annotation = AnnotationHelper.createAnnotationDescriptor("NotEqualsToNull");
        // noinspection ObjectEqualsNull
        assertFalse(annotation.equals(null));
    }

    @Test
    public void testNotEqualsToString() throws Exception {
        final AnnotationDescriptor annotation = AnnotationHelper.createAnnotationDescriptor("NotEqualsToString");
        assertNotEquals("NotEqualsToString", annotation);
    }

    @Test
    public void testNotEqualsByType() throws Exception {
        final AnnotationDescriptor annotation1 = AnnotationHelper.createAnnotationDescriptor("NotEqualsByType1");
        final AnnotationDescriptor annotation2 = AnnotationHelper.createAnnotationDescriptor("NotEqualsByType2");
        assertNotEquals(annotation1, annotation2);
    }

    @Test
    public void testNotEqualsByFieldType() throws Exception {
        final AnnotationDescriptor annotation1 =
                AnnotationHelper.createAnnotationDescriptor("NotEqualsByFieldName", Type.getType(String.class));
        final AnnotationDescriptor annotation2 =
                AnnotationHelper.createAnnotationDescriptor("NotEqualsByFieldName", Type.INT_TYPE);
        assertNotEquals(annotation1, annotation2);
    }

    @Test
    public void testNotEqualsByFieldName() throws Exception {
        final AnnotationDescriptor annotation1 =
                AnnotationHelper.createAnnotationDescriptor("NotEqualsByFieldName", "value1", Type.INT_TYPE);
        final AnnotationDescriptor annotation2 =
                AnnotationHelper.createAnnotationDescriptor("NotEqualsByFieldName", "value2", Type.INT_TYPE);
        assertNotEquals(annotation1, annotation2);
    }

    @Test
    public void testToString() throws Exception {
        final UUID nameUuid = UUID.randomUUID();
        final String name = "ToString"
                + Long.toHexString(nameUuid.getMostSignificantBits())
                + Long.toHexString(nameUuid.getLeastSignificantBits());
        final String fieldName = UUID.randomUUID().toString();
        final Type fieldType = Type.getObjectType(UUID.randomUUID().toString());
        final AnnotationDescriptor annotation =
                AnnotationHelper.createAnnotationDescriptor(name, fieldName, fieldType);
        final String annotationDescription = annotation.toString();
        assertNotNull(annotationDescription);
        assertTrue(annotationDescription.contains(name));
        assertTrue(annotationDescription.contains(fieldName));
        assertTrue(annotationDescription.contains(fieldType.toString()));
    }
}
