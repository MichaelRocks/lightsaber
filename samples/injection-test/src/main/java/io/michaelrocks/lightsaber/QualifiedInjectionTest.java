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

import io.michaelrocks.lightsaber.QualifiedInjectionTest.Qualifiers.*;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unused")
public class QualifiedInjectionTest {
    @Test
    public void testConstructionInjection() {
        final QualifiedModule module = new QualifiedModule();
        final Injector injector = Lightsaber.createInjector(module);
        final Container container = injector.getInstance(Key.of(ConstructorInjectionContainer.class));
        validateContainer(module, container);
    }

    @Test
    public void testFieldInjection() {
        final QualifiedModule module = new QualifiedModule();
        final Injector injector = Lightsaber.createInjector(module);
        final Container container = new FieldInjectionContainer();
        injector.injectMembers(container);
        validateContainer(module, container);
    }

    @Test
    public void testMethodInjection() {
        final QualifiedModule module = new QualifiedModule();
        final Injector injector = Lightsaber.createInjector(module);
        final Container container = new MethodInjectionContainer();
        injector.injectMembers(container);
        validateContainer(module, container);
    }

    private void validateContainer(final QualifiedModule module, final Container container) {
        assertEquals(module.provideNoQualifier(), container.getNoQualifier());
        assertEquals(module.provideEmptyQualifier(), container.getEmptyQualifier());
        assertEquals(module.provideBooleanQualifier(), container.getBooleanQualifier());
        assertEquals(module.provideBooleanQualifierExplicit(), container.getBooleanQualifierExplicit());
        assertEquals(module.provideByteQualifier(), container.getByteQualifier());
        assertEquals(module.provideByteQualifierExplicit(), container.getByteQualifierExplicit());
        assertEquals(module.provideCharQualifier(), container.getCharQualifier());
        assertEquals(module.provideCharQualifierExplicit(), container.getCharQualifierExplicit());
        assertEquals(module.provideFloatQualifier(), container.getFloatQualifier());
        assertEquals(module.provideFloatQualifierExplicit(), container.getFloatQualifierExplicit());
        assertEquals(module.provideDoubleQualifier(), container.getDoubleQualifier());
        assertEquals(module.provideDoubleQualifierExplicit(), container.getDoubleQualifierExplicit());
        assertEquals(module.provideIntQualifier(), container.getIntQualifier());
        assertEquals(module.provideIntQualifierExplicit(), container.getIntQualifierExplicit());
        assertEquals(module.provideLongQualifier(), container.getLongQualifier());
        assertEquals(module.provideLongQualifierExplicit(), container.getLongQualifierExplicit());
        assertEquals(module.provideShortQualifier(), container.getShortQualifier());
        assertEquals(module.provideShortQualifierExplicit(), container.getShortQualifierExplicit());
        assertEquals(module.provideStringQualifier(), container.getStringQualifier());
        assertEquals(module.provideStringQualifierExplicit(), container.getStringQualifierExplicit());
        assertEquals(module.provideEnumQualifier(), container.getEnumQualifier());
        assertEquals(module.provideEnumQualifierExplicit(), container.getEnumQualifierExplicit());
        assertEquals(module.provideClassQualifier(), container.getClassQualifier());
        assertEquals(module.provideClassQualifierExplicit(), container.getClassQualifierExplicit());
        assertEquals(module.provideAnnotationQualifier(), container.getAnnotationQualifier());
        assertEquals(module.provideAnnotationQualifierExplicit(), container.getAnnotationQualifierExplicit());
        assertEquals(module.provideBooleanArrayQualifier(), container.getBooleanArrayQualifier());
        assertEquals(module.provideBooleanArrayQualifierExplicit(), container.getBooleanArrayQualifierExplicit());
        assertEquals(module.provideByteArrayQualifier(), container.getByteArrayQualifier());
        assertEquals(module.provideByteArrayQualifierExplicit(), container.getByteArrayQualifierExplicit());
        assertEquals(module.provideCharArrayQualifier(), container.getCharArrayQualifier());
        assertEquals(module.provideCharArrayQualifierExplicit(), container.getCharArrayQualifierExplicit());
        assertEquals(module.provideFloatArrayQualifier(), container.getFloatArrayQualifier());
        assertEquals(module.provideFloatArrayQualifierExplicit(), container.getFloatArrayQualifierExplicit());
        assertEquals(module.provideDoubleArrayQualifier(), container.getDoubleArrayQualifier());
        assertEquals(module.provideDoubleArrayQualifierExplicit(), container.getDoubleArrayQualifierExplicit());
        assertEquals(module.provideIntArrayQualifier(), container.getIntArrayQualifier());
        assertEquals(module.provideIntArrayQualifierExplicit(), container.getIntArrayQualifierExplicit());
        assertEquals(module.provideLongArrayQualifier(), container.getLongArrayQualifier());
        assertEquals(module.provideLongArrayQualifierExplicit(), container.getLongArrayQualifierExplicit());
        assertEquals(module.provideShortArrayQualifier(), container.getShortArrayQualifier());
        assertEquals(module.provideShortArrayQualifierExplicit(), container.getShortArrayQualifierExplicit());
        assertEquals(module.provideStringArrayQualifier(), container.getStringArrayQualifier());
        assertEquals(module.provideStringArrayQualifierExplicit(), container.getStringArrayQualifierExplicit());
        assertEquals(module.provideEnumArrayQualifier(), container.getEnumArrayQualifier());
        assertEquals(module.provideEnumArrayQualifierExplicit(), container.getEnumArrayQualifierExplicit());
        assertEquals(module.provideClassArrayQualifier(), container.getClassArrayQualifier());
        assertEquals(module.provideClassArrayQualifierExplicit(), container.getClassArrayQualifierExplicit());
        assertEquals(module.provideAnnotationArrayQualifier(), container.getAnnotationArrayQualifier());
        assertEquals(module.provideAnnotationArrayQualifierExplicit(), container.getAnnotationArrayQualifierExplicit());
    }

    private static class QualifiedModule implements Module {
        @Provides
        public String provideNoQualifier() {
            return "NoQualifier";
        }

        @Provides
        @EmptyQualifier
        public String provideEmptyQualifier() {
            return "EmptyQualifier";
        }

        @Provides
        @BooleanQualifier
        public String provideBooleanQualifier() {
            return "BooleanQualifier";
        }

        @Provides
        @BooleanQualifier(false)
        public String provideBooleanQualifierExplicit() {
            return "BooleanQualifierExplicit";
        }

        @Provides
        @ByteQualifier
        public String provideByteQualifier() {
            return "ByteQualifier";
        }

