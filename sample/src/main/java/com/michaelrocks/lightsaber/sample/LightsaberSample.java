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

package com.michaelrocks.lightsaber.sample;

import com.michaelrocks.lightsaber.Injector;
import com.michaelrocks.lightsaber.Lightsaber;

import javax.inject.Inject;
import java.io.File;
import java.net.URISyntaxException;

public class LightsaberSample {
    @Inject
    Wookiee wookiee;

    public static void main(final String[] args) throws URISyntaxException {
        System.out.println(
                new File(LightsaberSample.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
        new LightsaberSample().run();
    }

    private void run() {
        System.out.println("Before injection: " + wookiee);
        final Injector injector = Lightsaber.createInjector(new LightsaberModule());
        injector.injectMembers(this);
        System.out.println("After injection: " + wookiee);
        wookiee.roar();
    }
}
