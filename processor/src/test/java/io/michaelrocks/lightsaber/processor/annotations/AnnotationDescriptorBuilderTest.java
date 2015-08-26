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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnnotationDescriptorBuilderTest {
    @Test
    public void testEmptyAnnotation() throws Exception {
        final AnnotationDescriptor annotation = newBuilder("EmptyAnnotation")
                .build();
        assertEquals("EmptyAnnotation", annotation.getType().getInternalName());
        assertTrue(annotation.getFields().isEmpty());
    }

    @Test
    public void testDefaultValueAnnotation() throws Exception {
        final AnnotationDescriptor annotation = newBuilder("DefaultValueAnnotation")
                .addDefaultField(Type.getType(String.class))
                .build();
        assertEquals("DefaultValueAnnotation", annotation.getType().getInternalName());
        assertEquals(1, annotation.getFields().size());
        assertEquals(Type.getType(String.class), annotation.getFields().get("value"));
    }

    @Test
    public void testNamedValueAnnotation() throws Exception {
        final AnnotationDescriptor annotation = newBuilder("NamedValueAnnotation")
                .addField("namedValue", Type.getType(String.class))
                .build();
        assertEquals("NamedValueAnnotation", annotation.getType().getInternalName());
        assertEquals(1, annotation.getFields().size());
        assertEquals(Type.getType(String.class), annotation.getFields().get("namedValue"));
    }

    @Test
    public void testMultipleValuesAnnotation() throws Exception {
        final AnnotationDescriptor annotation = newBuilder("NamedValueAnnotation")
                .addField("namedValue1", Type.getType(String.class))
                .addField("namedValue2", Type.INT_TYPE)
                .addField("namedValue3", Type.getObjectType("[Z"))
                .build();
        assertEquals("NamedValueAnnotation", annotation.getType().getInternalName());
        assertEquals(3, annotation.getFields().size());
        assertEquals(Type.getType(String.class), annotation.getFields().get("namedValue1"));
        assertEquals(Type.INT_TYPE, annotation.getFields().get("namedValue2"));
        assertEquals(Type.getObjectType("[Z"), annotation.getFields().get("namedValue3"));
    }

    private static AnnotationDescriptorBuilder newBuilder(final String annotationName) {
        final Type type = Type.getObjectType(annotationName);
        return new AnnotationDescriptorBuilder(type);
    }
}
