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
import org.junit.Test;
import org.objectweb.asm.Type;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnnotationRegistryTest {
    @Test
    public void testEmptyAnnotationRegistry() throws Exception {
        final Type annotationType = AnnotationHelper.getAnnotationType("NonExistentAnnotation");
        final AnnotationRegistry registry = new AnnotationRegistry();
        assertTrue(!registry.hasResolvedDefaults(annotationType));
        assertTrue(!registry.hasUnresolvedDefaults(annotationType));
    }

    @Test
    public void testAddResolvedEmptyDefaults() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotation("ResolvedEmptyAnnotation");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testAddUnresolvedEmptyDefaults() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotation("UnresolvedEmptyAnnotation");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        assertTrue(!registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testResolveEmptyAnnotationWithResolvedEmptyDefaults() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotation("ResolvedEmptyAnnotation");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation = registry.resolveAnnotation(annotationDefaults);
        assertTrue(annotationDefaults == resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testResolveEmptyAnnotationWithUnresolvedEmptyDefaults() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotation("UnresolvedEmptyAnnotation");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation = registry.resolveAnnotation(annotationDefaults);
        assertTrue(annotationDefaults != resolvedAnnotation);
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testResolveEmptyAnnotationWithResolvedSimpleDefaults() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotation("ResolvedSimpleAnnotation", "DefaultValue");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotation("ResolvedSimpleAnnotation"));
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testResolveEmptyAnnotationWithUnresolvedSimpleDefaults() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotation("UnresolvedSimpleAnnotation", "DefaultValue");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotation("UnresolvedSimpleAnnotation"));
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testResolveAnnotationWithOverriddenField() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotation("OverriddenFieldAnnotation", "DefaultValue");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData unresolvedAnnotation =
                AnnotationHelper.createAnnotation("OverriddenFieldAnnotation", "DefaultValue");
        final AnnotationData resolvedAnnotation = registry.resolveAnnotation(unresolvedAnnotation);
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotation("OverriddenFieldAnnotation", "DefaultValue");
        assertEquals(expectedAnnotation, resolvedAnnotation);
    }

    @Test
    public void testResolveAnnotationWithExtraField() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotation("ExtraFieldAnnotation", "DefaultValue");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData unresolvedAnnotation =
                AnnotationHelper.createAnnotation("ExtraFieldAnnotation", "extraValue", "ExtraValue");
        final AnnotationData resolvedAnnotation = registry.resolveAnnotation(unresolvedAnnotation);
        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("value", "DefaultValue");
        expectedValues.put("extraValue", "ExtraValue");
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotation("ExtraFieldAnnotation", expectedValues);
        assertEquals(expectedAnnotation, resolvedAnnotation);
    }

    @Test
    public void testResolveResolvedAnnotationWithEmptyInnerResolvedAnnotation() throws Exception {
        final AnnotationData innerAnnotation = AnnotationHelper.createResolvedAnnotation("InnerAnnotation");
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotation("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotation("OuterAnnotation"));
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasResolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithEmptyInnerResolvedAnnotation() throws Exception {
        final AnnotationData innerAnnotation = AnnotationHelper.createResolvedAnnotation("InnerAnnotation");
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotation("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotation("OuterAnnotation"));
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotation("OuterAnnotation", innerAnnotation);
        assertEquals(expectedAnnotation, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasResolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithEmptyInnerUnresolvedAnnotation() throws Exception {
        final AnnotationData innerAnnotation = AnnotationHelper.createAnnotation("InnerAnnotation");
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotation("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotation("OuterAnnotation"));
        final AnnotationData resolvedInnerAnnotation =
                AnnotationHelper.createResolvedAnnotation("InnerAnnotation");
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotation("OuterAnnotation", resolvedInnerAnnotation);
        assertEquals(expectedAnnotation, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasResolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithInnerUnresolvableAnnotation() throws Exception {
        final AnnotationData unresolvableAnnotation = AnnotationHelper.createAnnotation("UnresolvableAnnotation");
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotation("OuterAnnotation", unresolvableAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotation("OuterAnnotation"));
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasResolvedDefaults(unresolvableAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(unresolvableAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithNestedInnerUnresolvableAnnotation() throws Exception {
        final AnnotationData unresolvableAnnotation = AnnotationHelper.createAnnotation("UnresolvableAnnotation");
        final AnnotationData innerAnnotation =
                AnnotationHelper.createAnnotation("InnerAnnotation", unresolvableAnnotation);
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotation("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotation("OuterAnnotation"));
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasResolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasResolvedDefaults(unresolvableAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(unresolvableAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithNestedInnerAnnotation() throws Exception {
        final AnnotationData innerInnerAnnotation =
                AnnotationHelper.createAnnotation("InnerInnerAnnotation", "InnerInnerDefaultValue");
        final AnnotationData innerAnnotation =
                AnnotationHelper.createAnnotation("InnerAnnotation", innerInnerAnnotation);
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotation("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerInnerAnnotation);
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);

        final AnnotationData unresolvedInnerInnerAnnotation =
                AnnotationHelper.createAnnotation("InnerInnerAnnotation",
                        "innerInnerStringValue", "InnerInnerStringValue");
        final AnnotationData unresolvedInnerAnnotation =
                AnnotationHelper.createAnnotation("InnerAnnotation",
                        Pair.of("value", unresolvedInnerInnerAnnotation),
                        Pair.of("innerStringValue", "InnerStringValue"));
        final AnnotationData unresolvedAnnotation =
                AnnotationHelper.createAnnotation("OuterAnnotation",
                        Pair.of("value", unresolvedInnerAnnotation),
                        Pair.of("outerStringValue", "OuterStringValue"));
        final AnnotationData resolvedAnnotation = registry.resolveAnnotation(unresolvedAnnotation);

        final AnnotationData expectedInnerInnerAnnotation =
                AnnotationHelper.createAnnotation("InnerInnerAnnotation",
                        Pair.of("value", "InnerInnerDefaultValue"),
                        Pair.of("innerInnerStringValue", "InnerInnerStringValue"));
        final AnnotationData expectedInnerAnnotation =
                AnnotationHelper.createAnnotation("InnerAnnotation",
                        Pair.of("value", expectedInnerInnerAnnotation),
                        Pair.of("innerStringValue", "InnerStringValue"));
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createAnnotation("OuterAnnotation",
                        Pair.of("value", expectedInnerAnnotation),
                        Pair.of("outerStringValue", "OuterStringValue"));
        assertEquals(expectedAnnotation, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasResolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.getType()));
        assertTrue(registry.hasResolvedDefaults(innerInnerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerInnerAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithNestedUnresolvedAnnotationArray() throws Exception {
        final AnnotationData innerAnnotation =
                AnnotationHelper.createAnnotation("InnerAnnotation", "DefaultValue");
        final List<AnnotationData> innerAnnotations = Arrays.asList(
                AnnotationHelper.createAnnotation("InnerAnnotation", "ExplicitValue"),
                AnnotationHelper.createAnnotation("InnerAnnotation", "stringValue", "StringValue"));
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotation("OuterAnnotation", innerAnnotations);
        final AnnotationRegistry registry = new AnnotationRegistry();

        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotation("OuterAnnotation"));

        final List<AnnotationData> expectedInnerAnnotations = Arrays.asList(
                AnnotationHelper.createResolvedAnnotation("InnerAnnotation", "ExplicitValue"),
                AnnotationHelper.createResolvedAnnotation("InnerAnnotation",
                        Pair.of("stringValue", "StringValue"),
                        Pair.of("value", "DefaultValue")));
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotation("OuterAnnotation", expectedInnerAnnotations);

        assertEquals(expectedAnnotation, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasResolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.getType()));
    }

    private void registerDefaults(final AnnotationRegistry registry, final AnnotationData defaults) {
        final AnnotationDescriptor descriptor =
                new AnnotationDescriptor(defaults.getType(), Collections.<String, Type>emptyMap());
        registry.addAnnotationDefaults(descriptor, defaults);
    }
}
