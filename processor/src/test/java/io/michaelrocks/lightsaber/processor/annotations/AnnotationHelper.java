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

import org.apache.commons.lang3.tuple.Pair;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class AnnotationHelper {
    public static AnnotationDescriptor createAnnotationDescriptor(final String annotationName) {
        return new AnnotationDescriptor(getAnnotationType(annotationName), Collections.<String, Type>emptyMap());
    }

    public static AnnotationDescriptor createAnnotationDescriptor(final String annotationName, final Type type) {
        return createAnnotationDescriptor(annotationName, "value", type);
    }

    public static AnnotationDescriptor createAnnotationDescriptor(final String annotationName, final String name,
            final Type type) {
        return createAnnotationDescriptor(annotationName, Collections.singletonMap(name, type));
    }

    @SafeVarargs
    public static AnnotationDescriptor createAnnotationDescriptor(final String annotationName,
            final Pair<String, Type>... values) {
        return new AnnotationDescriptor(getAnnotationType(annotationName), pairsToMap(values));
    }

    public static AnnotationData createAnnotationData(final String annotationName) {
        return new AnnotationData(getAnnotationType(annotationName), Collections.<String, Object>emptyMap(), false);
    }

    public static AnnotationData createAnnotationData(final String annotationName, final Object defaultValue) {
        return createAnnotationData(annotationName, "value", defaultValue);
    }

    public static AnnotationData createAnnotationData(final String annotationName, final String methodName,
            final Object defaultValue) {
        return createAnnotationData(annotationName, Collections.singletonMap(methodName, defaultValue));
    }

    public static AnnotationDescriptor createAnnotationDescriptor(final String annotationName,
            final Map<String, Type> fields) {
        return new AnnotationDescriptor(getAnnotationType(annotationName), fields);
    }

    @SafeVarargs
    public static AnnotationData createAnnotationData(final String annotationName,
            final Pair<String, ?>... values) {
        return new AnnotationData(getAnnotationType(annotationName), pairsToMap(values), false);
    }

    public static AnnotationData createAnnotationData(final String annotationName,
            final Map<String, Object> values) {
        return new AnnotationData(getAnnotationType(annotationName), values, false);
    }

    public static AnnotationData createResolvedAnnotationData(final String annotationName) {
        return new AnnotationData(getAnnotationType(annotationName), Collections.<String, Object>emptyMap(), true);
    }

    public static AnnotationData createResolvedAnnotationData(final String annotationName, final Object defaultValue) {
        return createResolvedAnnotationData(annotationName, "value", defaultValue);
    }

    public static AnnotationData createResolvedAnnotationData(final String annotationName, final String methodName,
            final Object defaultValue) {
        return createResolvedAnnotationData(annotationName, Collections.singletonMap(methodName, defaultValue));
    }

    @SafeVarargs
    public static AnnotationData createResolvedAnnotationData(final String annotationName,
            final Pair<String, ?>... values) {
        return new AnnotationData(getAnnotationType(annotationName), pairsToMap(values), false);
    }

    public static AnnotationData createResolvedAnnotationData(final String annotationName,
            final Map<String, Object> values) {
        return new AnnotationData(getAnnotationType(annotationName), values, true);
    }

    public static Type getAnnotationType(final String annotationName) {
        return Type.getObjectType(annotationName);
    }

    private static <T> Map<String, T> pairsToMap(final Pair<String, ? extends T>[] pairs) {
        final Map<String, T> values = new HashMap<>();
        for (final Pair<String, ? extends T> pair : pairs) {
            values.put(pair.getKey(), pair.getValue());
        }
        return values;
    }
}
