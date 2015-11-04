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

import javax.inject.Named;
import javax.inject.Singleton;

import static org.junit.Assert.*;

public class ChildInjectionTest {
    private Lightsaber lightsaber;

    @Before
    public void createLightsaber() {
        lightsaber = new Lightsaber();
    }

    @Test
    public void testCreateSingletonBeforeChildInjector() {
        final Injector parentInjector = lightsaber.createInjector(new ParentModule());
        final String parentString = Lightsaber.getInstance(parentInjector, String.class);
        assertEquals("Parent String", parentString);
        final Injector childInjector = lightsaber.createChildInjector(parentInjector, new ChildModule());
        assertSame(parentString, Lightsaber.getInstance(parentInjector, String.class));
        assertSame(parentString, Lightsaber.getInstance(childInjector, String.class));
    }

    @Test
    public void testCreateSingletonAfterChildInjector() {
        final Injector parentInjector = lightsaber.createInjector(new ParentModule());
        final Injector childInjector = lightsaber.createChildInjector(parentInjector, new ChildModule());
        final String parentString = Lightsaber.getInstance(parentInjector, String.class);
        assertEquals("Parent String", parentString);
        assertSame(parentString, Lightsaber.getInstance(parentInjector, String.class));
        assertSame(parentString, Lightsaber.getInstance(childInjector, String.class));
    }

    @Test
    public void testCreateSingletonInChildInjector() {
        final Injector parentInjector = lightsaber.createInjector(new ParentModule());
        final Injector childInjector = lightsaber.createChildInjector(parentInjector, new ChildModule());
        final String childString = Lightsaber.getInstance(childInjector, String.class);
        assertEquals("Parent String", childString);
        assertSame(childString, Lightsaber.getInstance(parentInjector, String.class));
        assertSame(childString, Lightsaber.getInstance(childInjector, String.class));
    }

    @Test
    public void testCreateSingletonInTwoChildInjectors() {
        final Injector parentInjector = lightsaber.createInjector(new ParentModule());
        final Injector childInjector1 = lightsaber.createChildInjector(parentInjector, new ChildModule());
        final Injector childInjector2 = lightsaber.createChildInjector(parentInjector, new ChildModule());
        final Object childObject1 = Lightsaber.getInstance(childInjector1, Object.class);
        final Object childObject2 = Lightsaber.getInstance(childInjector2, Object.class);
        assertEquals("Child Object", childObject1);
        assertEquals("Child Object", childObject2);
        assertNotSame(childObject1, childObject2);
    }

    @Test(expected = ConfigurationException.class)
    public void testCreateChildInjectorWithParentModule() {
        final Injector parentInjector = lightsaber.createInjector(new ParentModule());
        final Injector childInjector = lightsaber.createChildInjector(parentInjector, new ParentModule());
        final String childString = Lightsaber.getInstance(childInjector, String.class);
        assertEquals("Parent String", childString);
    }

    @Module
    private static class ParentModule {
        @Provides
        @Singleton
        public String provideString() {
            // noinspection RedundantStringConstructorCall
            return new String("Parent String");
        }
    }

    @Module
    private static class ChildModule {
        @Provides
        @Singleton
        public Object provideObject() {
            // noinspection RedundantStringConstructorCall
            return new String("Child Object");
        }

        @Provides
        @Singleton
        @Named("Child String")
        public String provideNamedString() {
            // noinspection RedundantStringConstructorCall
            return new String("Child String");
        }
    }
}
