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

package io.michaelrocks.lightsaber.processor.descriptors;

import io.michaelrocks.lightsaber.processor.commons.Types;
import io.michaelrocks.lightsaber.processor.signature.TypeSignature;
import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PackageInvaderDescriptor {
    private static final String CLASS_NAME = "Lightsaber$$PackageInvader";
    private static final String FIELD_PREFIX = "class";

    private final Type type;
    private final String packageName;
    private final Map<Type, FieldDescriptor> classFields;

    private PackageInvaderDescriptor(final Builder builder) {
        this.type = builder.type;
        this.packageName = builder.packageName;
        this.classFields = Collections.unmodifiableMap(new HashMap<>(builder.classFields));
    }

    public Type getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }

    public FieldDescriptor getClassField(final Type type) {
        return classFields.get(type);
    }

    public Map<Type, FieldDescriptor> getClassFields() {
        return classFields;
    }

    public static class Builder {
        private final Type type;
        private final String packageName;
        private final Set<Type> classes = new HashSet<>();
        private final Map<Type, FieldDescriptor> classFields = new HashMap<>();

        public Builder(final String packageName) {
            this.packageName = packageName;
            this.type = Type.getObjectType(packageName + '/' + CLASS_NAME);
        }

        public Builder addClass(final Type type) {
            classes.add(type);
            return this;
        }

        public PackageInvaderDescriptor build() {
            addClassFields();
            return new PackageInvaderDescriptor(this);
        }

        private void addClassFields() {
            classFields.clear();
            for (final Type type : classes) {
                addClassField(type);
            }
        }

        private void addClassField(final Type type) {
            final String fieldName = FIELD_PREFIX + classFields.size();
            final TypeSignature fieldType = new TypeSignature(Types.CLASS_TYPE, type);
            final FieldDescriptor field = new FieldDescriptor(fieldName, fieldType);
            classFields.put(type, field);
        }
    }
}