        @Provides
        @ByteQualifier(-42)
        public String provideByteQualifierExplicit() {
            return "ByteQualifierExplicit";
        }

        @Provides
        @CharQualifier
        public String provideCharQualifier() {
            return "CharQualifier";
        }

        @Provides
        @CharQualifier('y')
        public String provideCharQualifierExplicit() {
            return "CharQualifierExplicit";
        }

        @Provides
        @FloatQualifier
        public String provideFloatQualifier() {
            return "FloatQualifier";
        }

        @Provides
        @FloatQualifier(-0.0f)
        public String provideFloatQualifierExplicit() {
            return "FloatQualifierExplicit";
        }

        @Provides
        @DoubleQualifier
        public String provideDoubleQualifier() {
            return "DoubleQualifier";
        }

        @Provides
        @DoubleQualifier(-0.0f)
        public String provideDoubleQualifierExplicit() {
            return "DoubleQualifierExplicit";
        }

        @Provides
        @IntQualifier
        public String provideIntQualifier() {
            return "IntQualifier";
        }

        @Provides
        @IntQualifier(-42)
        public String provideIntQualifierExplicit() {
            return "IntQualifierExplicit";
        }

        @Provides
        @LongQualifier
        public String provideLongQualifier() {
            return "LongQualifier";
        }

        @Provides
        @LongQualifier(-42L)
        public String provideLongQualifierExplicit() {
            return "LongQualifierExplicit";
        }

        @Provides
        @ShortQualifier
        public String provideShortQualifier() {
            return "ShortQualifier";
        }

        @Provides
        @ShortQualifier(-42)
        public String provideShortQualifierExplicit() {
            return "ShortQualifierExplicit";
        }

        @Provides
        @StringQualifier
        public String provideStringQualifier() {
            return "StringQualifier";
        }

        @Provides
        @StringQualifier("ExplicitValue")
        public String provideStringQualifierExplicit() {
            return "StringQualifierExplicit";
        }

        @Provides
        @EnumQualifier
        public String provideEnumQualifier() {
            return "EnumQualifier";
        }

        @Provides
        @EnumQualifier(RetentionPolicy.CLASS)
        public String provideEnumQualifierExplicit() {
            return "EnumQualifierExplicit";
        }

        @Provides
        @ClassQualifier
        public String provideClassQualifier() {
            return "ClassQualifier";
        }

        @Provides
        @ClassQualifier(String.class)
        public String provideClassQualifierExplicit() {
            return "ClassQualifierExplicit";
        }

        @Provides
        @AnnotationQualifier
        public String provideAnnotationQualifier() {
            return "AnnotationQualifier";
        }

        @Provides
        @AnnotationQualifier(@IntQualifier(-42))
        public String provideAnnotationQualifierExplicit() {
            return "AnnotationQualifierExplicit";
        }

        @Provides
        @BooleanArrayQualifier
        public String provideBooleanArrayQualifier() {
            return "BooleanArrayQualifier";
        }

        @Provides
        @BooleanArrayQualifier(false)
        public String provideBooleanArrayQualifierExplicit() {
            return "BooleanArrayQualifierExplicit";
        }

        @Provides
        @ByteArrayQualifier
        public String provideByteArrayQualifier() {
            return "ByteArrayQualifier";
        }

        @Provides
        @ByteArrayQualifier(-42)
        public String provideByteArrayQualifierExplicit() {
            return "ByteArrayQualifierExplicit";
        }

        @Provides
        @CharArrayQualifier
        public String provideCharArrayQualifier() {
            return "CharArrayQualifier";
        }

        @Provides
        @CharArrayQualifier('y')
        public String provideCharArrayQualifierExplicit() {
            return "CharArrayQualifierExplicit";
        }

        @Provides
        @FloatArrayQualifier
        public String provideFloatArrayQualifier() {
            return "FloatArrayQualifier";
        }

        @Provides
        @FloatArrayQualifier(-0.0f)
        public String provideFloatArrayQualifierExplicit() {
            return "FloatArrayQualifierExplicit";
        }

        @Provides
        @DoubleArrayQualifier
        public String provideDoubleArrayQualifier() {
            return "DoubleArrayQualifier";
        }

        @Provides
        @DoubleArrayQualifier(-0.0)
        public String provideDoubleArrayQualifierExplicit() {
            return "DoubleArrayQualifierExplicit";
        }

        @Provides
        @IntArrayQualifier
        public String provideIntArrayQualifier() {
            return "IntArrayQualifier";
        }

        @Provides
        @IntArrayQualifier(-42)
        public String provideIntArrayQualifierExplicit() {
            return "IntArrayQualifierExplicit";
        }

        @Provides
        @LongArrayQualifier
        public String provideLongArrayQualifier() {
            return "LongArrayQualifier";
        }

        @Provides
        @LongArrayQualifier(-42L)
        public String provideLongArrayQualifierExplicit() {
            return "LongArrayQualifierExplicit";
        }

        @Provides
        @ShortArrayQualifier
        public String provideShortArrayQualifier() {
            return "ShortArrayQualifier";
        }

        @Provides
        @ShortArrayQualifier(-42)
        public String provideShortArrayQualifierExplicit() {
            return "ShortArrayQualifierExplicit";
        }

        @Provides
        @StringArrayQualifier
        public String provideStringArrayQualifier() {
            return "StringArrayQualifier";
        }

        @Provides
        @StringArrayQualifier("ExplicitValue")
        public String provideStringArrayQualifierExplicit() {
            return "StringArrayQualifierExplicit";
        }

        @Provides
        @EnumArrayQualifier
        public String provideEnumArrayQualifier() {
            return "EnumArrayQualifier";
        }

        @Provides
        @EnumArrayQualifier(RetentionPolicy.CLASS)
        public String provideEnumArrayQualifierExplicit() {
            return "EnumArrayQualifierExplicit";
        }

        @Provides
        @ClassArrayQualifier
        public String provideClassArrayQualifier() {
            return "ClassArrayQualifier";
        }

        @Provides
        @ClassArrayQualifier(String.class)
        public String provideClassArrayQualifierExplicit() {
            return "ClassArrayQualifierExplicit";
        }

        @Provides
        @AnnotationArrayQualifier
        public String provideAnnotationArrayQualifier() {
            return "AnnotationArrayQualifier";
        }

        @Provides
        @AnnotationArrayQualifier(@IntQualifier(-42))
        public String provideAnnotationArrayQualifierExplicit() {
            return "AnnotationArrayQualifierExplicit";
        }
    }

