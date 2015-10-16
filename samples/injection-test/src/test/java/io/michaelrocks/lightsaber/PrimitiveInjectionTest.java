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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PrimitiveInjectionTest {
    @Test
    public void testUnboxedIntoUnboxedInjection() {
        final UnboxedPrimitiveModule module = new UnboxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final UnboxedInjectableContainer container = new UnboxedInjectableContainer();
        injector.injectMembers(container);

        assertEquals(module.provideBoolean(), container.booleanField);
        assertEquals(module.provideByte(), container.byteField);
        assertEquals(module.provideChar(), container.charField);
        assertEquals(module.provideDouble(), container.doubleField, Double.MIN_VALUE);
        assertEquals(module.provideFloat(), container.floatField, Float.MIN_VALUE);
        assertEquals(module.provideInt(), container.intField);
        assertEquals(module.provideLong(), container.longField);
        assertEquals(module.provideShort(), container.shortField);
    }

    @Test
    public void testUnboxedIntoBoxedInjection() {
        final UnboxedPrimitiveModule module = new UnboxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final BoxedInjectableContainer container = new BoxedInjectableContainer();
        injector.injectMembers(container);

        assertEquals(module.provideBoolean(), container.booleanField);
        assertEquals(module.provideByte(), (byte) container.byteField);
        assertEquals(module.provideChar(), (char) container.characterField);
        assertEquals(module.provideDouble(), container.doubleField, Double.MIN_VALUE);
        assertEquals(module.provideFloat(), container.floatField, Float.MIN_VALUE);
        assertEquals(module.provideInt(), (int) container.integerField);
        assertEquals(module.provideLong(), (long) container.longField);
        assertEquals(module.provideShort(), (short) container.shortField);
    }

    @Test
    public void testBoxedIntoUnboxedInjection() {
        final BoxedPrimitiveModule module = new BoxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final UnboxedInjectableContainer container = new UnboxedInjectableContainer();
        injector.injectMembers(container);

        assertEquals(module.provideBoolean(), container.booleanField);
        assertEquals(module.provideByte(), Byte.valueOf(container.byteField));
        assertEquals(module.provideCharacter(), Character.valueOf(container.charField));
        assertEquals(module.provideDouble(), Double.valueOf(container.doubleField));
        assertEquals(module.provideFloat(), Float.valueOf(container.floatField));
        assertEquals(module.provideInteger(), Integer.valueOf(container.intField));
        assertEquals(module.provideLong(), Long.valueOf(container.longField));
        assertEquals(module.provideShort(), Short.valueOf(container.shortField));
    }

    @Test
    public void testBoxedIntoBoxedInjection() {
        final BoxedPrimitiveModule module = new BoxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final BoxedInjectableContainer container = new BoxedInjectableContainer();
        injector.injectMembers(container);

        assertEquals(module.provideBoolean(), container.booleanField);
        assertEquals(module.provideByte(), container.byteField);
        assertEquals(module.provideCharacter(), container.characterField);
        assertEquals(module.provideDouble(), container.doubleField);
        assertEquals(module.provideFloat(), container.floatField);
        assertEquals(module.provideInteger(), container.integerField);
        assertEquals(module.provideLong(), container.longField);
        assertEquals(module.provideShort(), container.shortField);
    }

    @Test
    public void testUnboxedForUnboxedConstruction() {
        final UnboxedPrimitiveModule module = new UnboxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final UnboxedConstructableContainer container =
                injector.getInstance(Key.of(UnboxedConstructableContainer.class));

        assertEquals(module.provideBoolean(), container.booleanField);
        assertEquals(module.provideByte(), container.byteField);
        assertEquals(module.provideChar(), container.charField);
        assertEquals(module.provideDouble(), container.doubleField, Double.MIN_VALUE);
        assertEquals(module.provideFloat(), container.floatField, Float.MIN_VALUE);
        assertEquals(module.provideInt(), container.intField);
        assertEquals(module.provideLong(), container.longField);
        assertEquals(module.provideShort(), container.shortField);
    }

    @Test
    public void testUnboxedForBoxedConstruction() {
        final UnboxedPrimitiveModule module = new UnboxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final BoxedConstructableContainer container =
                injector.getInstance(Key.of(BoxedConstructableContainer.class));

        assertEquals(module.provideBoolean(), container.booleanField);
        assertEquals(module.provideByte(), (byte) container.byteField);
        assertEquals(module.provideChar(), (char) container.characterField);
        assertEquals(module.provideDouble(), container.doubleField, Double.MIN_VALUE);
        assertEquals(module.provideFloat(), container.floatField, Float.MIN_VALUE);
        assertEquals(module.provideInt(), (int) container.integerField);
        assertEquals(module.provideLong(), (long) container.longField);
        assertEquals(module.provideShort(), (short) container.shortField);
    }

    @Test
    public void testBoxedForUnboxedConstruction() {
        final BoxedPrimitiveModule module = new BoxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final UnboxedConstructableContainer container =
                injector.getInstance(Key.of(UnboxedConstructableContainer.class));

        assertEquals(module.provideBoolean(), container.booleanField);
        assertEquals(module.provideByte(), Byte.valueOf(container.byteField));
        assertEquals(module.provideCharacter(), Character.valueOf(container.charField));
        assertEquals(module.provideDouble(), Double.valueOf(container.doubleField));
        assertEquals(module.provideFloat(), Float.valueOf(container.floatField));
        assertEquals(module.provideInteger(), Integer.valueOf(container.intField));
        assertEquals(module.provideLong(), Long.valueOf(container.longField));
        assertEquals(module.provideShort(), Short.valueOf(container.shortField));
    }

    @Test
    public void testBoxedForBoxedConstruction() {
        final BoxedPrimitiveModule module = new BoxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);
        final BoxedConstructableContainer container =
                injector.getInstance(Key.of(BoxedConstructableContainer.class));

        assertEquals(module.provideBoolean(), container.booleanField);
        assertEquals(module.provideByte(), container.byteField);
        assertEquals(module.provideCharacter(), container.characterField);
        assertEquals(module.provideDouble(), container.doubleField);
        assertEquals(module.provideFloat(), container.floatField);
        assertEquals(module.provideInteger(), container.integerField);
        assertEquals(module.provideLong(), container.longField);
        assertEquals(module.provideShort(), container.shortField);
    }

    @Test
    public void testUnboxedProvision() {
        final UnboxedPrimitiveModule module = new UnboxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);

        final UnboxedResult unboxedResult = injector.getInstance(Key.of(UnboxedResult.class));
        final BoxedResult boxedResult = injector.getInstance(Key.of(BoxedResult.class));

        assertNotNull(unboxedResult);
        assertNotNull(boxedResult);
    }

    @Test
    public void testBoxedProvision() {
        final BoxedPrimitiveModule module = new BoxedPrimitiveModule();
        final Injector injector = Lightsaber.getInstance().createInjector(module);

        final UnboxedResult unboxedResult = injector.getInstance(Key.of(UnboxedResult.class));
        final BoxedResult boxedResult = injector.getInstance(Key.of(BoxedResult.class));

        assertNotNull(unboxedResult);
        assertNotNull(boxedResult);
    }

    private static class UnboxedInjectableContainer {
        @Inject
        boolean booleanField;
        @Inject
        byte byteField;
        @Inject
        char charField;
        @Inject
        double doubleField;
        @Inject
        float floatField;
        @Inject
        int intField;
        @Inject
        long longField;
        @Inject
        short shortField;

        @Inject
        void unboxedMethod(final boolean booleanArg, final byte byteArg, final char charArg, final double doubleArg,
                final float floatArg, final int intArg, final long longArg, final short shortArg) {
            assertEquals(booleanField, booleanArg);
            assertEquals(byteField, byteArg);
            assertEquals(charField, charArg);
            assertEquals(doubleField, doubleArg, Double.MIN_VALUE);
            assertEquals(floatField, floatArg, Float.MIN_VALUE);
            assertEquals(intField, intArg);
            assertEquals(longField, longArg);
            assertEquals(shortField, shortArg);
        }

        @Inject
        void boxedMethod(final Boolean booleanArg, final Byte byteArg, final Character characterArg,
                final Double doubleArg, final Float floatArg, final Integer integerArg, final Long longArg,
                final Short shortArg) {
            assertEquals(booleanField, booleanArg);
            assertEquals(byteField, (byte) byteArg);
            assertEquals(charField, (char) characterArg);
            assertEquals(doubleField, doubleArg, Double.MIN_VALUE);
            assertEquals(floatField, floatArg, Float.MIN_VALUE);
            assertEquals(intField, (int) integerArg);
            assertEquals(longField, (long) longArg);
            assertEquals(shortField, (short) shortArg);
        }
    }

    private static class BoxedInjectableContainer {
        @Inject
        Boolean booleanField;
        @Inject
        Byte byteField;
        @Inject
        Character characterField;
        @Inject
        Double doubleField;
        @Inject
        Float floatField;
        @Inject
        Integer integerField;
        @Inject
        Long longField;
        @Inject
        Short shortField;

        @Inject
        void unboxedMethod(final boolean booleanArg, final byte byteArg, final char charArg, final double doubleArg,
                final float floatArg, final int intArg, final long longArg, final short shortArg) {
            assertEquals(booleanField, booleanArg);
            assertEquals(byteField, Byte.valueOf(byteArg));
            assertEquals(characterField, Character.valueOf(charArg));
            assertEquals(doubleField, Double.valueOf(doubleArg));
            assertEquals(floatField, Float.valueOf(floatArg));
            assertEquals(integerField, Integer.valueOf(intArg));
            assertEquals(longField, Long.valueOf(longArg));
            assertEquals(shortField, Short.valueOf(shortArg));
        }

        @Inject
        void boxedMethod(final Boolean booleanArg, final Byte byteArg, final Character characterArg,
                final Double doubleArg, final Float floatArg, final Integer integerArg, final Long longArg,
                final Short shortArg) {
            assertEquals(booleanField, booleanArg);
            assertEquals(byteField, byteArg);
            assertEquals(characterField, characterArg);
            assertEquals(doubleField, doubleArg);
            assertEquals(floatField, floatArg);
            assertEquals(integerField, integerArg);
            assertEquals(longField, longArg);
            assertEquals(shortField, shortArg);
        }
    }

    @Module
    private static class UnboxedPrimitiveModule {
        @Provides
        public boolean provideBoolean() {
            return true;
        }

        @Provides
        public byte provideByte() {
            return 42;
        }

        @Provides
        public char provideChar() {
            return 'x';
        }

        @Provides
        public double provideDouble() {
            return Math.PI;
        }

        @Provides
        public float provideFloat() {
            return (float) Math.E;
        }

        @Provides
        public int provideInt() {
            return 42424242;
        }

        @Provides
        public long provideLong() {
            return 4242424242424242L;
        }

        @Provides
        public short provideShort() {
            return 4242;
        }

        @Provides
        UnboxedResult consumeUnboxed(final boolean booleanArg, final byte byteArg, final char charArg,
                final double doubleArg, final float floatArg, final int intArg, final long longArg,
                final short shortArg) {
            assertEquals(provideBoolean(), booleanArg);
            assertEquals(provideByte(), byteArg);
            assertEquals(provideChar(), charArg);
            assertEquals(provideDouble(), doubleArg, Double.MIN_VALUE);
            assertEquals(provideFloat(), floatArg, Float.MIN_VALUE);
            assertEquals(provideInt(), intArg);
            assertEquals(provideLong(), longArg);
            assertEquals(provideShort(), shortArg);
            return new UnboxedResult();
        }

        @Provides
        BoxedResult consumeBoxed(final Boolean booleanArg, final Byte byteArg, final Character characterArg,
                final Double doubleArg, final Float floatArg, final Integer integerArg, final Long longArg,
                final Short shortArg) {
            assertEquals(provideBoolean(), booleanArg);
            assertEquals(provideByte(), (byte) byteArg);
            assertEquals(provideChar(), (char) characterArg);
            assertEquals(provideDouble(), doubleArg, Double.MIN_VALUE);
            assertEquals(provideFloat(), floatArg, Float.MIN_VALUE);
            assertEquals(provideInt(), (int) integerArg);
            assertEquals(provideLong(), (long) longArg);
            assertEquals(provideShort(), (short) shortArg);
            return new BoxedResult();
        }
    }

    @Module
    private static class BoxedPrimitiveModule {
        @Provides
        public Boolean provideBoolean() {
            return false;
        }

        @Provides
        public Byte provideByte() {
            return -42;
        }

        @Provides
        public Character provideCharacter() {
            return 'X';
        }

        @Provides
        public Double provideDouble() {
            return -Math.PI;
        }

        @Provides
        public Float provideFloat() {
            return (float) -Math.E;
        }

        @Provides
        public Integer provideInteger() {
            return -42424242;
        }

        @Provides
        public Long provideLong() {
            return -4242424242424242L;
        }

        @Provides
        public Short provideShort() {
            return -4242;
        }

        @Provides
        UnboxedResult consumeUnboxed(final boolean booleanArg, final byte byteArg, final char charArg,
                final double doubleArg, final float floatArg, final int intArg, final long longArg,
                final short shortArg) {
            assertEquals(provideBoolean(), booleanArg);
            assertEquals(provideByte(), Byte.valueOf(byteArg));
            assertEquals(provideCharacter(), Character.valueOf(charArg));
            assertEquals(provideDouble(), Double.valueOf(doubleArg));
            assertEquals(provideFloat(), Float.valueOf(floatArg));
            assertEquals(provideInteger(), Integer.valueOf(intArg));
            assertEquals(provideLong(), Long.valueOf(longArg));
            assertEquals(provideShort(), Short.valueOf(shortArg));
            return new UnboxedResult();
        }

        @Provides
        BoxedResult consumeBoxed(final Boolean booleanArg, final Byte byteArg, final Character characterArg,
                final Double doubleArg, final Float floatArg, final Integer integerArg, final Long longArg,
                final Short shortArg) {
            assertEquals(provideBoolean(), booleanArg);
            assertEquals(provideByte(), byteArg);
            assertEquals(provideCharacter(), characterArg);
            assertEquals(provideDouble(), doubleArg);
            assertEquals(provideFloat(), floatArg);
            assertEquals(provideInteger(), integerArg);
            assertEquals(provideLong(), longArg);
            assertEquals(provideShort(), shortArg);
            return new BoxedResult();
        }
    }

    private static class UnboxedResult {}
    private static class BoxedResult {}

    private static class UnboxedConstructableContainer {
        final boolean booleanField;
        final byte byteField;
        final char charField;
        final double doubleField;
        final float floatField;
        final int intField;
        final long longField;
        final short shortField;

        @Inject
        UnboxedConstructableContainer(final boolean booleanArg, final byte byteArg, final char charArg,
                final double doubleArg, final float floatArg, final int intArg, final long longArg,
                final short shortArg) {
            booleanField = booleanArg;
            byteField = byteArg;
            charField = charArg;
            doubleField = doubleArg;
            floatField = floatArg;
            intField = intArg;
            longField = longArg;
            shortField = shortArg;
        }
    }

    private static class BoxedConstructableContainer {
        final Boolean booleanField;
        final Byte byteField;
        final Character characterField;
        final Double doubleField;
        final Float floatField;
        final Integer integerField;
        final Long longField;
        final Short shortField;

        @Inject
        BoxedConstructableContainer(final Boolean booleanArg, final Byte byteArg, final Character characterArg,
                final Double doubleArg, final Float floatArg, final Integer integerArg, final Long longArg,
                final Short shortArg) {
            booleanField = booleanArg;
            byteField = byteArg;
            characterField = characterArg;
            doubleField = doubleArg;
            floatField = floatArg;
            integerField = integerArg;
            longField = longArg;
            shortField = shortArg;
        }
    }
}
