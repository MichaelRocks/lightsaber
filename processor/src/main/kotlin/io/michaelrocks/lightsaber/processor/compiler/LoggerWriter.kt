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

package io.michaelrocks.lightsaber.processor.compiler

import io.michaelrocks.lightsaber.processor.logging.getLogger
import org.slf4j.Logger
import java.io.Writer

internal fun <T : Any> T.newLoggerWriter(): Writer = LoggerWriter(javaClass)

internal class LoggerWriter : Writer {
  private val logger: Logger

  constructor(name: String) : super() {
    this.logger = getLogger(name)
  }

  constructor(type: Class<*>) : super() {
    this.logger = getLogger(type)
  }

  override fun write(chars: CharArray, offset: Int, length: Int) {
    logger.error(String(chars, offset, length))
  }

  override fun flush() {
    // Do nothing.
  }

  override fun close() {
    // Do nothing.
  }
}
