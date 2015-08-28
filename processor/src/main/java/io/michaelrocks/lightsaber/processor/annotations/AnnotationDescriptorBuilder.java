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

import org.apache.commons.lang3.Validate;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class AnnotationDescriptorBuilder {
    private final Type annotationType;
    @Nullable
    private Map<String, Type> fields;

    public AnnotationDescriptorBuilder(final Type annotationType) {
        this.annotationType = annotationType;
    }

    public AnnotationDescriptorBuilder addDefaultField(final Type type) {
        return addField("value", type);
    }

    public AnnotationDescriptorBuilder addField(final String name, final Type type) {
        Validate.notNull(name);
        Validate.notNull(type);
        if (fields == null) {
            fields = new LinkedHashMap<>();
        }
        fields.put(name, type);
        return this;
    }

    public AnnotationDescriptor build() {
        final Map<String, Type> unmodifiableFields =
                fields == null ? Collections.<String, Type>emptyMap() : Collections.unmodifiableMap(fields);
        return new AnnotationDescriptor(annotationType, unmodifiableFields);
    }
}
