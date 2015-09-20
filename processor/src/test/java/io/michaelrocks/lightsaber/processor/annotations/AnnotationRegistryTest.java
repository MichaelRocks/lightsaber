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
                AnnotationHelper.createResolvedAnnotationData("ResolvedEmptyAnnotation");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testAddUnresolvedEmptyDefaults() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotationData("UnresolvedEmptyAnnotation");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        assertTrue(!registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testResolveEmptyAnnotationWithResolvedEmptyDefaults() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotationData("ResolvedEmptyAnnotation");
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
                AnnotationHelper.createAnnotationData("UnresolvedEmptyAnnotation");
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
                AnnotationHelper.createResolvedAnnotationData("ResolvedSimpleAnnotation", "DefaultValue");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotationData("ResolvedSimpleAnnotation"));
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testResolveEmptyAnnotationWithUnresolvedSimpleDefaults() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotationData("UnresolvedSimpleAnnotation", "DefaultValue");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotationData("UnresolvedSimpleAnnotation"));
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
    }

    @Test
    public void testResolveAnnotationWithOverriddenField() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotationData("OverriddenFieldAnnotation", "DefaultValue");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData unresolvedAnnotation =
                AnnotationHelper.createAnnotationData("OverriddenFieldAnnotation", "DefaultValue");
        final AnnotationData resolvedAnnotation = registry.resolveAnnotation(unresolvedAnnotation);
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotationData("OverriddenFieldAnnotation", "DefaultValue");
        assertEquals(expectedAnnotation, resolvedAnnotation);
    }

    @Test
    public void testResolveAnnotationWithExtraField() throws Exception {
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotationData("ExtraFieldAnnotation", "DefaultValue");
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData unresolvedAnnotation =
                AnnotationHelper.createAnnotationData("ExtraFieldAnnotation", "extraValue", "ExtraValue");
        final AnnotationData resolvedAnnotation = registry.resolveAnnotation(unresolvedAnnotation);
        final Map<String, Object> expectedValues = new HashMap<>();
        expectedValues.put("value", "DefaultValue");
        expectedValues.put("extraValue", "ExtraValue");
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotationData("ExtraFieldAnnotation", expectedValues);
        assertEquals(expectedAnnotation, resolvedAnnotation);
    }

    @Test
    public void testResolveResolvedAnnotationWithEmptyInnerResolvedAnnotation() throws Exception {
        final AnnotationData innerAnnotation = AnnotationHelper.createResolvedAnnotationData("InnerAnnotation");
        final AnnotationData annotationDefaults =
                AnnotationHelper.createResolvedAnnotationData("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"));
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasResolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithEmptyInnerResolvedAnnotation() throws Exception {
        final AnnotationData innerAnnotation = AnnotationHelper.createResolvedAnnotationData("InnerAnnotation");
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"));
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotationData("OuterAnnotation", innerAnnotation);
        assertEquals(expectedAnnotation, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasResolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithEmptyInnerUnresolvedAnnotation() throws Exception {
        final AnnotationData innerAnnotation = AnnotationHelper.createAnnotationData("InnerAnnotation");
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"));
        final AnnotationData resolvedInnerAnnotation =
                AnnotationHelper.createResolvedAnnotationData("InnerAnnotation");
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotationData("OuterAnnotation", resolvedInnerAnnotation);
        assertEquals(expectedAnnotation, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(registry.hasResolvedDefaults(innerAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithInnerUnresolvableAnnotation() throws Exception {
        final AnnotationData unresolvableAnnotation = AnnotationHelper.createAnnotationData("UnresolvableAnnotation");
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotationData("OuterAnnotation", unresolvableAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"));
        assertEquals(annotationDefaults, resolvedAnnotation);
        assertTrue(registry.hasResolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.getType()));
        assertTrue(!registry.hasResolvedDefaults(unresolvableAnnotation.getType()));
        assertTrue(!registry.hasUnresolvedDefaults(unresolvableAnnotation.getType()));
    }

    @Test
    public void testResolveUnresolvedAnnotationWithNestedInnerUnresolvableAnnotation() throws Exception {
        final AnnotationData unresolvableAnnotation = AnnotationHelper.createAnnotationData("UnresolvableAnnotation");
        final AnnotationData innerAnnotation =
                AnnotationHelper.createAnnotationData("InnerAnnotation", unresolvableAnnotation);
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"));
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
                AnnotationHelper.createAnnotationData("InnerInnerAnnotation", "InnerInnerDefaultValue");
        final AnnotationData innerAnnotation =
                AnnotationHelper.createAnnotationData("InnerAnnotation", innerInnerAnnotation);
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotation);
        final AnnotationRegistry registry = new AnnotationRegistry();
        registerDefaults(registry, innerInnerAnnotation);
        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);

        final AnnotationData unresolvedInnerInnerAnnotation =
                AnnotationHelper.createAnnotationData("InnerInnerAnnotation",
                        "innerInnerStringValue", "InnerInnerStringValue");
        final AnnotationData unresolvedInnerAnnotation =
                AnnotationHelper.createAnnotationData("InnerAnnotation",
                        Pair.of("value", unresolvedInnerInnerAnnotation),
                        Pair.of("innerStringValue", "InnerStringValue"));
        final AnnotationData unresolvedAnnotation =
                AnnotationHelper.createAnnotationData("OuterAnnotation",
                        Pair.of("value", unresolvedInnerAnnotation),
                        Pair.of("outerStringValue", "OuterStringValue"));
        final AnnotationData resolvedAnnotation = registry.resolveAnnotation(unresolvedAnnotation);

        final AnnotationData expectedInnerInnerAnnotation =
                AnnotationHelper.createAnnotationData("InnerInnerAnnotation",
                        Pair.of("value", "InnerInnerDefaultValue"),
                        Pair.of("innerInnerStringValue", "InnerInnerStringValue"));
        final AnnotationData expectedInnerAnnotation =
                AnnotationHelper.createAnnotationData("InnerAnnotation",
                        Pair.of("value", expectedInnerInnerAnnotation),
                        Pair.of("innerStringValue", "InnerStringValue"));
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createAnnotationData("OuterAnnotation",
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
                AnnotationHelper.createAnnotationData("InnerAnnotation", "DefaultValue");
        final List<AnnotationData> innerAnnotations = Arrays.asList(
                AnnotationHelper.createAnnotationData("InnerAnnotation", "ExplicitValue"),
                AnnotationHelper.createAnnotationData("InnerAnnotation", "stringValue", "StringValue"));
        final AnnotationData annotationDefaults =
                AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotations);
        final AnnotationRegistry registry = new AnnotationRegistry();

        registerDefaults(registry, innerAnnotation);
        registerDefaults(registry, annotationDefaults);
        final AnnotationData resolvedAnnotation =
                registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"));

        final List<AnnotationData> expectedInnerAnnotations = Arrays.asList(
                AnnotationHelper.createResolvedAnnotationData("InnerAnnotation", "ExplicitValue"),
                AnnotationHelper.createResolvedAnnotationData("InnerAnnotation",
                        Pair.of("stringValue", "StringValue"),
                        Pair.of("value", "DefaultValue")));
        final AnnotationData expectedAnnotation =
                AnnotationHelper.createResolvedAnnotationData("OuterAnnotation", expectedInnerAnnotations);

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
