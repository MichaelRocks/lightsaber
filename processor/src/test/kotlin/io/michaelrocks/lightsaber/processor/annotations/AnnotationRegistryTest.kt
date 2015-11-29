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

package io.michaelrocks.lightsaber.processor.annotations

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.objectweb.asm.Type
import java.util.*

class AnnotationRegistryTest {
  @Test
  @Throws(Exception::class)
  fun testEmptyAnnotationRegistry() {
    val annotationType = AnnotationHelper.getAnnotationType("NonExistentAnnotation")
    val registry = AnnotationRegistry()
    assertTrue(!registry.hasResolvedDefaults(annotationType))
    assertTrue(!registry.hasUnresolvedDefaults(annotationType))
  }

  @Test
  @Throws(Exception::class)
  fun testAddResolvedEmptyDefaults() {
    val annotationDefaults = AnnotationHelper.createResolvedAnnotationData("ResolvedEmptyAnnotation")
    val registry = AnnotationRegistry()
    registerDefaults(registry, annotationDefaults)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
  }

  @Test
  @Throws(Exception::class)
  fun testAddUnresolvedEmptyDefaults() {
    val annotationDefaults = AnnotationHelper.createAnnotationData("UnresolvedEmptyAnnotation")
    val registry = AnnotationRegistry()
    registerDefaults(registry, annotationDefaults)
    assertTrue(!registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(registry.hasUnresolvedDefaults(annotationDefaults.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveEmptyAnnotationWithResolvedEmptyDefaults() {
    val annotationDefaults = AnnotationHelper.createResolvedAnnotationData("ResolvedEmptyAnnotation")
    val registry = AnnotationRegistry()
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(annotationDefaults)
    assertTrue(annotationDefaults === resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveEmptyAnnotationWithUnresolvedEmptyDefaults() {
    val annotationDefaults = AnnotationHelper.createAnnotationData("UnresolvedEmptyAnnotation")
    val registry = AnnotationRegistry()
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(annotationDefaults)
    assertTrue(annotationDefaults !== resolvedAnnotation)
    assertEquals(annotationDefaults, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveEmptyAnnotationWithResolvedSimpleDefaults() {
    val annotationDefaults = AnnotationHelper.createResolvedAnnotationData("ResolvedSimpleAnnotation", "DefaultValue")
    val registry = AnnotationRegistry()
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(
        AnnotationHelper.createAnnotationData("ResolvedSimpleAnnotation"))
    assertEquals(annotationDefaults, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveEmptyAnnotationWithUnresolvedSimpleDefaults() {
    val annotationDefaults = AnnotationHelper.createAnnotationData("UnresolvedSimpleAnnotation", "DefaultValue")
    val registry = AnnotationRegistry()
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(
        AnnotationHelper.createAnnotationData("UnresolvedSimpleAnnotation"))
    assertEquals(annotationDefaults, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveAnnotationWithOverriddenField() {
    val annotationDefaults = AnnotationHelper.createResolvedAnnotationData("OverriddenFieldAnnotation", "DefaultValue")
    val registry = AnnotationRegistry()
    registerDefaults(registry, annotationDefaults)
    val unresolvedAnnotation = AnnotationHelper.createAnnotationData("OverriddenFieldAnnotation", "DefaultValue")
    val resolvedAnnotation = registry.resolveAnnotation(unresolvedAnnotation)
    val expectedAnnotation = AnnotationHelper.createResolvedAnnotationData("OverriddenFieldAnnotation", "DefaultValue")
    assertEquals(expectedAnnotation, resolvedAnnotation)
  }

  @Test
  @Throws(Exception::class)
  fun testResolveAnnotationWithExtraField() {
    val annotationDefaults = AnnotationHelper.createResolvedAnnotationData("ExtraFieldAnnotation", "DefaultValue")
    val registry = AnnotationRegistry()
    registerDefaults(registry, annotationDefaults)
    val unresolvedAnnotation = AnnotationHelper.createAnnotationData("ExtraFieldAnnotation", "extraValue", "ExtraValue")
    val resolvedAnnotation = registry.resolveAnnotation(unresolvedAnnotation)
    val expectedValues = HashMap<String, Any>()
    expectedValues.put("value", "DefaultValue")
    expectedValues.put("extraValue", "ExtraValue")
    val expectedAnnotation = AnnotationHelper.createResolvedAnnotationData("ExtraFieldAnnotation", expectedValues)
    assertEquals(expectedAnnotation, resolvedAnnotation)
  }

  @Test
  @Throws(Exception::class)
  fun testResolveResolvedAnnotationWithEmptyInnerResolvedAnnotation() {
    val innerAnnotation = AnnotationHelper.createResolvedAnnotationData("InnerAnnotation")
    val annotationDefaults = AnnotationHelper.createResolvedAnnotationData("OuterAnnotation", innerAnnotation)
    val registry = AnnotationRegistry()
    registerDefaults(registry, innerAnnotation)
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"))
    assertEquals(annotationDefaults, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
    assertTrue(registry.hasResolvedDefaults(innerAnnotation.type))
    assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveUnresolvedAnnotationWithEmptyInnerResolvedAnnotation() {
    val innerAnnotation = AnnotationHelper.createResolvedAnnotationData("InnerAnnotation")
    val annotationDefaults = AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotation)
    val registry = AnnotationRegistry()
    registerDefaults(registry, innerAnnotation)
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"))
    val expectedAnnotation = AnnotationHelper.createResolvedAnnotationData("OuterAnnotation", innerAnnotation)
    assertEquals(expectedAnnotation, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
    assertTrue(registry.hasResolvedDefaults(innerAnnotation.type))
    assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveUnresolvedAnnotationWithEmptyInnerUnresolvedAnnotation() {
    val innerAnnotation = AnnotationHelper.createAnnotationData("InnerAnnotation")
    val annotationDefaults = AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotation)
    val registry = AnnotationRegistry()
    registerDefaults(registry, innerAnnotation)
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"))
    val resolvedInnerAnnotation = AnnotationHelper.createResolvedAnnotationData("InnerAnnotation")
    val expectedAnnotation = AnnotationHelper.createResolvedAnnotationData("OuterAnnotation", resolvedInnerAnnotation)
    assertEquals(expectedAnnotation, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
    assertTrue(registry.hasResolvedDefaults(innerAnnotation.type))
    assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveUnresolvedAnnotationWithInnerUnresolvableAnnotation() {
    val unresolvableAnnotation = AnnotationHelper.createAnnotationData("UnresolvableAnnotation")
    val annotationDefaults = AnnotationHelper.createAnnotationData("OuterAnnotation", unresolvableAnnotation)
    val registry = AnnotationRegistry()
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"))
    assertEquals(annotationDefaults, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasResolvedDefaults(unresolvableAnnotation.type))
    assertTrue(!registry.hasUnresolvedDefaults(unresolvableAnnotation.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveUnresolvedAnnotationWithNestedInnerUnresolvableAnnotation() {
    val unresolvableAnnotation = AnnotationHelper.createAnnotationData("UnresolvableAnnotation")
    val innerAnnotation = AnnotationHelper.createAnnotationData("InnerAnnotation", unresolvableAnnotation)
    val annotationDefaults = AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotation)
    val registry = AnnotationRegistry()
    registerDefaults(registry, innerAnnotation)
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"))
    assertEquals(annotationDefaults, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
    assertTrue(registry.hasResolvedDefaults(innerAnnotation.type))
    assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.type))
    assertTrue(!registry.hasResolvedDefaults(unresolvableAnnotation.type))
    assertTrue(!registry.hasUnresolvedDefaults(unresolvableAnnotation.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveUnresolvedAnnotationWithNestedInnerAnnotation() {
    val innerInnerAnnotation = AnnotationHelper.createAnnotationData("InnerInnerAnnotation", "InnerInnerDefaultValue")
    val innerAnnotation = AnnotationHelper.createAnnotationData("InnerAnnotation", innerInnerAnnotation)
    val annotationDefaults = AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotation)
    val registry = AnnotationRegistry()
    registerDefaults(registry, innerInnerAnnotation)
    registerDefaults(registry, innerAnnotation)
    registerDefaults(registry, annotationDefaults)

    val unresolvedInnerInnerAnnotation = AnnotationHelper.createAnnotationData("InnerInnerAnnotation",
        "innerInnerStringValue", "InnerInnerStringValue")
    val unresolvedInnerAnnotation = AnnotationHelper.createAnnotationData("InnerAnnotation",
        Pair("value", unresolvedInnerInnerAnnotation),
        Pair("innerStringValue", "InnerStringValue"))
    val unresolvedAnnotation = AnnotationHelper.createAnnotationData("OuterAnnotation",
        Pair("value", unresolvedInnerAnnotation),
        Pair("outerStringValue", "OuterStringValue"))
    val resolvedAnnotation = registry.resolveAnnotation(unresolvedAnnotation)

    val expectedInnerInnerAnnotation = AnnotationHelper.createAnnotationData("InnerInnerAnnotation",
        Pair("value", "InnerInnerDefaultValue"),
        Pair("innerInnerStringValue", "InnerInnerStringValue"))
    val expectedInnerAnnotation = AnnotationHelper.createAnnotationData("InnerAnnotation",
        Pair("value", expectedInnerInnerAnnotation),
        Pair("innerStringValue", "InnerStringValue"))
    val expectedAnnotation = AnnotationHelper.createAnnotationData("OuterAnnotation",
        Pair("value", expectedInnerAnnotation),
        Pair("outerStringValue", "OuterStringValue"))
    assertEquals(expectedAnnotation, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
    assertTrue(registry.hasResolvedDefaults(innerAnnotation.type))
    assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.type))
    assertTrue(registry.hasResolvedDefaults(innerInnerAnnotation.type))
    assertTrue(!registry.hasUnresolvedDefaults(innerInnerAnnotation.type))
  }

  @Test
  @Throws(Exception::class)
  fun testResolveUnresolvedAnnotationWithNestedUnresolvedAnnotationArray() {
    val innerAnnotation = AnnotationHelper.createAnnotationData("InnerAnnotation", "DefaultValue")
    val innerAnnotations = listOf(
        AnnotationHelper.createAnnotationData("InnerAnnotation", "ExplicitValue"),
        AnnotationHelper.createAnnotationData("InnerAnnotation", "stringValue", "StringValue"))
    val annotationDefaults = AnnotationHelper.createAnnotationData("OuterAnnotation", innerAnnotations)
    val registry = AnnotationRegistry()

    registerDefaults(registry, innerAnnotation)
    registerDefaults(registry, annotationDefaults)
    val resolvedAnnotation = registry.resolveAnnotation(AnnotationHelper.createAnnotationData("OuterAnnotation"))

    val expectedInnerAnnotations = listOf(
        AnnotationHelper.createResolvedAnnotationData("InnerAnnotation", "ExplicitValue"),
        AnnotationHelper.createResolvedAnnotationData("InnerAnnotation",
            Pair("stringValue", "StringValue"),
            Pair("value", "DefaultValue")))
    val expectedAnnotation = AnnotationHelper.createResolvedAnnotationData("OuterAnnotation", expectedInnerAnnotations)

    assertEquals(expectedAnnotation, resolvedAnnotation)
    assertTrue(registry.hasResolvedDefaults(annotationDefaults.type))
    assertTrue(!registry.hasUnresolvedDefaults(annotationDefaults.type))
    assertTrue(registry.hasResolvedDefaults(innerAnnotation.type))
    assertTrue(!registry.hasUnresolvedDefaults(innerAnnotation.type))
  }

  private fun registerDefaults(registry: AnnotationRegistry, defaults: AnnotationData) {
    val descriptor = AnnotationDescriptor(defaults.type, emptyMap<String, Type>())
    registry.addAnnotationDefaults(descriptor, defaults)
  }
}
