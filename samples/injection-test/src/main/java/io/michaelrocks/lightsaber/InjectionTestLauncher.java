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

package io.michaelrocks.lightsaber;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class InjectionTestLauncher {
    public static void main(final String[] args) {
        final Result result = JUnitCore.runClasses(PrimitiveTest.class);
        if (!result.wasSuccessful()) {
            final StringBuilder builder = new StringBuilder();
            for (final Failure failure : result.getFailures()) {
                builder.append(failure).append('\n');
            }
            throw new AssertionError(builder.toString());
        }
    }
}
