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

package io.michaelrocks.lightsaber;

import org.junit.Before;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Annotation;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LightsaberTest {
  private Lightsaber lightsaber;

  @Before
  public void createLightsaber() {
    final Lightsaber.Configurator configurator = mock(Lightsaber.Configurator.class, RETURNS_DEEP_STUBS);
    when(configurator.getInjectorConfigurators().get(ParentModule.class))
        .thenReturn(new InjectorConfigurator() {
          @Override
          public void configureInjector(final LightsaberInjector injector, final Object module) {
            injector.registerProvider(Key.of(String.class), new Provider<String>() {
              @Override
              public String get() {
                return "Parent String";
              }
            });
          }
        });
    when(configurator.getInjectorConfigurators().get(ChildModule.class))
        .thenReturn(new InjectorConfigurator() {
          @Override
          public void configureInjector(final LightsaberInjector injector, final Object module) {
            injector.registerProvider(Key.of(Object.class), new Provider<Object>() {
              @Override
              public Object get() {
                return "Child Object";
              }
            });
          }
        });
    when(configurator.getInjectorConfigurators().get(ChildAnnotatedModule.class))
        .thenReturn(new InjectorConfigurator() {
          @Override
          public void configureInjector(final LightsaberInjector injector, final Object module) {
            injector.registerProvider(Key.of(String.class, new NamedProxy("Annotated")),
                new Provider<String>() {
                  @Override
                  public String get() {
                    return "Child Annotated String";
                  }
                });
          }
        });
    lightsaber = new Lightsaber(configurator);
  }

  @Test
  public void testCreateInjector() throws Exception {
    final ParentModule parentModule = new ParentModule();
    final Injector injector = lightsaber.createInjector(parentModule);
    assertSame(injector, injector.getInstance(Key.of(Injector.class)));
    assertEquals("Parent String", injector.getInstance(Key.of(String.class)));
    assertEquals(2, injector.getAllProviders().size());
    assertTrue(injector.getAllProviders().containsKey(Key.of(Injector.class)));
    assertTrue(injector.getAllProviders().containsKey(Key.of(String.class)));
  }

  @Test
  public void testCreateChildInjector() throws Exception {
    final Injector injector = lightsaber.createInjector(new ParentModule());
    final Injector childInjector = lightsaber.createChildInjector(injector, new ChildModule());
    assertSame(injector, injector.getInstance(Key.of(Injector.class)));
    assertSame(childInjector, childInjector.getInstance(Key.of(Injector.class)));
    assertEquals("Parent String", childInjector.getInstance(Key.of(String.class)));
    assertEquals("Child Object", childInjector.getInstance(Key.of(Object.class)));
    assertEquals(3, childInjector.getAllProviders().size());
    assertTrue(childInjector.getAllProviders().containsKey(Key.of(Injector.class)));
    assertTrue(childInjector.getAllProviders().containsKey(Key.of(String.class)));
    assertTrue(childInjector.getAllProviders().containsKey(Key.of(Object.class)));
  }

  @Test
  public void testCreateChildInjectorWithAnnotation() throws Exception {
    final Injector injector = lightsaber.createInjector(new ParentModule());
    final Injector childInjector =
        lightsaber.createChildInjector(injector, new ChildAnnotatedModule());
    final Named annotation = new NamedProxy("Annotated");
    assertSame(injector, injector.getInstance(Key.of(Injector.class)));
    assertSame(childInjector, childInjector.getInstance(Key.of(Injector.class)));
    assertEquals("Parent String", childInjector.getInstance(Key.of(String.class)));
    assertEquals("Child Annotated String", childInjector.getInstance(Key.of(String.class, annotation)));
    assertEquals(3, childInjector.getAllProviders().size());
    assertTrue(childInjector.getAllProviders().containsKey(Key.of(Injector.class)));
    assertTrue(childInjector.getAllProviders().containsKey(Key.of(String.class)));
    assertTrue(childInjector.getAllProviders().containsKey(Key.of(String.class, annotation)));
  }

  @Test(expected = ConfigurationException.class)
  public void testCreateChildInjectorWithSameModule() throws Exception {
    final Injector injector = lightsaber.createInjector(new ParentModule());
    // noinspection unused
    final Injector childInjector = lightsaber.createChildInjector(injector, new ParentModule());
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

    public NamedProxy(@Nonnull final String value) {
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
