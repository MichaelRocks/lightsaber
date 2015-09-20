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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationRegistry {
    private final Map<Type, AnnotationDescriptor> annotationsByType = new HashMap<>();
    private final Map<Type, AnnotationData> unresolvedDefaultsByType = new HashMap<>();
    private final Map<Type, AnnotationData> resolvedDefaultsByType = new HashMap<>();

    public void addAnnotationDefaults(final AnnotationDescriptor annotation, final AnnotationData defaults) {
        Validate.isTrue(annotation.getType().equals(defaults.getType()));

        if (annotationsByType.containsKey(annotation.getType())) {
            System.out.println("Annotation already registered: " + annotation.getType());
            return;
        }

        annotationsByType.put(annotation.getType(), annotation);

        if (defaults.isResolved()) {
            Validate.isTrue(!unresolvedDefaultsByType.containsKey(annotation.getType()));
            resolvedDefaultsByType.put(annotation.getType(), defaults);
        } else {
            Validate.isTrue(!resolvedDefaultsByType.containsKey(annotation.getType()));
            unresolvedDefaultsByType.put(annotation.getType(), defaults);
        }
    }

    public AnnotationDescriptor findAnnotationByType(final Type annotationType) {
        return annotationsByType.get(annotationType);
    }

    public AnnotationData resolveAnnotation(final AnnotationData data) {
        if (data.isResolved()) {
            return data;
        }

        return new AnnotationResolver().resolve(data);
    }

    boolean hasUnresolvedDefaults(final Type annotationType) {
        return unresolvedDefaultsByType.containsKey(annotationType);
    }

    boolean hasResolvedDefaults(final Type annotationType) {
        return resolvedDefaultsByType.containsKey(annotationType);
    }

    private class AnnotationResolver {
        private final Map<String, Object> values = new HashMap<>();
        private boolean resolved = true;

        AnnotationData resolve(final AnnotationData data) {
            return resolve(data, true);
        }

        private AnnotationData resolve(final AnnotationData data, final boolean applyDefaults) {
            if (applyDefaults) {
                applyDefaults(data);
            }
            for (final Map.Entry<String, Object> entry : data.getValues().entrySet()) {
                values.put(entry.getKey(), resolveObject(entry.getValue()));
            }
            return new AnnotationData(data.getType(), Collections.unmodifiableMap(values), resolved);
        }

        private void applyDefaults(final AnnotationData data) {
            final AnnotationData defaults = resolveDefaults(data.getType());
            if (defaults == null) {
                resolved = false;
                return;
            }

            resolved &= defaults.isResolved();
            values.putAll(defaults.getValues());
        }

        private AnnotationData resolveDefaults(final Type annotationType) {
            AnnotationData resolvedDefaults = resolvedDefaultsByType.get(annotationType);
            if (resolvedDefaults == null) {
                final AnnotationData unresolvedDefaults = unresolvedDefaultsByType.remove(annotationType);
                if (unresolvedDefaults != null) {
                    resolvedDefaults = unresolvedDefaults.isResolved()
                            ? unresolvedDefaults
                            : new AnnotationResolver().resolve(unresolvedDefaults, false);
                    resolvedDefaultsByType.put(annotationType, resolvedDefaults);
                }
            }
            return resolvedDefaults;
        }

        private Object resolveObject(final Object value) {
            if (value instanceof AnnotationData) {
                final AnnotationData data = resolveAnnotation((AnnotationData) value);
                resolved &= data.isResolved();
                return data;
            } else if (value instanceof List<?>) {
                return resolveArray((List<?>) value);
            } else {
                return value;
            }
        }

        private List<?> resolveArray(final List<?> array) {
            final List<Object> resolvedArray = new ArrayList<>(array.size());
            for (final Object value : array) {
                resolvedArray.add(resolveObject(value));
            }
            return resolvedArray;
        }
    }
}
