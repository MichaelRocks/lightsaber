/*
 * Copyright 2017 Michael Rozumyanskiy
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

import io.michaelrocks.lightsaber.internal.AbstractInjectingProvider;
import io.michaelrocks.lightsaber.internal.ParameterizedTypeImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Nonnull;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class LightsaberGenericTest {
  private Lightsaber.Configurator configurator;

  @Before
  public void createConfigurator() {
    configurator = mock(Lightsaber.Configurator.class, RETURNS_DEEP_STUBS);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final LightsaberInjector injector = (LightsaberInjector) invocation.getArguments()[0];
        injector.registerProvider(getJvmStringListType(), new AbstractInjectingProvider<List<String>>(injector) {
              @Nonnull
              @Override
              public List<String> getWithInjector(@Nonnull final Injector injector) {
                return Collections.singletonList("Parent List");
              }
            });
        return null;
      }
    })
        .when(configurator).configureInjector(any(LightsaberInjector.class), isA(ParentModule.class));
  }

  @Test
  public void testCreateInjector() throws Exception {
    final Lightsaber lightsaber = new Lightsaber(configurator);
    final ParentModule parentModule = new ParentModule();

    final Injector injector = lightsaber.createInjector(parentModule);

    verify(configurator).configureInjector((LightsaberInjector) injector, null);
    verify(configurator).configureInjector((LightsaberInjector) injector, parentModule);
    verifyNoMoreInteractions(configurator);
    assertEquals(Collections.singletonList("Parent List"), injector.getInstance(getJvmStringListType()));
    assertEquals(Collections.singletonList("Parent List"), injector.getInstance(Key.of(getJvmStringListType())));
    assertEquals(Collections.singletonList("Parent List"), injector.getInstance(getArtificialStringListType()));
    assertEquals(Collections.singletonList("Parent List"), injector.getInstance(Key.of(getArtificialStringListType())));
  }

  private static Type getJvmStringListType() {
    return new TypeReference<List<String>>() {}.getType();
  }

  private static Type getArtificialStringListType() {
    return new ParameterizedTypeImpl(null, List.class, String.class);
  }

  private static class ParentModule {
  }
}
