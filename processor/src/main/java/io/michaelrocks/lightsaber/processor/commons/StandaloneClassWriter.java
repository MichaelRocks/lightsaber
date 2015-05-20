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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class StandaloneClassWriter extends ClassWriter {
    public StandaloneClassWriter(final int flags) {
        super(flags);
    }

    public StandaloneClassWriter(final ClassReader classReader, final int flags) {
        super(classReader, flags);
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        // FIXME: We need to determine a common super class by analyzing a type graph.
        try {
            return super.getCommonSuperClass(type1, type2);
        } catch (final Exception exception) {
            System.out.println("StandaloneClassWriter: super.getCommonSuperClass() failed: " +
                    "type1 = " + type1 + ", type2 = " + type2);
            return "java/lang/Object";
        }
    }
}
