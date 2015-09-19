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

package io.michaelrocks.lightsaber.processor.watermark;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class WatermarkClassVisitor extends ClassVisitor {
    private final boolean isDirtyByDefault;
    private boolean isDirty;
    private boolean isAttributeAdded;

    public WatermarkClassVisitor(final ClassVisitor classVisitor, final boolean isDirtyByDefault) {
        super(Opcodes.ASM5, classVisitor);
        this.isDirtyByDefault = isDirtyByDefault;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(final boolean dirty) {
        isDirty = dirty;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {
        isDirty = isDirtyByDefault;
        isAttributeAdded = false;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitAttribute(final Attribute attr) {
        if (attr instanceof LightsaberAttribute) {
            if (!isAttributeAdded) {
                super.visitAttribute(attr);
            }
            isAttributeAdded = true;
        } else {
            super.visitAttribute(attr);
        }
    }

    @Override
    public void visitEnd() {
        if (!isAttributeAdded && isDirty()) {
            visitAttribute(new LightsaberAttribute());
        }
        super.visitEnd();
    }
}
