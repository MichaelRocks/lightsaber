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

import io.michaelrocks.lightsaber.Module;
import io.michaelrocks.lightsaber.Provides;

import javax.inject.Provider;
import javax.inject.Singleton;

@Module
class LightsaberModule {
  @Provides
  private final DarthVader darthVader = DarthVader.INSTANCE;

  @Provides
  private Wookiee provideWookiee(final Chewbacca chewbacca) {
    return chewbacca;
  }

  @Provides
  private Droid provideDroid(final R2D2 r2d2) {
    return r2d2;
  }

  @Provides
  @Singleton
  private Planet providePlanet(final Provider<Kashyyyk> kashyyykProvider) {
    return kashyyykProvider.get();
  }
}
