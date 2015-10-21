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

package io.michaelrocks.lightsaber.access;

import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.Key;
import io.michaelrocks.lightsaber.Lightsaber;
import org.junit.Test;

@InternalQualifier
public class AccessTest {
    @Test
    public void testInjectionAccess() {
        final AccessModule module = new AccessModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final InternalDependency target = injector.getInstance(Key.of(InternalDependency.class));
        target.action();
    }

    @Test
    public void testInjectionAccessWithQualifier() {
        final AccessModule module = new AccessModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final InternalQualifier qualifier = getClass().getAnnotation(InternalQualifier.class);
        final InternalDependency target = injector.getInstance(Key.of(InternalDependency.class, qualifier));
        target.action();
    }
}
