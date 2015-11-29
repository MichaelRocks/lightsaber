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

package io.michaelrocks.lightsaber.processor.io

import java.io.IOException

open class ClassFileVisitor(protected var classFileVisitor: ClassFileVisitor?) {
  @Throws(IOException::class)
  open fun visitClassFile(path: String, classData: ByteArray) {
    classFileVisitor?.visitClassFile(path, classData)
  }

  @Throws(IOException::class)
  open fun visitOtherFile(path: String, fileData: ByteArray) {
    classFileVisitor?.visitOtherFile(path, fileData)
  }

  @Throws(IOException::class)
  open fun visitDirectory(path: String) {
    classFileVisitor?.visitDirectory(path)
  }

  @Throws(IOException::class)
  fun visitEnd() {
    classFileVisitor?.visitEnd()
  }
}
