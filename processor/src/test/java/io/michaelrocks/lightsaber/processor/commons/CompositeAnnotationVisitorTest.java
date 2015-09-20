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

import static io.michaelrocks.lightsaber.processor.commons.CompositeVisitorVerifier.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CompositeAnnotationVisitorTest {
    @Test
    public void testIsEmpty() throws Exception {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        assertTrue(compositeAnnotationVisitor.isEmpty());
    }

    @Test
    public void testIsNotEmpty() throws Exception {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        compositeAnnotationVisitor.addVisitor(mock(AnnotationVisitor.class));
        assertFalse(compositeAnnotationVisitor.isEmpty());
    }

    @Test
    public void testEmptyVisit() throws Exception {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        compositeAnnotationVisitor.visitEnd();
    }

    @Test
    public void testVisit() throws Exception {
        final Object value = new Object();
        verifyMethodInvocations(CompositeAnnotationVisitor.class, new Action<AnnotationVisitor, Void>() {
            @Override
            public Void invoke(final AnnotationVisitor instance) {
                instance.visit("Name", value);
                return null;
            }
        });
    }

    @Test
    public void testVisitEnum() throws Exception {
        verifyMethodInvocations(CompositeAnnotationVisitor.class, new Action<AnnotationVisitor, Void>() {
            @Override
            public Void invoke(final AnnotationVisitor instance) {
                instance.visitEnum("Name", "Desc", "Value");
                return null;
            }
        });
    }

    @Test
    public void testVisitAnnotation() throws Exception {
        verifyCompositeMethodInvocations(CompositeAnnotationVisitor.class,
                new Action<AnnotationVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final AnnotationVisitor instance) {
                        return instance.visitAnnotation("Name", "Desc");
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
    public void testVisitArray() throws Exception {
        verifyCompositeMethodInvocations(CompositeAnnotationVisitor.class,
                new Action<AnnotationVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final AnnotationVisitor instance) {
                        return instance.visitArray("Name");
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
    public void testVisitEnd() throws Exception {
        verifyMethodInvocations(CompositeAnnotationVisitor.class, new Action<AnnotationVisitor, Void>() {
            @Override
            public Void invoke(final AnnotationVisitor instance) {
                instance.visitEnd();
                return null;
            }
        });
    }
}
