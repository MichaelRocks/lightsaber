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

package io.michaelrocks.lightsaber.sample

import io.michaelrocks.lightsaber.Lightsaber
import javax.inject.Inject
import javax.inject.Provider

public class LightsaberSample {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            LightsaberSample().run()
        }
    }

    @Inject
    private lateinit var wookiee: Wookiee
    @Inject
    private lateinit var wookieeProvider: Provider<Wookiee>

    @Inject
    private lateinit var droid: Droid
    @Inject
    private lateinit var droidProvider: Provider<Droid>

    @Inject
    private lateinit var darthVader: DarthVader

    @Inject
    private lateinit var planet: Planet

    private fun run() {
        val injector = Lightsaber.getInstance().createInjector(LightsaberModule())
        injector.injectMembers(this)
        System.out.println("After injection")
        System.out.println("Wookiee: $wookiee from ${wookiee.planet}")
        val anotherWookiee = wookieeProvider.get()
        System.out.println("Another wookiee: $anotherWookiee from ${anotherWookiee.planet}")
        System.out.println("Droid: $droid")
        val anotherDroid = droidProvider.get()
        System.out.println("Another droid: $anotherDroid")
        System.out.println("Darth Vader: " + darthVader)
        System.out.println("Planet: " + planet)
        wookiee.roar()
        droid.repair()
    }
}
