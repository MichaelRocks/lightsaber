/*
 * Copyright 2018 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.access

import io.michaelrocks.lightsaber.Key
import io.michaelrocks.lightsaber.Lightsaber
import org.junit.Assert.assertSame
import org.junit.Test

class AccessTest {
  @Test
  fun testInjectionAccess() {
    val injector = Lightsaber.get().createInjector(AccessComponent())
    val target = injector.getInstance(Key.of<InternalDependency>(InternalDependency::class.java))
    target.action()
  }

  @Test
  fun testInjectionAccessWithQualifier() {
    val injector = Lightsaber.get().createInjector(AccessComponent())
    val qualifier = AnnotationHolder::class.java.getAnnotation(InternalQualifier::class.java)
    val target = injector.getInstance(Key.of<InternalDependency>(InternalDependency::class.java, qualifier))
    target.action()
  }

  @Test
  fun testInjectionAccessWithSingletonScope() {
    val injector = Lightsaber.get().createInjector(AccessComponent())
    val qualifier = AnnotationHolder::class.java.getAnnotation(SingletonQualifier::class.java)
    val target1 = injector.getInstance(Key.of<InternalDependency>(InternalDependency::class.java, qualifier))
    val target2 = injector.getInstance(Key.of<InternalDependency>(InternalDependency::class.java, qualifier))
    target1.action()
    target2.action()
    assertSame(target1, target2)
  }

  @InternalQualifier
  @SingletonQualifier
  private class AnnotationHolder
}