    private interface Container {
        String getNoQualifier();
        String getEmptyQualifier();
        String getBooleanQualifier();
        String getBooleanQualifierExplicit();
        String getByteQualifier();
        String getByteQualifierExplicit();
        String getCharQualifier();
        String getCharQualifierExplicit();
        String getFloatQualifier();
        String getFloatQualifierExplicit();
        String getDoubleQualifier();
        String getDoubleQualifierExplicit();
        String getIntQualifier();
        String getIntQualifierExplicit();
        String getLongQualifier();
        String getLongQualifierExplicit();
        String getShortQualifier();
        String getShortQualifierExplicit();
        String getStringQualifier();
        String getStringQualifierExplicit();
        String getEnumQualifier();
        String getEnumQualifierExplicit();
        String getClassQualifier();
        String getClassQualifierExplicit();
        String getAnnotationQualifier();
        String getAnnotationQualifierExplicit();
        String getBooleanArrayQualifier();
        String getBooleanArrayQualifierExplicit();
        String getByteArrayQualifier();
        String getByteArrayQualifierExplicit();
        String getCharArrayQualifier();
        String getCharArrayQualifierExplicit();
        String getFloatArrayQualifier();
        String getFloatArrayQualifierExplicit();
        String getDoubleArrayQualifier();
        String getDoubleArrayQualifierExplicit();
        String getIntArrayQualifier();
        String getIntArrayQualifierExplicit();
        String getLongArrayQualifier();
        String getLongArrayQualifierExplicit();
        String getShortArrayQualifier();
        String getShortArrayQualifierExplicit();
        String getStringArrayQualifier();
        String getStringArrayQualifierExplicit();
        String getEnumArrayQualifier();
        String getEnumArrayQualifierExplicit();
        String getClassArrayQualifier();
        String getClassArrayQualifierExplicit();
        String getAnnotationArrayQualifier();
        String getAnnotationArrayQualifierExplicit();
    }

    private static class ConstructorInjectionContainer implements Container {
        private final String noQualifier;
        private final String emptyQualifier;
        private final String booleanQualifier;
        private final String booleanQualifierExplicit;
        private final String byteQualifier;
        private final String byteQualifierExplicit;
        private final String charQualifier;
        private final String charQualifierExplicit;
        private final String floatQualifier;
        private final String floatQualifierExplicit;
        private final String doubleQualifier;
        private final String doubleQualifierExplicit;
        private final String intQualifier;
        private final String intQualifierExplicit;
        private final String longQualifier;
        private final String longQualifierExplicit;
        private final String shortQualifier;
        private final String shortQualifierExplicit;
        private final String stringQualifier;
        private final String stringQualifierExplicit;
        private final String enumQualifier;
        private final String enumQualifierExplicit;
        private final String classQualifier;
        private final String classQualifierExplicit;
        private final String annotationQualifier;
        private final String annotationQualifierExplicit;
        private final String booleanArrayQualifier;
        private final String booleanArrayQualifierExplicit;
        private final String byteArrayQualifier;
        private final String byteArrayQualifierExplicit;
        private final String charArrayQualifier;
        private final String charArrayQualifierExplicit;
        private final String floatArrayQualifier;
        private final String floatArrayQualifierExplicit;
        private final String doubleArrayQualifier;
        private final String doubleArrayQualifierExplicit;
        private final String intArrayQualifier;
        private final String intArrayQualifierExplicit;
        private final String longArrayQualifier;
        private final String longArrayQualifierExplicit;
        private final String shortArrayQualifier;
        private final String shortArrayQualifierExplicit;
        private final String stringArrayQualifier;
        private final String stringArrayQualifierExplicit;
        private final String enumArrayQualifier;
        private final String enumArrayQualifierExplicit;
        private final String classArrayQualifier;
        private final String classArrayQualifierExplicit;
        private final String annotationArrayQualifier;
        private final String annotationArrayQualifierExplicit;

        @Inject
        public ConstructorInjectionContainer(
                final String noQualifier,
                final String emptyQualifier,
                final String booleanQualifier,
                final String booleanQualifierExplicit,
                final String byteQualifier,
                final String byteQualifierExplicit,
                final String charQualifier,
                final String charQualifierExplicit,
                final String floatQualifier,
                final String floatQualifierExplicit,
                final String doubleQualifier,
                final String doubleQualifierExplicit,
                final String intQualifier,
                final String intQualifierExplicit,
                final String longQualifier,
                final String longQualifierExplicit,
                final String shortQualifier,
                final String shortQualifierExplicit,
                final String stringQualifier,
                final String stringQualifierExplicit,
                final String enumQualifier,
                final String enumQualifierExplicit,
                final String classQualifier,
                final String classQualifierExplicit,
                final String annotationQualifier,
                final String annotationQualifierExplicit,
                final String booleanArrayQualifier,
                final String booleanArrayQualifierExplicit,
                final String byteArrayQualifier,
                final String byteArrayQualifierExplicit,
                final String charArrayQualifier,
                final String charArrayQualifierExplicit,
                final String floatArrayQualifier,
                final String floatArrayQualifierExplicit,
                final String doubleArrayQualifier,
                final String doubleArrayQualifierExplicit,
                final String intArrayQualifier,
                final String intArrayQualifierExplicit,
                final String longArrayQualifier,
                final String longArrayQualifierExplicit,
                final String shortArrayQualifier,
                final String shortArrayQualifierExplicit,
                final String stringArrayQualifier,
                final String stringArrayQualifierExplicit,
                final String enumArrayQualifier,
                final String enumArrayQualifierExplicit,
                final String classArrayQualifier,
                final String classArrayQualifierExplicit,
                final String annotationArrayQualifier,
                final String annotationArrayQualifierExplicit) {
            this.noQualifier = noQualifier;
            this.emptyQualifier = emptyQualifier;
            this.booleanQualifier = booleanQualifier;
            this.booleanQualifierExplicit = booleanQualifierExplicit;
            this.byteQualifier = byteQualifier;
            this.byteQualifierExplicit = byteQualifierExplicit;
            this.charQualifier = charQualifier;
            this.charQualifierExplicit = charQualifierExplicit;
            this.floatQualifier = floatQualifier;
            this.floatQualifierExplicit = floatQualifierExplicit;
            this.doubleQualifier = doubleQualifier;
            this.doubleQualifierExplicit = doubleQualifierExplicit;
            this.intQualifier = intQualifier;
            this.intQualifierExplicit = intQualifierExplicit;
            this.longQualifier = longQualifier;
            this.longQualifierExplicit = longQualifierExplicit;
            this.shortQualifier = shortQualifier;
            this.shortQualifierExplicit = shortQualifierExplicit;
            this.stringQualifier = stringQualifier;
            this.stringQualifierExplicit = stringQualifierExplicit;
            this.enumQualifier = enumQualifier;
            this.enumQualifierExplicit = enumQualifierExplicit;
            this.classQualifier = classQualifier;
            this.classQualifierExplicit = classQualifierExplicit;
            this.annotationQualifier = annotationQualifier;
            this.annotationQualifierExplicit = annotationQualifierExplicit;
            this.booleanArrayQualifier = booleanArrayQualifier;
            this.booleanArrayQualifierExplicit = booleanArrayQualifierExplicit;
            this.byteArrayQualifier = byteArrayQualifier;
            this.byteArrayQualifierExplicit = byteArrayQualifierExplicit;
            this.charArrayQualifier = charArrayQualifier;
            this.charArrayQualifierExplicit = charArrayQualifierExplicit;
            this.floatArrayQualifier = floatArrayQualifier;
            this.floatArrayQualifierExplicit = floatArrayQualifierExplicit;
            this.doubleArrayQualifier = doubleArrayQualifier;
            this.doubleArrayQualifierExplicit = doubleArrayQualifierExplicit;
            this.intArrayQualifier = intArrayQualifier;
            this.intArrayQualifierExplicit = intArrayQualifierExplicit;
            this.longArrayQualifier = longArrayQualifier;
            this.longArrayQualifierExplicit = longArrayQualifierExplicit;
            this.shortArrayQualifier = shortArrayQualifier;
            this.shortArrayQualifierExplicit = shortArrayQualifierExplicit;
            this.stringArrayQualifier = stringArrayQualifier;
            this.stringArrayQualifierExplicit = stringArrayQualifierExplicit;
            this.enumArrayQualifier = enumArrayQualifier;
            this.enumArrayQualifierExplicit = enumArrayQualifierExplicit;
            this.classArrayQualifier = classArrayQualifier;
            this.classArrayQualifierExplicit = classArrayQualifierExplicit;
            this.annotationArrayQualifier = annotationArrayQualifier;
            this.annotationArrayQualifierExplicit = annotationArrayQualifierExplicit;
        }

