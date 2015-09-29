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

package io.michaelrocks.lightsaber.processor.validation;

import io.michaelrocks.lightsaber.processor.ProcessorContext;
import io.michaelrocks.lightsaber.processor.commons.AccessFlagStringifier;
import io.michaelrocks.lightsaber.processor.descriptors.ClassDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ModuleDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.ProviderDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedFieldDescriptor;
import io.michaelrocks.lightsaber.processor.descriptors.QualifiedMethodDescriptor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class SanityChecker {
    private final ProcessorContext processorContext;

    public SanityChecker(final ProcessorContext processorContext) {
        this.processorContext = processorContext;
    }

    public void performSanityChecks() {
        checkStaticInjectionPoints();
        checkProvidableTargetsAreConstructable();
        checkProviderMethodsReturnValues();
    }

    private void checkStaticInjectionPoints() {
        for (final InjectionTargetDescriptor injectableTarget : processorContext.getInjectableTargets()) {
            for (final QualifiedFieldDescriptor field : injectableTarget.getInjectableStaticFields().values()) {
                processorContext.reportError("Static field injection is not supported yet: " + field);
            }
            for (final QualifiedMethodDescriptor method : injectableTarget.getInjectableStaticMethods().values()) {
                processorContext.reportError("Static method injection is not supported yet: " + method);
            }
        }
    }

    private void checkProvidableTargetsAreConstructable() {
        for (final InjectionTargetDescriptor providableTarget : processorContext.getProvidableTargets()) {
            checkProvidableTargetIsConstructable(providableTarget.getTargetType());
        }
    }

    private void checkProviderMethodsReturnValues() {
        for (final ModuleDescriptor module : processorContext.getModules()) {
            for (final ProviderDescriptor provider : module.getProviders()) {
                if (provider.getProvidableType().equals(Type.VOID_TYPE)) {
                    processorContext.reportError("Provider method returns void: " + provider.getProviderMethod());
                }
            }
        }
    }

    private void checkProvidableTargetIsConstructable(final Type providableTarget) {
        final ClassDescriptor targetClass = processorContext.getTypeGraph().findClassDescriptor(providableTarget);
        checkProvidableTargetAccessFlagNotSet(targetClass, Opcodes.ACC_INTERFACE);
        checkProvidableTargetAccessFlagNotSet(targetClass, Opcodes.ACC_ABSTRACT);
        checkProvidableTargetAccessFlagNotSet(targetClass, Opcodes.ACC_ENUM);
        checkProvidableTargetAccessFlagNotSet(targetClass, Opcodes.ACC_ANNOTATION);
    }

    private void checkProvidableTargetAccessFlagNotSet(final ClassDescriptor targetClass, final int flag) {
        if ((targetClass.getAccess() & flag) != 0) {
            processorContext.reportError(
                    "Providable class cannot be " + AccessFlagStringifier.classAccessFlagToString(flag)
                            + ": " + targetClass.getClassType());
        }
    }
}
