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

package com.michaelrocks.lightsaber.sample

import com.michaelrocks.lightsaber.Module
import com.michaelrocks.lightsaber.Provides

public class LightsaberModule : Module {
    Provides
    public fun provideWookie(planet: Planet): Wookiee {
        return Chewbacca(planet)
    }

    Provides
    public fun provideDroid(r2d2: R2D2): Droid {
        return r2d2
    }

    Provides
    public fun providePlanet(): Planet {
        return Kashyyyk()
    }
}