        @Override
        public String getNoQualifier() {
            return noQualifier;
        }

        @Override
        public String getEmptyQualifier() {
            return emptyQualifier;
        }

        @Override
        public String getBooleanQualifier() {
            return booleanQualifier;
        }

        @Override
        public String getBooleanQualifierExplicit() {
            return booleanQualifierExplicit;
        }

        @Override
        public String getByteQualifier() {
            return byteQualifier;
        }

        @Override
        public String getByteQualifierExplicit() {
            return byteQualifierExplicit;
        }

        @Override
        public String getCharQualifier() {
            return charQualifier;
        }

        @Override
        public String getCharQualifierExplicit() {
            return charQualifierExplicit;
        }

        @Override
        public String getFloatQualifier() {
            return floatQualifier;
        }

        @Override
        public String getFloatQualifierExplicit() {
            return floatQualifierExplicit;
        }

        @Override
        public String getDoubleQualifier() {
            return doubleQualifier;
        }

        @Override
        public String getDoubleQualifierExplicit() {
            return doubleQualifierExplicit;
        }

        @Override
        public String getIntQualifier() {
            return intQualifier;
        }

        @Override
        public String getIntQualifierExplicit() {
            return intQualifierExplicit;
        }

        @Override
        public String getLongQualifier() {
            return longQualifier;
        }

        @Override
        public String getLongQualifierExplicit() {
            return longQualifierExplicit;
        }

        @Override
        public String getShortQualifier() {
            return shortQualifier;
        }

        @Override
        public String getShortQualifierExplicit() {
            return shortQualifierExplicit;
        }

        @Override
        public String getStringQualifier() {
            return stringQualifier;
        }

        @Override
        public String getStringQualifierExplicit() {
            return stringQualifierExplicit;
        }

        @Override
        public String getEnumQualifier() {
            return enumQualifier;
        }

        @Override
        public String getEnumQualifierExplicit() {
            return enumQualifierExplicit;
        }

        @Override
        public String getClassQualifier() {
            return classQualifier;
        }

        @Override
        public String getClassQualifierExplicit() {
            return classQualifierExplicit;
        }

        @Override
        public String getAnnotationQualifier() {
            return annotationQualifier;
        }

        @Override
        public String getAnnotationQualifierExplicit() {
            return annotationQualifierExplicit;
        }

        @Override
        public String getBooleanArrayQualifier() {
            return booleanArrayQualifier;
        }

        @Override
        public String getBooleanArrayQualifierExplicit() {
            return booleanArrayQualifierExplicit;
        }

        @Override
        public String getByteArrayQualifier() {
            return byteArrayQualifier;
        }

        @Override
        public String getByteArrayQualifierExplicit() {
            return byteArrayQualifierExplicit;
        }

        @Override
        public String getCharArrayQualifier() {
            return charArrayQualifier;
        }

        @Override
        public String getCharArrayQualifierExplicit() {
            return charArrayQualifierExplicit;
        }

        @Override
        public String getFloatArrayQualifier() {
            return floatArrayQualifier;
        }

        @Override
        public String getFloatArrayQualifierExplicit() {
            return floatArrayQualifierExplicit;
        }

        @Override
        public String getDoubleArrayQualifier() {
            return doubleArrayQualifier;
        }

        @Override
        public String getDoubleArrayQualifierExplicit() {
            return doubleArrayQualifierExplicit;
        }

        @Override
        public String getIntArrayQualifier() {
            return intArrayQualifier;
        }

        @Override
        public String getIntArrayQualifierExplicit() {
            return intArrayQualifierExplicit;
        }

        @Override
        public String getLongArrayQualifier() {
            return longArrayQualifier;
        }

        @Override
        public String getLongArrayQualifierExplicit() {
            return longArrayQualifierExplicit;
        }

        @Override
        public String getShortArrayQualifier() {
            return shortArrayQualifier;
        }

        @Override
        public String getShortArrayQualifierExplicit() {
            return shortArrayQualifierExplicit;
        }

        @Override
        public String getStringArrayQualifier() {
            return stringArrayQualifier;
        }

        @Override
        public String getStringArrayQualifierExplicit() {
            return stringArrayQualifierExplicit;
        }

        @Override
        public String getEnumArrayQualifier() {
            return enumArrayQualifier;
        }

        @Override
        public String getEnumArrayQualifierExplicit() {
            return enumArrayQualifierExplicit;
        }

        @Override
        public String getClassArrayQualifier() {
            return classArrayQualifier;
        }

        @Override
        public String getClassArrayQualifierExplicit() {
            return classArrayQualifierExplicit;
        }

        @Override
        public String getAnnotationArrayQualifier() {
            return annotationArrayQualifier;
        }

