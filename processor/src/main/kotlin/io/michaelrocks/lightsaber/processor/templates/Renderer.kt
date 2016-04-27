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

package io.michaelrocks.lightsaber.processor.templates

import java.util.*

interface Renderer {
  fun substitute(name: String, value: String): Renderer
  fun render(): String
}

internal class RendererImpl(private val lines: List<Line>) : Renderer {
  private val parameters = HashMap<String, String>()

  override fun substitute(name: String, value: String) = apply {
    parameters.put(name, value.trimEnd('\n'))
  }

  override fun render(): String {
    return buildString {
      lines.forEach { line ->
        when (line) {
          is Line.Text -> appendln(line.text)
          is Line.Parameter -> appendln(line)
        }
      }
    }
  }

  private fun StringBuilder.appendln(parameter: Line.Parameter) {
    val text = parameters[parameter.name] ?: error("Value for parameter ${parameter.name} not provided")
    text.splitToSequence('\n').forEach { string ->
      if (!string.isBlank()) {
        append(parameter.prefix)
      }
      appendln(string)
    }
  }
}
