/*
 * Copyright 2018 Michael Rozumyanskiy
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

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Annotation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

public class LightsaberTest {
  private final Lightsaber.Configurator configurator = mock(Lightsaber.Configurator.class);

  @Test
  public void testCreateInjector() throws Exception {
    final Lightsaber lightsaber = new Lightsaber(configurator);
    final InjectorConfigurator parentComponent = createParentComponent();

    final Injector injector = lightsaber.createInjector(parentComponent);

    verify(parentComponent).configureInjector((LightsaberInjector) injector);
    verifyNoMoreInteractions(parentComponent);
    assertSame(injector, injector.getInstance(Key.of(Injector.class)));
    assertEquals("Parent String", injector.getInstance(String.class));
    assertEquals("Parent String", injector.getInstance(Key.of(String.class)));
  }

  @Test
  public void testCreateChildInjector() throws Exception {
    final Lightsaber lightsaber = new Lightsaber(configurator);
    final InjectorConfigurator parentComponent = createParentComponent();
    final InjectorConfigurator childComponent = createChildComponent();

    final Injector injector = lightsaber.createInjector(parentComponent);
    final Injector childInjector = lightsaber.createChildInjector(injector, childComponent);

    verify(parentComponent).configureInjector((LightsaberInjector) injector);
    verifyNoMoreInteractions(parentComponent);
    verify(childComponent).configureInjector((LightsaberInjector) childInjector);
    verifyNoMoreInteractions(childComponent);
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
    final InjectorConfigurator parentComponent = createParentComponent();
    final InjectorConfigurator childAnnotatedComponent = createChildAnnotatedComponent();

    final Injector injector = lightsaber.createInjector(parentComponent);
    final Injector childInjector =
        lightsaber.createChildInjector(injector, childAnnotatedComponent);

    verify(parentComponent).configureInjector((LightsaberInjector) injector);
    verifyNoMoreInteractions(parentComponent);
    verify(childAnnotatedComponent).configureInjector((LightsaberInjector) childInjector);
    verifyNoMoreInteractions(childAnnotatedComponent);
    final Named annotation = new NamedProxy("Annotated");
    assertSame(injector, injector.getInstance(Key.of(Injector.class)));
    assertSame(childInjector, childInjector.getInstance(Key.of(Injector.class)));
    assertEquals("Parent String", childInjector.getInstance(String.class));
    assertEquals("Parent String", childInjector.getInstance(Key.of(String.class)));
    assertEquals("Child Annotated String", childInjector.getInstance(Key.of(String.class, annotation)));
  }

  private static InjectorConfigurator createParentComponent() {
    final InjectorConfigurator configurator = mock(InjectorConfigurator.class);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final LightsaberInjector injector = (LightsaberInjector) invocation.getArguments()[0];
        injector.registerProvider(String.class, new Provider<String>() {
          @Nonnull
          @Override
          public String get() {
            return "Parent String";
          }
        });
        return null;
      }
    })
        .when(configurator).configureInjector(any(LightsaberInjector.class));
    return configurator;
  }

  private static InjectorConfigurator createChildComponent() {
    final InjectorConfigurator configurator = mock(InjectorConfigurator.class);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final LightsaberInjector injector = (LightsaberInjector) invocation.getArguments()[0];
        injector.registerProvider(Key.of(Object.class), new Provider<Object>() {
          @Nonnull
          @Override
          public Object get() {
            return "Child Object";
          }
        });
        return null;
      }
    })
        .when(configurator).configureInjector(any(LightsaberInjector.class));
    return configurator;
  }

  private static InjectorConfigurator createChildAnnotatedComponent() {
    final InjectorConfigurator configurator = mock(InjectorConfigurator.class);
    doAnswer(new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) throws Throwable {
        final LightsaberInjector injector = (LightsaberInjector) invocation.getArguments()[0];
        injector.registerProvider(Key.of(String.class, new NamedProxy("Annotated")),
            new Provider<String>() {
              @Nonnull
              @Override
              public String get() {
                return "Child Annotated String";
              }
            });
        return null;
      }
    })
        .when(configurator).configureInjector(any(LightsaberInjector.class));
    return configurator;
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