        @Override
        public String getAnnotationArrayQualifierExplicit() {
            return annotationArrayQualifierExplicit;
        }
    }

    private static class FieldInjectionContainer implements Container {
        @Inject
        final String noQualifier = null;

        @Inject
        @EmptyQualifier
        final String emptyQualifier = null;

        @Inject
        @BooleanQualifier
        final String booleanQualifier = null;

        @Inject
        @BooleanQualifier(false)
        final String booleanQualifierExplicit = null;

        @Inject
        @ByteQualifier
        final String byteQualifier = null;

        @Inject
        @ByteQualifier(-42)
        final String byteQualifierExplicit = null;

        @Inject
        @CharQualifier
        final String charQualifier = null;

        @Inject
        @CharQualifier('y')
        final String charQualifierExplicit = null;

        @Inject
        @FloatQualifier
        final String floatQualifier = null;

        @Inject
        @FloatQualifier(-0.0f)
        final String floatQualifierExplicit = null;

        @Inject
        @DoubleQualifier
        final String doubleQualifier = null;

        @Inject
        @DoubleQualifier(-0.0f)
        final String doubleQualifierExplicit = null;

        @Inject
        @IntQualifier
        final String intQualifier = null;

        @Inject
        @IntQualifier(-42)
        final String intQualifierExplicit = null;

        @Inject
        @LongQualifier
        final String longQualifier = null;

        @Inject
        @LongQualifier(-42L)
        final String longQualifierExplicit = null;

        @Inject
        @ShortQualifier
        final String shortQualifier = null;

        @Inject
        @ShortQualifier(-42)
        final String shortQualifierExplicit = null;

        @Inject
        @StringQualifier
        final String stringQualifier = null;

        @Inject
        @StringQualifier("ExplicitValue")
        final String stringQualifierExplicit = null;

        @Inject
        @EnumQualifier
        final String enumQualifier = null;

        @Inject
        @EnumQualifier(RetentionPolicy.CLASS)
        final String enumQualifierExplicit = null;

        @Inject
        @ClassQualifier
        final String classQualifier = null;

        @Inject
        @ClassQualifier(String.class)
        final String classQualifierExplicit = null;

        @Inject
        @AnnotationQualifier
        final String annotationQualifier = null;

        @Inject
        @AnnotationQualifier(@IntQualifier(-42))
        final String annotationQualifierExplicit = null;

        @Inject
        @BooleanArrayQualifier
        final String booleanArrayQualifier = null;

        @Inject
        @BooleanArrayQualifier(false)
        final String booleanArrayQualifierExplicit = null;

        @Inject
        @ByteArrayQualifier
        final String byteArrayQualifier = null;

        @Inject
        @ByteArrayQualifier(-42)
        final String byteArrayQualifierExplicit = null;

        @Inject
        @CharArrayQualifier
        final String charArrayQualifier = null;

        @Inject
        @CharArrayQualifier('y')
        final String charArrayQualifierExplicit = null;

        @Inject
        @FloatArrayQualifier
        final String floatArrayQualifier = null;

        @Inject
        @FloatArrayQualifier(-0.0f)
        final String floatArrayQualifierExplicit = null;

        @Inject
        @DoubleArrayQualifier
        final String doubleArrayQualifier = null;

        @Inject
        @DoubleArrayQualifier(-0.0)
        final String doubleArrayQualifierExplicit = null;

        @Inject
        @IntArrayQualifier
        final String intArrayQualifier = null;

        @Inject
        @IntArrayQualifier(-42)
        final String intArrayQualifierExplicit = null;

        @Inject
        @LongArrayQualifier
        final String longArrayQualifier = null;

        @Inject
        @LongArrayQualifier(-42L)
        final String longArrayQualifierExplicit = null;

        @Inject
        @ShortArrayQualifier
        final String shortArrayQualifier = null;

        @Inject
        @ShortArrayQualifier(-42)
        final String shortArrayQualifierExplicit = null;

        @Inject
        @StringArrayQualifier
        final String stringArrayQualifier = null;

        @Inject
        @StringArrayQualifier("ExplicitValue")
        final String stringArrayQualifierExplicit = null;

        @Inject
        @EnumArrayQualifier
        final String enumArrayQualifier = null;

        @Inject
        @EnumArrayQualifier(RetentionPolicy.CLASS)
        final String enumArrayQualifierExplicit = null;

        @Inject
        @ClassArrayQualifier
        final String classArrayQualifier = null;

        @Inject
        @ClassArrayQualifier(String.class)
        final String classArrayQualifierExplicit = null;

        @Inject
        @AnnotationArrayQualifier
        final String annotationArrayQualifier = null;

        @Inject
        @AnnotationArrayQualifier(@IntQualifier(-42))
        final String annotationArrayQualifierExplicit = null;

        @Override
        public String getNoQualifier() {
            return noQualifier;
        }

        @Override
        public String getEmptyQualifier() {
            return emptyQualifier;
        }

        @Override
        public String getBooleanQualifier() {
            return booleanQualifier;
        }

        @Override
        public String getBooleanQualifierExplicit() {
            return booleanQualifierExplicit;
        }

        @Override
        public String getByteQualifier() {
            return byteQualifier;
        }

        @Override
        public String getByteQualifierExplicit() {
            return byteQualifierExplicit;
        }

        @Override
        public String getCharQualifier() {
            return charQualifier;
        }

        @Override
        public String getCharQualifierExplicit() {
            return charQualifierExplicit;
        }

        @Override
        public String getFloatQualifier() {
            return floatQualifier;
        }

        @Override
        public String getFloatQualifierExplicit() {
            return floatQualifierExplicit;
        }

        @Override
        public String getDoubleQualifier() {
            return doubleQualifier;
        }

        @Override
        public String getDoubleQualifierExplicit() {
            return doubleQualifierExplicit;
        }

        @Override
        public String getIntQualifier() {
            return intQualifier;
        }

        @Override
        public String getIntQualifierExplicit() {
            return intQualifierExplicit;
        }

        @Override
        public String getLongQualifier() {
            return longQualifier;
        }

        @Override
        public String getLongQualifierExplicit() {
            return longQualifierExplicit;
        }

        @Override
        public String getShortQualifier() {
            return shortQualifier;
        }

        @Override
        public String getShortQualifierExplicit() {
            return shortQualifierExplicit;
        }

        @Override
        public String getStringQualifier() {
            return stringQualifier;
        }

        @Override
        public String getStringQualifierExplicit() {
            return stringQualifierExplicit;
        }

        @Override
        public String getEnumQualifier() {
            return enumQualifier;
        }

        @Override
        public String getEnumQualifierExplicit() {
            return enumQualifierExplicit;
        }

