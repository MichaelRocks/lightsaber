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

import io.michaelrocks.lightsaber.mockito.any
import io.michaelrocks.lightsaber.mockito.given
import io.michaelrocks.lightsaber.mockito.mock
import io.michaelrocks.lightsaber.mockito.same
import io.michaelrocks.lightsaber.processor.files.FileRegistry
import org.junit.Assert.assertSame
import org.junit.Test

class AnnotationRegistryTest {
  @Test(expected = IllegalArgumentException::class)
  fun testFindNonExistentAnnotation() {
    val annotationType = AnnotationHelper.getAnnotationType("NonExistentAnnotation")
    val fileRegistry = mock<FileRegistry>()
    given(fileRegistry.readClass(annotationType)).thenThrow(IllegalArgumentException())
    val annotationRegistry = AnnotationRegistryImpl(fileRegistry)
    annotationRegistry.findAnnotation(annotationType)
  }

  @Test(expected = IllegalArgumentException::class)
  fun testFindNonExistentAnnotationDefaults() {
    val annotationType = AnnotationHelper.getAnnotationType("NonExistentAnnotation")
    val fileRegistry = mock<FileRegistry>()
    given(fileRegistry.readClass(annotationType)).thenThrow(IllegalArgumentException())
    val annotationRegistry = AnnotationRegistryImpl(fileRegistry)
    annotationRegistry.findAnnotationDefaults(annotationType)
  }

  @Test
  fun testFindAnnotation() {
    val descriptor = AnnotationHelper.createAnnotationDescriptor("Annotation")
    val defaults = AnnotationHelper.createAnnotationData("Annotation")
    val annotation = AnnotationReader.Annotation(descriptor, defaults)

    val classData = ByteArray(0)
    val fileRegistry = mock<FileRegistry>()
    given(fileRegistry.readClass(defaults.type)).thenReturn(classData)

    val annotationReader = mock<AnnotationReader>()
    given(annotationReader.readAnnotation(same(classData), any()))
        .thenReturn(annotation)
        .thenThrow(IllegalStateException())

    val registry = AnnotationRegistryImpl(fileRegistry, annotationReader)
    val actualDescriptor = registry.findAnnotation(descriptor.type)
    val actualDefaults = registry.findAnnotationDefaults(defaults.type)

    assertSame(descriptor, actualDescriptor)
    assertSame(defaults, actualDefaults)
  }

  @Test
  fun testFindNestedAnnotation() {
    val nestedDescriptor = AnnotationHelper.createAnnotationDescriptor("NestedAnnotation")
    val nestedDefaults = AnnotationHelper.createAnnotationData("NestedAnnotation")
    val nestedAnnotation = AnnotationReader.Annotation(nestedDescriptor, nestedDefaults)

    val descriptor = AnnotationHelper.createAnnotationDescriptor("Annotation", nestedAnnotation.descriptor.type)
    val defaults = AnnotationHelper.createAnnotationData("Annotation", nestedAnnotation.defaults)
    val annotation = AnnotationReader.Annotation(descriptor, defaults)

    val nestedClassData = ByteArray(0)
    val classData = ByteArray(0)
    val fileRegistry = mock<FileRegistry>()
    given(fileRegistry.readClass(nestedDefaults.type)).thenReturn(nestedClassData)
    given(fileRegistry.readClass(defaults.type)).thenReturn(classData)

    val annotationReader = mock<AnnotationReader>()
    given(annotationReader.readAnnotation(same(nestedClassData), any()))
        .thenReturn(nestedAnnotation)
        .thenThrow(IllegalStateException())
    given(annotationReader.readAnnotation(same(classData), any()))
        .thenReturn(annotation)
        .thenThrow(IllegalStateException())

    val registry = AnnotationRegistryImpl(fileRegistry, annotationReader)
    val actualNestedDescriptor = registry.findAnnotation(nestedDescriptor.type)
    val actualNestedDefaults = registry.findAnnotationDefaults(nestedDefaults.type)
    val actualDescriptor = registry.findAnnotation(descriptor.type)
    val actualDefaults = registry.findAnnotationDefaults(defaults.type)

    assertSame(nestedDescriptor, actualNestedDescriptor)
    assertSame(nestedDefaults, actualNestedDefaults)
    assertSame(descriptor, actualDescriptor)
    assertSame(defaults, actualDefaults)
    assertSame(defaults.values["value"], actualDefaults.values["value"])
  }
}
