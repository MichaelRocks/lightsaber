/*
 * Copyright 2019 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber

import org.junit.Assert.assertEquals
import org.junit.Test
import javax.inject.Named
import javax.inject.Provider
import javax.inject.Singleton

class ProviderInterceptorInjectionTest {
  @Test
  fun testInjectionInterceptor() {
    val strings = listOf("StringInstanceClass", "StringInstanceKey", "StringProviderClass", "StringProviderKey")
    var stringIndex = 0
    val stringProvider = Provider<String> { strings[stringIndex++] }

    val objects = listOf("ObjectInstanceClass", "ObjectInstanceKey", "ObjectProviderClass", "ObjectProviderKey")
    var objectIndex = 0
    val objectProvider = Provider<Any> { objects[objectIndex++] }

    val stringKey = Key.of(String::class.java)
    val objectKey = Key.of(Any::class.java)

    val interceptor = object : ProviderInterceptor {
      override fun intercept(chain: ProviderInterceptor.Chain, key: Key<*>): Provider<*> {
        return when (key) {
          stringKey -> stringProvider
          objectKey -> objectProvider
          else -> chain.proceed(key)
        }
      }
    }

    val lightsaber = Lightsaber.Builder().addProviderInterceptor(interceptor).build()
    val parentComponent = ParentComponent()
    val childComponent = ChildComponent()

    val injector = lightsaber.createInjector(parentComponent)
    val childInjector = injector.createChildInjector(childComponent)

    assertEquals("StringInstanceClass", childInjector.getInstance(String::class.java))
    assertEquals("StringInstanceKey", childInjector.getInstance(Key.of(String::class.java)))
    assertEquals("StringProviderClass", childInjector.getProvider(String::class.java).get())
    assertEquals("StringProviderKey", childInjector.getProvider(Key.of(String::class.java)).get())

    assertEquals("ObjectInstanceClass", childInjector.getInstance(Any::class.java))
    assertEquals("ObjectInstanceKey", childInjector.getInstance(Key.of(Any::class.java)))
    assertEquals("ObjectProviderClass", childInjector.getProvider(Any::class.java).get())
    assertEquals("ObjectProviderKey", childInjector.getProvider(Key.of(Any::class.java)).get())

    val annotation = ChildModule::class.java.getDeclaredMethod("provideNamedString").getAnnotation(Named::class.java)
    assertEquals("Child String", childInjector.getInstance(Key.of(String::class.java, annotation)))
  }

  @Module
  private class ParentModule {

    @Provide
    @Singleton
    fun provideString(): String = StringBuilder("Parent String").toString()
  }

  @Module
  private class ChildModule {

    @Provide
    @Singleton
    @Named("Child String")
    fun provideNamedString(): String = StringBuilder("Child String").toString()
  }

  @Component
  private class ParentComponent {

    @Import
    private fun importParentModule(): ParentModule = ParentModule()
  }

  @Component(parent = ParentComponent::class)
  private class ChildComponent {

    @Import
    private fun importChildModule(): ChildModule = ChildModule()
  }
}