        @Override
        public String getClassQualifier() {
            return classQualifier;
        }

        @Override
        public String getClassQualifierExplicit() {
            return classQualifierExplicit;
        }

        @Override
        public String getAnnotationQualifier() {
            return annotationQualifier;
        }

        @Override
        public String getAnnotationQualifierExplicit() {
            return annotationQualifierExplicit;
        }

        @Override
        public String getBooleanArrayQualifier() {
            return booleanArrayQualifier;
        }

        @Override
        public String getBooleanArrayQualifierExplicit() {
            return booleanArrayQualifierExplicit;
        }

        @Override
        public String getByteArrayQualifier() {
            return byteArrayQualifier;
        }

        @Override
        public String getByteArrayQualifierExplicit() {
            return byteArrayQualifierExplicit;
        }

        @Override
        public String getCharArrayQualifier() {
            return charArrayQualifier;
        }

        @Override
        public String getCharArrayQualifierExplicit() {
            return charArrayQualifierExplicit;
        }

        @Override
        public String getFloatArrayQualifier() {
            return floatArrayQualifier;
        }

        @Override
        public String getFloatArrayQualifierExplicit() {
            return floatArrayQualifierExplicit;
        }

        @Override
        public String getDoubleArrayQualifier() {
            return doubleArrayQualifier;
        }

        @Override
        public String getDoubleArrayQualifierExplicit() {
            return doubleArrayQualifierExplicit;
        }

        @Override
        public String getIntArrayQualifier() {
            return intArrayQualifier;
        }

        @Override
        public String getIntArrayQualifierExplicit() {
            return intArrayQualifierExplicit;
        }

        @Override
        public String getLongArrayQualifier() {
            return longArrayQualifier;
        }

        @Override
        public String getLongArrayQualifierExplicit() {
            return longArrayQualifierExplicit;
        }

        @Override
        public String getShortArrayQualifier() {
            return shortArrayQualifier;
        }

        @Override
        public String getShortArrayQualifierExplicit() {
            return shortArrayQualifierExplicit;
        }

        @Override
        public String getStringArrayQualifier() {
            return stringArrayQualifier;
        }

        @Override
        public String getStringArrayQualifierExplicit() {
            return stringArrayQualifierExplicit;
        }

        @Override
        public String getEnumArrayQualifier() {
            return enumArrayQualifier;
        }

        @Override
        public String getEnumArrayQualifierExplicit() {
            return enumArrayQualifierExplicit;
        }

        @Override
        public String getClassArrayQualifier() {
            return classArrayQualifier;
        }

        @Override
        public String getClassArrayQualifierExplicit() {
            return classArrayQualifierExplicit;
        }

        @Override
        public String getAnnotationArrayQualifier() {
            return annotationArrayQualifier;
        }

        @Override
        public String getAnnotationArrayQualifierExplicit() {
            return annotationArrayQualifierExplicit;
        }
    }

    private static class MethodInjectionContainer implements Container {
        private String noQualifier;
        private String emptyQualifier;
        private String booleanQualifier;
        private String booleanQualifierExplicit;
        private String byteQualifier;
        private String byteQualifierExplicit;
        private String charQualifier;
        private String charQualifierExplicit;
        private String floatQualifier;
        private String floatQualifierExplicit;
        private String doubleQualifier;
        private String doubleQualifierExplicit;
        private String intQualifier;
        private String intQualifierExplicit;
        private String longQualifier;
        private String longQualifierExplicit;
        private String shortQualifier;
        private String shortQualifierExplicit;
        private String stringQualifier;
        private String stringQualifierExplicit;
        private String enumQualifier;
        private String enumQualifierExplicit;
        private String classQualifier;
        private String classQualifierExplicit;
        private String annotationQualifier;
        private String annotationQualifierExplicit;
        private String booleanArrayQualifier;
        private String booleanArrayQualifierExplicit;
        private String byteArrayQualifier;
        private String byteArrayQualifierExplicit;
        private String charArrayQualifier;
        private String charArrayQualifierExplicit;
        private String floatArrayQualifier;
        private String floatArrayQualifierExplicit;
        private String doubleArrayQualifier;
        private String doubleArrayQualifierExplicit;
        private String intArrayQualifier;
        private String intArrayQualifierExplicit;
        private String longArrayQualifier;
        private String longArrayQualifierExplicit;
        private String shortArrayQualifier;
        private String shortArrayQualifierExplicit;
        private String stringArrayQualifier;
        private String stringArrayQualifierExplicit;
        private String enumArrayQualifier;
        private String enumArrayQualifierExplicit;
        private String classArrayQualifier;
        private String classArrayQualifierExplicit;
        private String annotationArrayQualifier;
        private String annotationArrayQualifierExplicit;

        @Override
        public String getNoQualifier() {
            return noQualifier;
        }

        @Override
        public String getEmptyQualifier() {
            return emptyQualifier;
        }

        @Override
        public String getBooleanQualifier() {
            return booleanQualifier;
        }

        @Override
        public String getBooleanQualifierExplicit() {
            return booleanQualifierExplicit;
        }

        @Override
        public String getByteQualifier() {
            return byteQualifier;
        }

        @Override
        public String getByteQualifierExplicit() {
            return byteQualifierExplicit;
        }

        @Override
        public String getCharQualifier() {
            return charQualifier;
        }

        @Override
        public String getCharQualifierExplicit() {
            return charQualifierExplicit;
        }

        @Override
        public String getFloatQualifier() {
            return floatQualifier;
        }

        @Override
        public String getFloatQualifierExplicit() {
            return floatQualifierExplicit;
        }

        @Override
        public String getDoubleQualifier() {
            return doubleQualifier;
        }

        @Override
        public String getDoubleQualifierExplicit() {
            return doubleQualifierExplicit;
        }

        @Override
        public String getIntQualifier() {
            return intQualifier;
        }

        @Override
        public String getIntQualifierExplicit() {
            return intQualifierExplicit;
        }

        @Override
        public String getLongQualifier() {
            return longQualifier;
        }

        @Override
        public String getLongQualifierExplicit() {
            return longQualifierExplicit;
        }

        @Override
        public String getShortQualifier() {
            return shortQualifier;
        }

        @Override
        public String getShortQualifierExplicit() {
            return shortQualifierExplicit;
        }

        @Override
        public String getStringQualifier() {
            return stringQualifier;
        }

        @Override
        public String getStringQualifierExplicit() {
            return stringQualifierExplicit;
        }

        @Override
        public String getEnumQualifier() {
            return enumQualifier;
        }

        @Override
        public String getEnumQualifierExplicit() {
            return enumQualifierExplicit;
        }

