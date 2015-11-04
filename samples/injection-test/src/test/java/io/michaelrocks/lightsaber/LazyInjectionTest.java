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

import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.*;

public class LazyInjectionTest {
    @Test
    public void testLazyConstructorInjection() {
        final LazyModule module = new LazyModule();
        final Injector injector = Lightsaber.get().createInjector(module);
        final Target target = injector.getInstance(Key.of(ConstructorInjectionTarget.class));
        validateTarget(module, target);
    }

    @Test
    public void testLazyFieldInjection() {
        final LazyModule module = new LazyModule();
        final Injector injector = Lightsaber.get().createInjector(module);
        final Target target = new FieldInjectionTarget();
        injector.injectMembers(target);
        validateTarget(module, target);
    }

    @Test
    public void testLazyMethodInjection() {
        final LazyModule module = new LazyModule();
        final Injector injector = Lightsaber.get().createInjector(module);
        final Target target = new MethodInjectionTarget();
        injector.injectMembers(target);
        validateTarget(module, target);
    }

    private void validateTarget(final LazyModule module, final Target target) {
        assertEquals(module.provideString(), target.getString());
        assertEquals(module.provideString(), target.getLazyString1().get());
        assertEquals(module.provideString(), target.getLazyString2().get());
        assertNotSame(target.getString(), target.getLazyString1().get());
        assertNotSame(target.getString(), target.getLazyString2().get());
        assertNotSame(target.getLazyString1().get(), target.getLazyString2().get());
        assertSame(target.getLazyString1().get(), target.getLazyString1().get());
    }

    @Module
    private static class LazyModule {
        @Provides
        public String provideString() {
            // noinspection RedundantStringConstructorCall
            return new String("String");
        }
    }

    private interface Target {
        String getString();
        Lazy<String> getLazyString1();
        Lazy<String> getLazyString2();
    }

    private static class ConstructorInjectionTarget implements Target {
        private final String string;
        private final Lazy<String> lazyString1;
        private final Lazy<String> lazyString2;

        @Inject
        public ConstructorInjectionTarget(
                final String string,
                final Lazy<String> lazyString1,
                final Lazy<String> lazyString2) {
            this.string = string;
            this.lazyString1 = lazyString1;
            this.lazyString2 = lazyString2;
        }

        @Override
        public String getString() {
            return string;
        }

        @Override
        public Lazy<String> getLazyString1() {
            return lazyString1;
        }

        @Override
        public Lazy<String> getLazyString2() {
            return lazyString2;
        }
    }

    private static class FieldInjectionTarget implements Target {
        @Inject
        private final String string = null;
        @Inject
        private final Lazy<String> lazyString1 = null;
        @Inject
        private final Lazy<String> lazyString2 = null;

        @Override
        public String getString() {
            return string;
        }

        @Override
        public Lazy<String> getLazyString1() {
            return lazyString1;
        }

        @Override
        public Lazy<String> getLazyString2() {
            return lazyString2;
        }
    }

    private static class MethodInjectionTarget implements Target {
        private String string = null;
        private Lazy<String> lazyString1 = null;
        private Lazy<String> lazyString2 = null;

        @Override
        public String getString() {
            return string;
        }

        @Override
        public Lazy<String> getLazyString1() {
            return lazyString1;
        }

        @Override
        public Lazy<String> getLazyString2() {
            return lazyString2;
        }

        @Inject
        public void setString(final String string) {
            this.string = string;
        }

        @Inject
        public void setLazyString1(final Lazy<String> lazyString1) {
            this.lazyString1 = lazyString1;
        }

        @Inject
        public void setLazyString2(final Lazy<String> lazyString2) {
            this.lazyString2 = lazyString2;
        }
    }
}
