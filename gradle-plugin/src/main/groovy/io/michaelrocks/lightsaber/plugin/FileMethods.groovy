/*
 * Copyright 2016 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.plugin

import groovy.transform.CompileStatic

@CompileStatic
public class FileMethods {
  public static String relativize(final File parent, final File child) {
    final URI relativeUri = parent.toURI().relativize(child.toURI())
    return relativeUri.toString()
  }

  public static File resolve(final File parent, final String path) {
    return new File(parent, path)
  }

  public static void createParentDirectories(final File file) {
    file.parentFile?.mkdirs()
  }

  public static void copyTo(final File source, final File destination, final boolean replaceExisting = false) {
    if (!replaceExisting && destination.exists()) {
      return
    }

    source.withDataInputStream { final sourceStream ->
      destination.withDataOutputStream { final destinationStream ->
        destinationStream << sourceStream
      }
    }

    destination.lastModified = source.lastModified()
  }

  public static void deleteDirectoryIfEmpty(final File directory) {
    if (!directory.isDirectory()) {
      return
    }

    directory.delete()
  }
}
