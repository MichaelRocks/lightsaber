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

package io.michaelrocks.lightsaber.processor.commons;

import org.junit.Test;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.TypeReference;

import static io.michaelrocks.lightsaber.processor.commons.CompositeVisitorVerifier.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CompositeFieldVisitorTest {
    @Test
    public void testIsEmpty() throws Exception {
        final CompositeFieldVisitor compositeFieldVisitor = new CompositeFieldVisitor();
        assertTrue(compositeFieldVisitor.isEmpty());
    }

    @Test
    public void testIsNotEmpty() throws Exception {
        final CompositeFieldVisitor compositeFieldVisitor = new CompositeFieldVisitor();
        compositeFieldVisitor.addVisitor(mock(FieldVisitor.class));
        assertFalse(compositeFieldVisitor.isEmpty());
    }

    @Test
    public void testEmptyVisit() throws Exception {
        final CompositeFieldVisitor compositeFieldVisitor = new CompositeFieldVisitor();
        compositeFieldVisitor.visitEnd();
    }

    @Test
    public void testVisitAnnotation() throws Exception {
        verifyCompositeMethodInvocations(CompositeFieldVisitor.class,
                new Action<FieldVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final FieldVisitor instance) {
                        return instance.visitAnnotation("Desc", true);
                    }
                },
                new Action<AnnotationVisitor, Object>() {
                    @Override
                    public Object invoke(final AnnotationVisitor instance) {
                        instance.visitEnd();
                        return null;
                    }
                });
    }

    @Test
    public void testVisitTypeAnnotation() throws Exception {
        verifyCompositeMethodInvocations(CompositeFieldVisitor.class,
                new Action<FieldVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final FieldVisitor instance) {
                        return instance.visitTypeAnnotation(TypeReference.FIELD, null, "Desc", true);
                    }
                },
                new Action<AnnotationVisitor, Object>() {
                    @Override
                    public Object invoke(final AnnotationVisitor instance) {
                        instance.visitEnd();
                        return null;
                    }
                });
    }

    @Test
    public void testVisitAttribute() throws Exception {
        final Attribute attribute = new Attribute("AttributeType") {};
        verifyMethodInvocations(CompositeFieldVisitor.class,
                new Action<FieldVisitor, Object>() {
                    @Override
                    public Object invoke(final FieldVisitor instance) {
                        instance.visitAttribute(attribute);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitEnd() throws Exception {
        verifyMethodInvocations(CompositeFieldVisitor.class,
                new Action<FieldVisitor, Object>() {
                    @Override
                    public Object invoke(final FieldVisitor instance) {
                        instance.visitEnd();
                        return null;
                    }
                });
    }
}
