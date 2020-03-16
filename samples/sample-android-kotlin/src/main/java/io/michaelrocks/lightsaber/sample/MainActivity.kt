/*
 * Copyright 2020 Michael Rozumyanskiy
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

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import io.michaelrocks.lightsaber.Lightsaber
import io.michaelrocks.lightsaber.kotlin.R
import io.michaelrocks.lightsaber.sample.library.Droid
import io.michaelrocks.lightsaber.sample.library.Planet
import javax.inject.Inject
import javax.inject.Provider

class MainActivity : Activity() {
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

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    val afterInjectionTextView = findViewById<TextView>(R.id.afterInjectionTextView)

    val injector = Lightsaber.Builder().build().createInjector(LightsaberComponent())
    injector.injectMembers(this)

    print(afterInjectionTextView, "Wookiee: $wookiee from ${wookiee.planet}")
    val anotherWookiee = wookieeProvider.get()
    print(afterInjectionTextView, "Another wookiee: $anotherWookiee from ${anotherWookiee.planet}")
    print(afterInjectionTextView, "Droid: $droid")
    val anotherDroid = droidProvider.get()
    print(afterInjectionTextView, "Another droid: $anotherDroid")
    print(afterInjectionTextView, "Darth Vader: $darthVader")
    print(afterInjectionTextView, "Planet: $planet")
  }

  private fun print(textView: TextView, message: CharSequence) {
    textView.append("\n")
    textView.append(message)
  }
}
