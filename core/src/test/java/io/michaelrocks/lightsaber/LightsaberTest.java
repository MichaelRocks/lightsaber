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
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.annotation.Nonnull;
import javax.inject.Named;
import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class LightsaberTest {
  private Lightsaber.Configurator configurator;

  @Before
  public void createConfigurator() {
    configurator = mock(Lightsaber.Configurator.class, RETURNS_DEEP_STUBS);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final LightsaberInjector injector = (LightsaberInjector) invocation.getArguments()[0];
        injector.registerProvider(String.class, new AbstractInjectingProvider<String>(injector) {
          @Nonnull
          @Override
          public String getWithInjector(@Nonnull final Injector injector) {
            return "Parent String";
          }
        });
        return null;
      }
    })
        .when(configurator).configureInjector(any(LightsaberInjector.class), isA(ParentModule.class));
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final LightsaberInjector injector = (LightsaberInjector) invocation.getArguments()[0];
        injector.registerProvider(Key.of(Object.class), new AbstractInjectingProvider<Object>(injector) {
          @Nonnull
          @Override
          public Object getWithInjector(@Nonnull final Injector injector) {
            return "Child Object";
          }
        });
        return null;
      }
    })
        .when(configurator).configureInjector(any(LightsaberInjector.class), isA(ChildModule.class));
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final LightsaberInjector injector = (LightsaberInjector) invocation.getArguments()[0];
        injector.registerProvider(Key.of(String.class, new NamedProxy("Annotated")),
            new AbstractInjectingProvider<String>(injector) {
              @Nonnull
              @Override
              public String getWithInjector(@Nonnull final Injector injector) {
                return "Child Annotated String";
              }
            });
        return null;
      }
    })
        .when(configurator).configureInjector(any(LightsaberInjector.class), isA(ChildAnnotatedModule.class));
  }

  @Test
  public void testCreateInjector() throws Exception {
    final Lightsaber lightsaber = new Lightsaber(configurator);
    final ParentModule parentModule = new ParentModule();

    final Injector injector = lightsaber.createInjector(parentModule);

    verify(configurator).configureInjector((LightsaberInjector) injector, null);
    verify(configurator).configureInjector((LightsaberInjector) injector, parentModule);
    verifyNoMoreInteractions(configurator);
    assertSame(injector, injector.getInstance(Key.of(Injector.class)));
    assertEquals("Parent String", injector.getInstance(String.class));
    assertEquals("Parent String", injector.getInstance(Key.of(String.class)));
  }

  @Test
  public void testCreateChildInjector() throws Exception {
    final Lightsaber lightsaber = new Lightsaber(configurator);
    final ParentModule parentModule = new ParentModule();
    final ChildModule childModule = new ChildModule();

    final Injector injector = lightsaber.createInjector(parentModule);
    final Injector childInjector = lightsaber.createChildInjector(injector, childModule);

    verify(configurator).configureInjector((LightsaberInjector) injector, null);
    verify(configurator).configureInjector((LightsaberInjector) injector, parentModule);
    verify(configurator).configureInjector((LightsaberInjector) childInjector, childModule);
    verifyNoMoreInteractions(configurator);
    assertSame(injector, injector.getInstance(Key.of(Injector.class)));
    assertSame(childInjector, childInjector.getInstance(Key.of(Injector.class)));
    assertEquals("Parent String", childInjector.getInstance(String.class));
    assertEquals("Parent String", childInjector.getInstance(Key.of(String.class)));
    assertEquals("Child Object", childInjector.getInstance(Object.class));
    assertEquals("Child Object", childInjector.getInstance(Key.of(Object.class)));
  }

  @Test
  public void testCreateChildInjectorWithAnnotation() throws Exception {
    final Lightsaber lightsaber = new Lightsaber(configurator);
    final ParentModule parentModule = new ParentModule();
    final ChildAnnotatedModule childAnnotatedModule = new ChildAnnotatedModule();

    final Injector injector = lightsaber.createInjector(parentModule);
    final Injector childInjector =
        lightsaber.createChildInjector(injector, childAnnotatedModule);

    verify(configurator).configureInjector((LightsaberInjector) injector, null);
    verify(configurator).configureInjector((LightsaberInjector) injector, parentModule);
    verify(configurator).configureInjector((LightsaberInjector) childInjector, childAnnotatedModule);
    verifyNoMoreInteractions(configurator);
    final Named annotation = new NamedProxy("Annotated");
    assertSame(injector, injector.getInstance(Key.of(Injector.class)));
    assertSame(childInjector, childInjector.getInstance(Key.of(Injector.class)));
    assertEquals("Parent String", childInjector.getInstance(String.class));
    assertEquals("Parent String", childInjector.getInstance(Key.of(String.class)));
    assertEquals("Child Annotated String", childInjector.getInstance(Key.of(String.class, annotation)));
  }

  private static class ParentModule {
  }

  private static class ChildModule {
  }

  private static class ChildAnnotatedModule {
  }

  @SuppressWarnings("ClassExplicitlyAnnotation")
  private static class NamedProxy implements Named {
    @Nonnull
    private final String value;

    NamedProxy(@Nonnull final String value) {
      this.value = value;
    }

    @Override
    public String value() {
      return value;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return Named.class;
    }

    @Override
    public boolean equals(final Object object) {
      if (this == object) {
        return true;
      }

      if (!(object instanceof Named)) {
        return false;
      }

      final Named that = (Named) object;
      return value.equals(that.value());
    }

    @Override
    public int hashCode() {
      // Hash code for annotation is the sum of 127 * fieldName.hashCode() ^ fieldValue.hashCode().
      return 127 * "value".hashCode() ^ value.hashCode();
    }
  }
}
