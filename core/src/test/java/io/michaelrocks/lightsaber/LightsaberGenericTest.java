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

package io.michaelrocks.lightsaber;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Provider;

import io.michaelrocks.lightsaber.internal.InjectorConfigurator;
import io.michaelrocks.lightsaber.internal.LightsaberInjector;
import io.michaelrocks.lightsaber.internal.ParameterizedTypeImpl;


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class LightsaberGenericTest {
  @Test
  public void testCreateInjector() throws Exception {
    final Lightsaber lightsaber = new Lightsaber.Builder().build();
    final InjectorConfigurator parentComponent = createParentComponent();

    final Injector injector = lightsaber.createInjector(parentComponent);

    verify(parentComponent).configureInjector((LightsaberInjector) injector);
    verifyNoMoreInteractions(parentComponent);
    assertEquals(Collections.singletonList("Parent List"), injector.getInstance(getJvmStringListType()));
    assertEquals(Collections.singletonList("Parent List"), injector.getInstance(Key.of(getJvmStringListType())));
    assertEquals(Collections.singletonList("Parent List"), injector.getInstance(getArtificialStringListType()));
    assertEquals(Collections.singletonList("Parent List"), injector.getInstance(Key.of(getArtificialStringListType())));
  }

  private static InjectorConfigurator createParentComponent() {
    final InjectorConfigurator configurator = mock(InjectorConfigurator.class, RETURNS_DEEP_STUBS);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final LightsaberInjector injector = (LightsaberInjector) invocation.getArguments()[0];
        injector.registerProvider(getJvmStringListType(), new Provider<List<String>>() {
          @Nonnull
          @Override
          public List<String> get() {
            return Collections.singletonList("Parent List");
          }
        });
        return null;
      }
    })
        .when(configurator).configureInjector(any(LightsaberInjector.class));
    return configurator;
  }


  private static Type getJvmStringListType() {
    return new TypeReference<List<String>>() {}.getType();
  }

  private static Type getArtificialStringListType() {
    return new ParameterizedTypeImpl(null, List.class, String.class);
  }
}
