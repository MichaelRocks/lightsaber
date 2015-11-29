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

package io.michaelrocks.lightsaber.sample;

import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.Lightsaber;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.File;
import java.net.URISyntaxException;

public class LightsaberSample {
  @Inject
  private final Wookiee wookiee = null;
  @Inject
  private final Provider<Wookiee> wookieeProvider = null;

  @Inject
  private final Droid droid = null;
  @Inject
  private final Provider<Droid> droidProvider = null;

  @Inject
  private final DarthVader darthVader = null;

  @Inject
  private final Planet planet = null;

  public static void main(final String[] args) throws URISyntaxException {
    System.out.println(
        new File(LightsaberSample.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
    new LightsaberSample().run();
  }

  private void run() {
    System.out.println("Before injection");
    System.out.println("Wookiee: " + wookiee);
    System.out.println("Droid: " + droid);
    System.out.println("Darth Vader: " + darthVader);
    System.out.println("Planet: " + planet);
    final Injector injector = Lightsaber.get().createInjector(new LightsaberModule());
    injector.injectMembers(this);
    System.out.println("After injection");
    System.out.println("Wookiee: " + wookiee + " from " + wookiee.getPlanet());
    final Wookiee anotherWookiee = wookieeProvider.get();
    System.out.println("Another wookiee: " + anotherWookiee + " from " + anotherWookiee.getPlanet());
    System.out.println("Droid: " + droid);
    final Droid anotherDroid = droidProvider.get();
    System.out.println("Another droid: " + anotherDroid);
    System.out.println("Darth Vader: " + darthVader);
    System.out.println("Planet: " + planet);
    wookiee.roar();
    droid.repair();
  }
}
