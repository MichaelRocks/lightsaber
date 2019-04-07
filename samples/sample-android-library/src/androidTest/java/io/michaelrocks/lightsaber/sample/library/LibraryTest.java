/*
 * Copyright 2019 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.sample.library;

import android.content.Context;
import android.os.Looper;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.annotation.UiThreadTest;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class LibraryTest {
  @Test
  public void checkApplicationContext() {
    final Context context = ApplicationProvider.getApplicationContext();
    assertEquals("io.michaelrocks.lightsaber.sample.library.test", context.getPackageName());
  }

  @Test
  @UiThreadTest
  public void checkUiThread() {
    assertSame(Looper.getMainLooper(), Looper.myLooper());
  }
}
