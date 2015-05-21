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

import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ClassDescriptor {
    private final Type classType;
    private final Type superType;
    private final Collection<Type> interfaceTypes;

    public ClassDescriptor(final String name, final String superName, final String[] interfaces) {
        this(Type.getObjectType(name),
                superName == null ? null : Type.getObjectType(superName),
                getInterfacesTypes(interfaces));
    }

    public ClassDescriptor(final Type classType, final Type superType, final Collection<Type> interfaceTypes) {
        this.classType = classType;
        this.superType = superType;
        this.interfaceTypes = Collections.unmodifiableCollection(new ArrayList<>(interfaceTypes));
    }

    private static List<Type> getInterfacesTypes(final String[] interfaces) {
        final List<Type> interfacesTypes = new ArrayList<>(interfaces.length);
        for (final String interfaceName : interfaces) {
            interfacesTypes.add(Type.getObjectType(interfaceName));
        }
        return interfacesTypes;
    }

    public Type getClassType() {
        return classType;
    }

    public Type getSuperType() {
        return superType;
    }

    public Collection<Type> getInterfaceTypes() {
        return interfaceTypes;
    }
}
