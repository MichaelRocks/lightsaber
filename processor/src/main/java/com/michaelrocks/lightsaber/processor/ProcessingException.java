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

package com.michaelrocks.lightsaber.processor;

import java.io.File;
import java.io.IOException;

public class ProcessingException extends IOException {
    private final File jarFile;

    public ProcessingException(final String message) {
        this(null, message);
    }

    public ProcessingException(final File jarFile, final String message) {
        super(message);
        this.jarFile = jarFile;
    }

    public ProcessingException(final Throwable cause) {
        this((File) null, cause);
    }

    public ProcessingException(final File jarFile, final Throwable cause) {
        super(cause);
        this.jarFile = jarFile;
    }

    public ProcessingException(final String message, final Throwable cause) {
        this(null, message, cause);
    }

    public ProcessingException(final File jarFile, final String message, final Throwable cause) {
        super(message, cause);
        this.jarFile = jarFile;
    }

    public File getJarFile() {
        return jarFile;
    }

    @Override
    public String getMessage() {
        return jarFile == null ? super.getMessage() : "[" + jarFile.getAbsolutePath() + "] " + super.getMessage();
    }
}
