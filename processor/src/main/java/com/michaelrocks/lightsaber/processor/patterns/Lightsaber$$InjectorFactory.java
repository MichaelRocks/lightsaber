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

package com.michaelrocks.lightsaber.processor.patterns;

import com.michaelrocks.lightsaber.Injector;
import com.michaelrocks.lightsaber.Lightsaber;
import com.michaelrocks.lightsaber.Module;

public class Lightsaber$$InjectorFactory {
    public static Injector createInjector(final Module... modules) {
        final Module[] newModules;
        if (modules == null || modules.length == 0) {
            newModules = new Module[1];
        } else {
            newModules = new Module[modules.length + 1];
            System.arraycopy(modules, 0, newModules, 1, modules.length);
        }
        newModules[0] = new Lightsaber$$GlobalModule();
        return Lightsaber.createInjector(newModules);
    }
}
