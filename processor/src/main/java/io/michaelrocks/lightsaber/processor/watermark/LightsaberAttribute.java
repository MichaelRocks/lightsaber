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
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

public class LightsaberAttribute extends Attribute {
    public LightsaberAttribute() {
        this("Lightsaber");
    }

    private LightsaberAttribute(final String type) {
        super(type);
    }

    @Override
    protected LightsaberAttribute read(final ClassReader classReader, final int offset, final int length,
            final char[] buffer, final int codeOffset, final Label[] labels) {
        return new LightsaberAttribute();
    }

    @Override
    protected ByteVector write(final ClassWriter classWriter, final byte[] code, final int length, final int maxStack,
            final int maxLocals) {
        return new ByteVector();
    }
}