        @Override
        public String getClassQualifier() {
            return classQualifier;
        }

        @Override
        public String getClassQualifierExplicit() {
            return classQualifierExplicit;
        }

        @Override
        public String getAnnotationQualifier() {
            return annotationQualifier;
        }

        @Override
        public String getAnnotationQualifierExplicit() {
            return annotationQualifierExplicit;
        }

        @Override
        public String getBooleanArrayQualifier() {
            return booleanArrayQualifier;
        }

        @Override
        public String getBooleanArrayQualifierExplicit() {
            return booleanArrayQualifierExplicit;
        }

        @Override
        public String getByteArrayQualifier() {
            return byteArrayQualifier;
        }

        @Override
        public String getByteArrayQualifierExplicit() {
            return byteArrayQualifierExplicit;
        }

        @Override
        public String getCharArrayQualifier() {
            return charArrayQualifier;
        }

        @Override
        public String getCharArrayQualifierExplicit() {
            return charArrayQualifierExplicit;
        }

        @Override
        public String getFloatArrayQualifier() {
            return floatArrayQualifier;
        }

        @Override
        public String getFloatArrayQualifierExplicit() {
            return floatArrayQualifierExplicit;
        }

        @Override
        public String getDoubleArrayQualifier() {
            return doubleArrayQualifier;
        }

        @Override
        public String getDoubleArrayQualifierExplicit() {
            return doubleArrayQualifierExplicit;
        }

        @Override
        public String getIntArrayQualifier() {
            return intArrayQualifier;
        }

        @Override
        public String getIntArrayQualifierExplicit() {
            return intArrayQualifierExplicit;
        }

        @Override
        public String getLongArrayQualifier() {
            return longArrayQualifier;
        }

        @Override
        public String getLongArrayQualifierExplicit() {
            return longArrayQualifierExplicit;
        }

        @Override
        public String getShortArrayQualifier() {
            return shortArrayQualifier;
        }

        @Override
        public String getShortArrayQualifierExplicit() {
            return shortArrayQualifierExplicit;
        }

        @Override
        public String getStringArrayQualifier() {
            return stringArrayQualifier;
        }

        @Override
        public String getStringArrayQualifierExplicit() {
            return stringArrayQualifierExplicit;
        }

        @Override
        public String getEnumArrayQualifier() {
            return enumArrayQualifier;
        }

        @Override
        public String getEnumArrayQualifierExplicit() {
            return enumArrayQualifierExplicit;
        }

        @Override
        public String getClassArrayQualifier() {
            return classArrayQualifier;
        }

        @Override
        public String getClassArrayQualifierExplicit() {
            return classArrayQualifierExplicit;
        }

        @Override
        public String getAnnotationArrayQualifier() {
            return annotationArrayQualifier;
        }

        @Override
        public String getAnnotationArrayQualifierExplicit() {
            return annotationArrayQualifierExplicit;
        }

        private void setNoQualifier(final String noQualifier) {
            this.noQualifier = noQualifier;
        }

        private void setEmptyQualifier(@EmptyQualifier final String emptyQualifier) {
            this.emptyQualifier = emptyQualifier;
        }

        private void setBooleanQualifier(@BooleanQualifier final String booleanQualifier) {
            this.booleanQualifier = booleanQualifier;
        }

        private void setBooleanQualifierExplicit(@BooleanQualifier(false) final String booleanQualifierExplicit) {
            this.booleanQualifierExplicit = booleanQualifierExplicit;
        }

        private void setByteQualifier(@ByteQualifier final String byteQualifier) {
            this.byteQualifier = byteQualifier;
        }

        private void setByteQualifierExplicit(@ByteQualifier(-42) final String byteQualifierExplicit) {
            this.byteQualifierExplicit = byteQualifierExplicit;
        }

        private void setCharQualifier(@CharQualifier final String charQualifier) {
            this.charQualifier = charQualifier;
        }

        private void setCharQualifierExplicit(@CharQualifier('y') final String charQualifierExplicit) {
            this.charQualifierExplicit = charQualifierExplicit;
        }

        private void setFloatQualifier(@FloatQualifier final String floatQualifier) {
            this.floatQualifier = floatQualifier;
        }

        private void setFloatQualifierExplicit(@FloatQualifier(-0.0f) final String floatQualifierExplicit) {
            this.floatQualifierExplicit = floatQualifierExplicit;
        }

        private void setDoubleQualifier(@DoubleQualifier final String doubleQualifier) {
            this.doubleQualifier = doubleQualifier;
        }

        private void setDoubleQualifierExplicit(@DoubleQualifier(-0.0f) final String doubleQualifierExplicit) {
            this.doubleQualifierExplicit = doubleQualifierExplicit;
        }

        private void setIntQualifier(@IntQualifier final String intQualifier) {
            this.intQualifier = intQualifier;
        }

        private void setIntQualifierExplicit(@IntQualifier(-42) final String intQualifierExplicit) {
            this.intQualifierExplicit = intQualifierExplicit;
        }

        private void setLongQualifier(@LongQualifier final String longQualifier) {
            this.longQualifier = longQualifier;
        }

        private void setLongQualifierExplicit(@LongQualifier(-42L) final String longQualifierExplicit) {
            this.longQualifierExplicit = longQualifierExplicit;
        }

        private void setShortQualifier(@ShortQualifier final String shortQualifier) {
            this.shortQualifier = shortQualifier;
        }

        private void setShortQualifierExplicit(@ShortQualifier(-42) final String shortQualifierExplicit) {
            this.shortQualifierExplicit = shortQualifierExplicit;
        }

        private void setStringQualifier(@StringQualifier final String stringQualifier) {
            this.stringQualifier = stringQualifier;
        }

        private void setStringQualifierExplicit(
                @StringQualifier("ExplicitValue") final String stringQualifierExplicit) {
            this.stringQualifierExplicit = stringQualifierExplicit;
        }

        private void setEnumQualifier(@EnumQualifier final String enumQualifier) {
            this.enumQualifier = enumQualifier;
        }

        private void setEnumQualifierExplicit(
                @EnumQualifier(RetentionPolicy.CLASS) final String enumQualifierExplicit) {
            this.enumQualifierExplicit = enumQualifierExplicit;
        }

        private void setClassQualifier(@ClassQualifier final String classQualifier) {
            this.classQualifier = classQualifier;
        }

        private void setClassQualifierExplicit(@ClassQualifier(String.class) final String classQualifierExplicit) {
            this.classQualifierExplicit = classQualifierExplicit;
        }

        private void setAnnotationQualifier(@AnnotationQualifier final String annotationQualifier) {
            this.annotationQualifier = annotationQualifier;
        }

