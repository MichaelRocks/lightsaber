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

package io.michaelrocks.lightsaber.processor.annotations.proxy

import java.util.*

class ArrayClassLoader : ClassLoader {
  private val loadedClasses = HashSet<String>()
  private val pendingClasses = HashMap<String, ByteArray>()

  constructor()

  constructor(parentClassLoader: ClassLoader) : super(parentClassLoader)

  fun addClass(name: String, bytes: ByteArray) {
    check(!loadedClasses.contains(name))
    pendingClasses.put(name, bytes)
  }

  fun hasClass(name: String): Boolean = loadedClasses.contains(name) || pendingClasses.containsKey(name)

  @Throws(ClassNotFoundException::class)
  override fun findClass(name: String): Class<*> {
    val loadedClass: Class<*>?
    val bytes = pendingClasses.remove(name)
    if (bytes == null) {
      loadedClass = findLoadedClass(name)
    } else {
      loadedClass = defineClass(name, bytes, 0, bytes.size)
      loadedClasses.add(name)
    }

    if (loadedClass != null) {
      return loadedClass
    }

    throw ClassNotFoundException(name)
  }
}
