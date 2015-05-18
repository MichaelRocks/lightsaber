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

package io.michaelrocks.lightsaber.processor;

import java.io.IOException;

public class ProcessingException extends IOException {
    private final String path;

    public ProcessingException(final String message) {
        this(message, (String) null);
    }

    public ProcessingException(final String message, final String path) {
        super(message);
        this.path = path;
    }

    public ProcessingException(final Throwable cause) {
        this(cause, null);
    }

    public ProcessingException(final Throwable cause, final String path) {
        super(cause);
        this.path = path;
    }

    public ProcessingException(final String message, final Throwable cause) {
        this(message, cause, null);
    }

    public ProcessingException(final String message, final Throwable cause, final String path) {
        super(message, cause);
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String getMessage() {
        return path == null ? super.getMessage() : "[" + path + "] " + super.getMessage();
    }
}