        private void setAnnotationQualifierExplicit(
                @AnnotationQualifier(@IntQualifier(-42)) final String annotationQualifierExplicit) {
            this.annotationQualifierExplicit = annotationQualifierExplicit;
        }

        private void setBooleanArrayQualifier(@BooleanArrayQualifier final String booleanArrayQualifier) {
            this.booleanArrayQualifier = booleanArrayQualifier;
        }

        private void setBooleanArrayQualifierExplicit(
                @BooleanArrayQualifier(false) final String booleanArrayQualifierExplicit) {
            this.booleanArrayQualifierExplicit = booleanArrayQualifierExplicit;
        }

        private void setByteArrayQualifier(@ByteArrayQualifier final String byteArrayQualifier) {
            this.byteArrayQualifier = byteArrayQualifier;
        }

        private void setByteArrayQualifierExplicit(@ByteArrayQualifier(-42) final String byteArrayQualifierExplicit) {
            this.byteArrayQualifierExplicit = byteArrayQualifierExplicit;
        }

        private void setCharArrayQualifier(@CharArrayQualifier final String charArrayQualifier) {
            this.charArrayQualifier = charArrayQualifier;
        }

        private void setCharArrayQualifierExplicit(@CharArrayQualifier('y') final String charArrayQualifierExplicit) {
            this.charArrayQualifierExplicit = charArrayQualifierExplicit;
        }

        private void setFloatArrayQualifier(@FloatArrayQualifier final String floatArrayQualifier) {
            this.floatArrayQualifier = floatArrayQualifier;
        }

        private void setFloatArrayQualifierExplicit(
                @FloatArrayQualifier(-0.0f) final String floatArrayQualifierExplicit) {
            this.floatArrayQualifierExplicit = floatArrayQualifierExplicit;
        }

        private void setDoubleArrayQualifier(@DoubleArrayQualifier final String doubleArrayQualifier) {
            this.doubleArrayQualifier = doubleArrayQualifier;
        }

        private void setDoubleArrayQualifierExplicit(
                @DoubleArrayQualifier(-0.0) final String doubleArrayQualifierExplicit) {
            this.doubleArrayQualifierExplicit = doubleArrayQualifierExplicit;
        }

        private void setIntArrayQualifier(@IntArrayQualifier final String intArrayQualifier) {
            this.intArrayQualifier = intArrayQualifier;
        }

        private void setIntArrayQualifierExplicit(@IntArrayQualifier(-42) final String intArrayQualifierExplicit) {
            this.intArrayQualifierExplicit = intArrayQualifierExplicit;
        }

        private void setLongArrayQualifier(@LongArrayQualifier final String longArrayQualifier) {
            this.longArrayQualifier = longArrayQualifier;
        }

        private void setLongArrayQualifierExplicit(@LongArrayQualifier(-42L) final String longArrayQualifierExplicit) {
            this.longArrayQualifierExplicit = longArrayQualifierExplicit;
        }

        private void setShortArrayQualifier(@ShortArrayQualifier final String shortArrayQualifier) {
            this.shortArrayQualifier = shortArrayQualifier;
        }

        private void setShortArrayQualifierExplicit(
                @ShortArrayQualifier(-42) final String shortArrayQualifierExplicit) {
            this.shortArrayQualifierExplicit = shortArrayQualifierExplicit;
        }

        private void setStringArrayQualifier(@StringArrayQualifier final String stringArrayQualifier) {
            this.stringArrayQualifier = stringArrayQualifier;
        }

        private void setStringArrayQualifierExplicit(
                @StringArrayQualifier("ExplicitValue") final String stringArrayQualifierExplicit) {
            this.stringArrayQualifierExplicit = stringArrayQualifierExplicit;
        }

        private void setEnumArrayQualifier(@EnumArrayQualifier final String enumArrayQualifier) {
            this.enumArrayQualifier = enumArrayQualifier;
        }

        private void setEnumArrayQualifierExplicit(
                @EnumArrayQualifier(RetentionPolicy.CLASS) final String enumArrayQualifierExplicit) {
            this.enumArrayQualifierExplicit = enumArrayQualifierExplicit;
        }

        private void setClassArrayQualifier(@ClassArrayQualifier final String classArrayQualifier) {
            this.classArrayQualifier = classArrayQualifier;
        }

        private void setClassArrayQualifierExplicit(
                @ClassArrayQualifier(String.class) final String classArrayQualifierExplicit) {
            this.classArrayQualifierExplicit = classArrayQualifierExplicit;
        }

        private void setAnnotationArrayQualifier(@AnnotationArrayQualifier final String annotationArrayQualifier) {
            this.annotationArrayQualifier = annotationArrayQualifier;
        }

        private void setAnnotationArrayQualifierExplicit(
                @AnnotationArrayQualifier(@IntQualifier(-42)) final String annotationArrayQualifierExplicit) {
            this.annotationArrayQualifierExplicit = annotationArrayQualifierExplicit;
        }
    }

    public interface Qualifiers {
        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface EmptyQualifier {
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface BooleanQualifier {
            boolean value() default true;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface ByteQualifier {
            byte value() default 42;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface CharQualifier {
            char value() default 'x';
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface FloatQualifier {
            float value() default Float.NaN;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface DoubleQualifier {
            double value() default Double.NaN;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface IntQualifier {
            int value() default 42;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface LongQualifier {
            long value() default 42L;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface ShortQualifier {
            short value() default 42;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface StringQualifier {
            String value() default "Value";
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface EnumQualifier {
            RetentionPolicy value() default RetentionPolicy.RUNTIME;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface ClassQualifier {
            Class<?> value() default Object.class;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface AnnotationQualifier {
            IntQualifier value() default @IntQualifier;
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface BooleanArrayQualifier {
            boolean[] value() default { true };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface ByteArrayQualifier {
            byte[] value() default { 42 };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface CharArrayQualifier {
            char[] value() default { 'x' };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface FloatArrayQualifier {
            float[] value() default { 0.0f };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface DoubleArrayQualifier {
            double[] value() default { 0.0 };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface IntArrayQualifier {
            int[] value() default { 42 };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface LongArrayQualifier {
            long[] value() default { 42L };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface ShortArrayQualifier {
            short[] value() default { 42 };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface StringArrayQualifier {
            String[] value() default { "Value" };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface EnumArrayQualifier {
            RetentionPolicy[] value() default { RetentionPolicy.RUNTIME };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface ClassArrayQualifier {
            Class<?>[] value() default { Object.class };
        }

        @Retention(RetentionPolicy.RUNTIME)
        @Qualifier
        @interface AnnotationArrayQualifier {
            IntQualifier[] value() default { @IntQualifier };
        }
    }
}
