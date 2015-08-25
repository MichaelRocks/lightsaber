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
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public class AnnotationDataBuilder {
    private final Type annotationType;
    @Nullable
    private Map<String, Object> values;
    private boolean isResolved = false;

    public AnnotationDataBuilder(final Type annotationType) {
        this.annotationType = annotationType;
    }

    public AnnotationDataBuilder addDefaultValue(final Object defaultValue) {
        return addDefaultValue("value", defaultValue);
    }

    public AnnotationDataBuilder addDefaultValue(final String name, final Object defaultValue) {
        Validate.notNull(name);
        Validate.notNull(defaultValue);
        if (values == null) {
            values = new HashMap<>();
        }
        values.put(name, defaultValue);
        return this;
    }

    public AnnotationDataBuilder setResolved(final boolean resolved) {
        isResolved = resolved;
        return this;
    }

    public AnnotationData build() {
        final Map<String, Object> unmodifiableValues =
                values == null ? Collections.<String, Object>emptyMap() : Collections.unmodifiableMap(values);
        return new AnnotationData(annotationType, unmodifiableValues, isResolved);
    }
}
