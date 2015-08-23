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
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypeReference;

import static io.michaelrocks.lightsaber.processor.commons.CompositeVisitorVerifier.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class CompositeMethodVisitorTest {
    @Test
    public void testIsEmpty() throws Exception {
        final CompositeMethodVisitor compositeMethodVisitor = new CompositeMethodVisitor();
        assertTrue(compositeMethodVisitor.isEmpty());
    }

    @Test
    public void testIsNotEmpty() throws Exception {
        final CompositeMethodVisitor compositeMethodVisitor = new CompositeMethodVisitor();
        compositeMethodVisitor.addVisitor(mock(MethodVisitor.class));
        assertFalse(compositeMethodVisitor.isEmpty());
    }

    @Test
    public void testEmptyVisit() throws Exception {
        final CompositeMethodVisitor compositeMethodVisitor = new CompositeMethodVisitor();
        compositeMethodVisitor.visitEnd();
    }

    @Test
    public void testVisitParameter() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitParameter("Name", Opcodes.ACC_FINAL);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitAnnotationDefault() throws Exception {
        verifyCompositeMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final MethodVisitor instance) {
                        return instance.visitAnnotationDefault();
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
    public void testVisitAnnotation() throws Exception {
        verifyCompositeMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final MethodVisitor instance) {
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
        verifyCompositeMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final MethodVisitor instance) {
                        return instance.visitTypeAnnotation(TypeReference.METHOD_TYPE_PARAMETER, null, "Desc", true);
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
    public void testVisitParameterAnnotation() throws Exception {
        verifyCompositeMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final MethodVisitor instance) {
                        return instance.visitParameterAnnotation(1, "Desc", true);
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
        final Attribute attribute = new Attribute("AttributeType") {
        };
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitAttribute(attribute);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitCode() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitCode();
                        return null;
                    }
                });
    }

    @Test
    public void testVisitFrame() throws Exception {
        final Object[] local = new Object[] {};
        final Object[] stack = new Object[] { new Object() };
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitFrame(Opcodes.F_NEW, 0, local, 1, stack);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitInsn() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitInsn(Opcodes.AALOAD);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitIntInsn() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitIntInsn(Opcodes.NEWARRAY, 1);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitVarInsn() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitVarInsn(Opcodes.ILOAD, 1);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitTypeInsn() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitTypeInsn(Opcodes.NEW, "Type");
                        return null;
                    }
                });
    }

    @Test
    public void testVisitFieldInsn() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitFieldInsn(Opcodes.GETFIELD, "Owner", "Name", "Desc");
                        return null;
                    }
                });
    }

    @Test
    public void testVisitMethodInsn() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Owner", "Name", "Desc");
                        return null;
                    }
                });
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "Owner", "Name", "Desc", true);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitInvokeDynamicInsn() throws Exception {
        final Handle bootstrapMethod = new Handle(Opcodes.H_INVOKEINTERFACE, "Owner", "Name", "Desc");
        final Object[] arguments = new Object[] { "Argument" };
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitInvokeDynamicInsn("Name", "Desc", bootstrapMethod, arguments);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitJumpInsn() throws Exception {
        final Label label = new Label();
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitJumpInsn(Opcodes.IFEQ, label);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitLabel() throws Exception {
        final Label label = new Label();
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitLabel(label);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitLdcInsn() throws Exception {
        final Object object = new Object();
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitLdcInsn(object);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitIincInsn() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitIincInsn(1, 2);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitTableSwitchInsn() throws Exception {
        final Label label = new Label();
        final Label[] labels = new Label[] { new Label() };
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitTableSwitchInsn(1, 2, label, labels);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitLookupSwitchInsn() throws Exception {
        final Label label = new Label();
        final int[] keys = new int[] { 1, 2 };
        final Label[] labels = new Label[] { new Label() };
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitLookupSwitchInsn(label, keys, labels);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitMultiANewArrayInsn() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitMultiANewArrayInsn("Desc", 2);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitInsnAnnotation() throws Exception {
        verifyCompositeMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final MethodVisitor instance) {
                        return instance.visitInsnAnnotation(TypeReference.METHOD_TYPE_PARAMETER, null, "Desc", true);
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
    public void testVisitTryCatchBlock() throws Exception {
        final Label start = new Label();
        final Label end = new Label();
        final Label handler = new Label();
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitTryCatchBlock(start, end, handler, "Type");
                        return null;
                    }
                });
    }

    @Test
    public void testVisitTryCatchAnnotation() throws Exception {
        verifyCompositeMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final MethodVisitor instance) {
                        return instance.visitTryCatchAnnotation(TypeReference.METHOD_TYPE_PARAMETER, null, "Desc",
                                true);
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
    public void testVisitLocalVariable() throws Exception {
        final Label start = new Label();
        final Label end = new Label();
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitLocalVariable("Name", "Desc", "Signature", start, end, 2);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitLocalVariableAnnotation() throws Exception {
        final Label[] start = new Label[] { new Label() };
        final Label[] end = new Label[] { new Label() };
        final int[] index = new int[] { 1, 2 };
        verifyCompositeMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, AnnotationVisitor>() {
                    @Override
                    public AnnotationVisitor invoke(final MethodVisitor instance) {
                        return instance.visitLocalVariableAnnotation(TypeReference.METHOD_TYPE_PARAMETER, null,
                                start, end, index, "Desc", true);
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
    public void testVisitLineNumber() throws Exception {
        final Label label = new Label();
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitLineNumber(2, label);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitMaxs() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitMaxs(1, 2);
                        return null;
                    }
                });
    }

    @Test
    public void testVisitEnd() throws Exception {
        verifyMethodInvocations(CompositeMethodVisitor.class,
                new Action<MethodVisitor, Object>() {
                    @Override
                    public Object invoke(final MethodVisitor instance) {
                        instance.visitEnd();
                        return null;
                    }
                });
    }
}