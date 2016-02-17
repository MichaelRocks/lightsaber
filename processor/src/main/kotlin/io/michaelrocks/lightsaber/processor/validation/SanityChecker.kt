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

package io.michaelrocks.lightsaber.processor.validation

import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.commons.AccessFlagStringifier
import io.michaelrocks.lightsaber.processor.descriptors.ClassDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.providableType
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

class SanityChecker(private val processorContext: ProcessorContext) {
  fun performSanityChecks() {
    checkStaticInjectionPoints()
    checkProvidableTargetsAreConstructable()
    checkProviderMethodsReturnValues()
  }

  private fun checkStaticInjectionPoints() {
    for (injectableTarget in processorContext.getInjectableTargets()) {
      for (field in injectableTarget.injectableStaticFields.values) {
        processorContext.reportError("Static field injection is not supported yet: " + field)
      }
      for (method in injectableTarget.injectableStaticMethods.values) {
        processorContext.reportError("Static method injection is not supported yet: " + method)
      }
    }
  }

  private fun checkProvidableTargetsAreConstructable() {
    for (providableTarget in processorContext.getProvidableTargets()) {
      checkProvidableTargetIsConstructable(providableTarget.targetType)
    }
  }

  private fun checkProviderMethodsReturnValues() {
    for (module in processorContext.getModules()) {
      for (provider in module.providers) {
        if (provider.providableType == Type.VOID_TYPE) {
          processorContext.reportError("Provider method returns void: " + provider.providerMethod)
        }
      }
    }
  }

  private fun checkProvidableTargetIsConstructable(providableTarget: Type) {
    val targetClass = processorContext.classRegistry.findClass(providableTarget)
    checkProvidableTargetAccessFlagNotSet(targetClass, Opcodes.ACC_INTERFACE)
    checkProvidableTargetAccessFlagNotSet(targetClass, Opcodes.ACC_ABSTRACT)
    checkProvidableTargetAccessFlagNotSet(targetClass, Opcodes.ACC_ENUM)
    checkProvidableTargetAccessFlagNotSet(targetClass, Opcodes.ACC_ANNOTATION)
  }

  private fun checkProvidableTargetAccessFlagNotSet(targetClass: ClassDescriptor, flag: Int) {
    if ((targetClass.access and flag) != 0) {
      processorContext.reportError(
          "Providable class cannot be ${AccessFlagStringifier.classAccessFlagToString(flag)}: ${targetClass.classType}")
    }
  }
}
