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

import io.michaelrocks.lightsaber.processor.descriptors.EnumValueDescriptor;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public abstract class AbstractAnnotationParser extends AnnotationVisitor {
    protected AbstractAnnotationParser() {
        super(Opcodes.ASM5);
    }

    @Override
    public void visit(final String name, final Object value) {
        addValue(name, value);
    }

    @Override
    public void visitEnum(final String name, final String desc, final String value) {
        addValue(name, EnumValueDescriptor.from(desc, value));
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        final AbstractAnnotationParser parent = this;
        return new AnnotationInstanceParser(Type.getType(desc)) {
            @Override
            public void visitEnd() {
                parent.addValue(name, toAnnotation());
            }
        };
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
        final AbstractAnnotationParser parent = this;
        return new AnnotationArrayParser() {
            @Override
            public void visitEnd() {
                parent.addValue(name, getValues());
            }
        };
    }

    protected abstract void addValue(@Nullable String name, Object value);
}
