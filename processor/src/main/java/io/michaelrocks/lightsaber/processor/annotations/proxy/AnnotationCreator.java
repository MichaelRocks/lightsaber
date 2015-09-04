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

package io.michaelrocks.lightsaber.processor.annotations.proxy;

import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationData;
import io.michaelrocks.lightsaber.processor.annotations.AnnotationDescriptor;
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter;
import io.michaelrocks.lightsaber.processor.descriptors.EnumValueDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor;
import io.michaelrocks.lightsaber.processor.generation.ClassProducer;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnnotationCreator {
    private final ProcessorContext processorContext;
    private final ClassProducer classProducer;
    private final Set<Type> generatedAnnotationProxies = new HashSet<>();

    public AnnotationCreator(final ProcessorContext processorContext, final ClassProducer classProducer) {
        this.processorContext = processorContext;
        this.classProducer = classProducer;
    }

    public void newAnnotation(final GeneratorAdapter generator, final AnnotationData data) {
        final Type annotationProxyType = composeAnnotationProxyType(data.getType());
        final AnnotationDescriptor annotation =
                processorContext.getAnnotationRegistry().findAnnotationByType(data.getType());
        Validate.notNull(annotation);
        generateAnnotationProxyClassIfNecessary(annotation, annotationProxyType);
        constructAnnotationProxy(generator, annotation, data, annotationProxyType);
    }

    private static Type composeAnnotationProxyType(final Type annotationType) {
        return Type.getObjectType(annotationType.getInternalName() + "$Lightsaber$Proxy");
    }

    private void generateAnnotationProxyClassIfNecessary(final AnnotationDescriptor annotation,
            final Type annotationProxyType) {
        if (generatedAnnotationProxies.add(annotationProxyType)) {
            final AnnotationProxyGenerator generator =
                    new AnnotationProxyGenerator(processorContext, annotation, annotationProxyType);
            final byte[] annotationProxyClassData = generator.generate();
            classProducer.produceClass(annotationProxyType.getInternalName(), annotationProxyClassData);
        }
    }

    private void constructAnnotationProxy(final GeneratorAdapter generator, final AnnotationDescriptor annotation,
            final AnnotationData data, final Type annotationProxyType) {
        generator.newInstance(annotationProxyType);
        generator.dup();

        for (final Map.Entry<String, Type> field : annotation.getFields().entrySet()) {
            final String fieldName = field.getKey();
            final Type fieldType = field.getValue();
            final Object fieldValue = data.getValues().get(fieldName);
            Validate.notNull(fieldValue);
            createValue(generator, fieldType, fieldValue);
        }

        final Collection<Type> fieldTypes = annotation.getFields().values();
        final Type[] argumentTypes = fieldTypes.toArray(new Type[fieldTypes.size()]);
        final MethodDescriptor constructor = MethodDescriptor.forConstructor(argumentTypes);
        generator.invokeConstructor(annotationProxyType, constructor);
    }

    private void createValue(final GeneratorAdapter generator, final Type fieldType, final Object value) {
        switch (fieldType.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
            case Type.FLOAT:
            case Type.LONG:
            case Type.DOUBLE:
                // TODO: Check if the value class corresponds to fieldType.
                generator.visitLdcInsn(value);
                break;

            case Type.ARRAY:
                createArray(generator, fieldType, value);
                break;

            case Type.OBJECT:
                createObject(generator, fieldType, value);
                break;

            default:
                throw new IllegalArgumentException("Unsupported annotation field type: " + fieldType);
        }
    }

    private void createArray(final GeneratorAdapter generator, final Type fieldType, final Object value) {
        // TODO: Check if the value class corresponds to fieldType.
        final Type elementType = fieldType.getElementType();
        if (value.getClass().isArray()) {
            generator.newArray(fieldType, Array.getLength(value));
            final Iterable<Object> iterable = IteratorUtils.asIterable(IteratorUtils.arrayIterator(value));
            populateArray(generator, elementType, iterable);
        } else {
            // noinspection unchecked
            final List<Object> list = (List<Object>) value;
            generator.newArray(fieldType, list.size());
            populateArray(generator, elementType, list);
        }
    }

    private void populateArray(final GeneratorAdapter generator, final Type elementType,
            final Iterable<Object> values) {
        int index = 0;
        for (final Object value : values) {
            generator.dup();
            generator.push(index++);
            createValue(generator, elementType, value);
            generator.arrayStore(elementType);
        }
    }

    private void createObject(final GeneratorAdapter generator, final Type fieldType, final Object value) {
        if (value instanceof Type) {
            generator.push((Type) value);
        } else if (value instanceof String) {
            generator.push((String) value);
        } else if (value instanceof EnumValueDescriptor) {
            createEnumValue(generator, (EnumValueDescriptor) value);
        } else if (value instanceof AnnotationData) {
            newAnnotation(generator, (AnnotationData) value);
        }
    }

    private void createEnumValue(final GeneratorAdapter generator, final EnumValueDescriptor value) {
        generator.getStatic(value.getType(), value.getValue(), value.getType());
    }
}
