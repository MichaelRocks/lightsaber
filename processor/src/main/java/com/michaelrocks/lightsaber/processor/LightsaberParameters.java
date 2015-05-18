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

import com.beust.jcommander.Parameter;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class LightsaberParameters {
    @Parameter(names = "--jar", description = "Jar file to process")
    public String jar;

    @Parameter(names = "--classes", description = "Classes directory to process")
    public String classes;

    @Parameter(names = "--output", description = "Output jar file or classes directory")
    public String output;

    @Parameter(names = { "-v", "--verbose" }, description = "Use verbose output")
    public boolean verbose = false;

    @Parameter(names = "--stacktrace", description = "Print stack traces")
    public boolean printStacktrace = false;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("jar", jar)
                .append("classes", classes)
                .append("output", output)
                .append("verbose", verbose)
                .append("printStacktrace", printStacktrace)
                .toString();
    }
}
