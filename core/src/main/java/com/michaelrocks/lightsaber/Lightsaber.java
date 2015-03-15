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

package com.michaelrocks.lightsaber;

import com.michaelrocks.lightsaber.internal.InternalModule;
import com.michaelrocks.lightsaber.internal.LightsaberInjector;

public class Lightsaber {
    public static Injector createInjector(final Module... modules) {
        final LightsaberInjector injector = new LightsaberInjector();
        for (final Module module : modules) {
            if (module == null) {
                throw new NullPointerException("Trying to create injector with a null module");
            }

            if (!(module instanceof InternalModule)) {
                throw new ConfigurationException("Module " + module + " hasn't been processed");
            }

            ((InternalModule) module).configureInjector(injector);
        }
        return injector;
    }
}
