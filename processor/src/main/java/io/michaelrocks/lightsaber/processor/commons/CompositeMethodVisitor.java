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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.List;

public class CompositeMethodVisitor extends MethodVisitor implements CompositeVisitor<MethodVisitor> {
    private final List<MethodVisitor> methodVisitors = new ArrayList<>();

    public CompositeMethodVisitor() {
        super(Opcodes.ASM5);
    }

    @Override
    public void addVisitor(final MethodVisitor visitor) {
        methodVisitors.add(visitor);
    }

    @Override
    public boolean isEmpty() {
        return methodVisitors.isEmpty();
    }

    @Override
    public void visitParameter(final String name, final int access) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitParameter(name, access);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final MethodVisitor methodVisitor : methodVisitors) {
            final AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotationDefault();
            CompositeVisitorHelper.addVisitor(compositeAnnotationVisitor, annotationVisitor);
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final MethodVisitor methodVisitor : methodVisitors) {
            final AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotation(desc, visible);
            CompositeVisitorHelper.addVisitor(compositeAnnotationVisitor, annotationVisitor);
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String desc,
            final boolean visible) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final MethodVisitor methodVisitor : methodVisitors) {
            final AnnotationVisitor annotationVisitor =
                    methodVisitor.visitTypeAnnotation(typeRef, typePath, desc, visible);
            CompositeVisitorHelper.addVisitor(compositeAnnotationVisitor, annotationVisitor);
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final MethodVisitor methodVisitor : methodVisitors) {
            final AnnotationVisitor annotationVisitor =
                    methodVisitor.visitParameterAnnotation(parameter, desc, visible);
            if (annotationVisitor != null) {
                compositeAnnotationVisitor.addVisitor(annotationVisitor);
            }
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitAttribute(attr);
        }
    }

    @Override
    public void visitCode() {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitCode();
        }
    }

    @Override
    public void visitFrame(final int type, final int nLocal, final Object[] local, final int nStack,
            final Object[] stack) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitFrame(type, nLocal, local, nStack, stack);
        }
    }

    @Override
    public void visitInsn(final int opcode) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitInsn(opcode);
        }
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitIntInsn(opcode, operand);
        }
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitVarInsn(opcode, var);
        }
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitTypeInsn(opcode, type);
        }
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner, final String name, final String desc) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitFieldInsn(opcode, owner, name, desc);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner, final String name, final String desc,
            final boolean itf) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }

    @Override
    public void visitInvokeDynamicInsn(final String name, final String desc, final Handle bsm,
            final Object... bsmArgs) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitJumpInsn(opcode, label);
        }
    }

    @Override
    public void visitLabel(final Label label) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitLabel(label);
        }
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitLdcInsn(cst);
        }
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitIincInsn(var, increment);
        }
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max, final Label dflt, final Label... labels) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitTableSwitchInsn(min, max, dflt, labels);
        }
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitLookupSwitchInsn(dflt, keys, labels);
        }
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitMultiANewArrayInsn(desc, dims);
        }
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(final int typeRef, final TypePath typePath, final String desc,
            final boolean visible) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final MethodVisitor methodVisitor : methodVisitors) {
            final AnnotationVisitor annotationVisitor =
                    methodVisitor.visitInsnAnnotation(typeRef, typePath, desc, visible);
            CompositeVisitorHelper.addVisitor(compositeAnnotationVisitor, annotationVisitor);
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitTryCatchBlock(start, end, handler, type);
        }
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String desc,
            final boolean visible) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final MethodVisitor methodVisitor : methodVisitors) {
            final AnnotationVisitor annotationVisitor =
                    methodVisitor.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
            CompositeVisitorHelper.addVisitor(compositeAnnotationVisitor, annotationVisitor);
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public void visitLocalVariable(final String name, final String desc, final String signature, final Label start,
            final Label end, final int index) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitLocalVariable(name, desc, signature, start, end, index);
        }
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath,
            final Label[] start, final Label[] end,
            final int[] index, final String desc, final boolean visible) {
        final CompositeAnnotationVisitor compositeAnnotationVisitor = new CompositeAnnotationVisitor();
        for (final MethodVisitor methodVisitor : methodVisitors) {
            final AnnotationVisitor annotationVisitor =
                    methodVisitor.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
            CompositeVisitorHelper.addVisitor(compositeAnnotationVisitor, annotationVisitor);
        }
        return compositeAnnotationVisitor.isEmpty() ? null : compositeAnnotationVisitor;
    }

    @Override
    public void visitLineNumber(final int line, final Label start) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitLineNumber(line, start);
        }
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitMaxs(maxStack, maxLocals);
        }
    }

    @Override
    public void visitEnd() {
        for (final MethodVisitor methodVisitor : methodVisitors) {
            methodVisitor.visitEnd();
        }
    }
}
