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

package io.michaelrocks.lightsaber.internal;

import io.michaelrocks.lightsaber.ConfigurationException;
import io.michaelrocks.lightsaber.Injector;
import io.michaelrocks.lightsaber.Key;
import org.junit.Test;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Provider;
import java.lang.annotation.Annotation;

import static org.junit.Assert.*;

public class Lightsaber$$InjectorFactoryTest {
    @Test
    public void testCreateInjector() throws Exception {
        final ParentModule parentModule = new ParentModule();
        final Injector injector = Lightsaber$$InjectorFactory.createInjector(parentModule);
        assertSame(injector, injector.getInstance(Key.of(Injector.class)));
        assertEquals("Parent String", injector.getInstance(Key.of(String.class)));
        assertEquals(2, injector.getAllProviders().size());
        assertTrue(injector.getAllProviders().containsKey(Key.of(Injector.class)));
        assertTrue(injector.getAllProviders().containsKey(Key.of(String.class)));
    }

    @Test
    public void testCreateChildInjector() throws Exception {
        final Injector injector = Lightsaber$$InjectorFactory.createInjector(new ParentModule());
        final Injector childInjector = Lightsaber$$InjectorFactory.createChildInjector(injector, new ChildModule());
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
        final Injector injector = Lightsaber$$InjectorFactory.createInjector(new ParentModule());
        final Injector childInjector =
                Lightsaber$$InjectorFactory.createChildInjector(injector, new ChildAnnotatedModule());
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
        final Injector injector = Lightsaber$$InjectorFactory.createInjector(new ParentModule());
        // noinspection unused
        final Injector childInjector = Lightsaber$$InjectorFactory.createChildInjector(injector, new ParentModule());
    }

    private static class ParentModule implements ConfigurableModule {
        @Override
        public void configureInjector(final LightsaberInjector injector) {
            injector.registerProvider(Key.of(String.class), new Provider<String>() {
                @Override
                public String get() {
                    return "Parent String";
                }
            });
        }
    }

    private static class ChildModule implements ConfigurableModule {
        @Override
        public void configureInjector(final LightsaberInjector injector) {
            injector.registerProvider(Key.of(Object.class), new Provider<Object>() {
                @Override
                public Object get() {
                    return "Child Object";
                }
            });
        }
    }

    private static class ChildAnnotatedModule implements ConfigurableModule {
        @Override
        public void configureInjector(final LightsaberInjector injector) {
            injector.registerProvider(Key.of(String.class, new NamedProxy("Annotated")), new Provider<String>() {
                @Override
                public String get() {
                    return "Child Annotated String";
                }
            });
        }
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
