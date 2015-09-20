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
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypeReference;

import static io.michaelrocks.lightsaber.processor.commons.CompositeVisitorVerifier.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CompositeClassVisitorTest {
    @Test
    public void testIsEmpty() throws Exception {
        final CompositeClassVisitor compositeClassVisitor = new CompositeClassVisitor();
        assertTrue(compositeClassVisitor.isEmpty());
    }

    @Test
    public void testIsNotEmpty() throws Exception {
        final CompositeClassVisitor compositeClassVisitor = new CompositeClassVisitor();
        compositeClassVisitor.addVisitor(mock(ClassVisitor.class));
        assertFalse(compositeClassVisitor.isEmpty());
    }

    @Test
    public void testEmptyVisit() throws Exception {
        final CompositeClassVisitor compositeClassVisitor = new CompositeClassVisitor();
        compositeClassVisitor.visitEnd();
    }

    @Test
    public void testVisit() throws Exception {
        final String[] interfaces = new String[] { "Interface" };
        verifyMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, Object>() {
                    @Override
                    public Object invoke(final ClassVisitor instance) {
                        instance.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC, "Name", "Signature", "Super", interfaces);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitSource() throws Exception {
        verifyMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, Object>() {
                    @Override
                    public Object invoke(final ClassVisitor instance) {
                        instance.visitSource("Source", "Debug");
                        return null;
                    }
                });
    }

    @Test
    public void testVisitOuterClass() throws Exception {
        verifyMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, Object>() {
                    @Override
                    public Object invoke(final ClassVisitor instance) {
                        instance.visitOuterClass("Owner", "Name", "Desc");
                        return null;
                    }
                });
    }

    @Test
    public void testVisitAnnotation() throws Exception {
        verifyCompositeMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final ClassVisitor instance) {
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
        verifyCompositeMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final ClassVisitor instance) {
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
        final Attribute attribute = mock(Attribute.class);
        verifyMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, Object>() {
                    @Override
                    public Object invoke(final ClassVisitor instance) {
                        instance.visitAttribute(attribute);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitInnerClass() throws Exception {
        verifyMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, Object>() {
                    @Override
                    public Object invoke(final ClassVisitor instance) {
                        instance.visitInnerClass("Name", "Outer", "Inner", Opcodes.ACC_PUBLIC);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitField() throws Exception {
        final Object value = new Object();
        verifyCompositeMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, FieldVisitor>() {
                    @Override
                    public FieldVisitor invoke(final ClassVisitor instance) {
                        return instance.visitField(Opcodes.ACC_PUBLIC, "Name", "Desc", "Signature", value);
                    }
                },
                new Action<FieldVisitor, Object>() {
                    @Override
                    public Object invoke(final FieldVisitor instance) {
                        instance.visitEnd();
                        return null;
                    }
                });
    }

    @Test
    public void testVisitMethod() throws Exception {
        final String[] exceptions = new String[] { "Exception" };
        verifyCompositeMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, MethodVisitor>() {
                    @Override
                    public MethodVisitor invoke(final ClassVisitor instance) {
                        return instance.visitMethod(Opcodes.ACC_PUBLIC, "Name", "Desc", "Signature", exceptions);
                    }
                },
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitEnd();
                        return null;
                    }
                });
    }

    @Test
    public void testVisitEnd() throws Exception {
        verifyMethodInvocations(CompositeClassVisitor.class,
                new Action<ClassVisitor, Object>() {
                    @Override
                    public Object invoke(final ClassVisitor instance) {
                        instance.visitEnd();
                        return null;
                    }
                });
    }
}