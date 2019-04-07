/*
 * Copyright 2018 Michael Rozumyanskiy
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

import io.michaelrocks.grip.ClassRegistry
import io.michaelrocks.grip.mirrors.ClassMirror
import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.isStatic
import io.michaelrocks.lightsaber.processor.ErrorReporter
import io.michaelrocks.lightsaber.processor.commons.AccessFlagStringifier
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.rawType
import io.michaelrocks.lightsaber.processor.model.InjectionContext
import io.michaelrocks.lightsaber.processor.model.InjectionPoint
import io.michaelrocks.lightsaber.processor.model.isConstructorProvider
import org.objectweb.asm.Opcodes

class SanityChecker(
    private val classRegistry: ClassRegistry,
    private val errorReporter: ErrorReporter
) {
  fun performSanityChecks(context: InjectionContext) {
    checkStaticInjectionPoints(context)
    checkProvidableTargetsAreConstructable(context)
    checkProviderMethodsReturnValues(context)
    checkSubcomponentsAreComponents(context)
    checkComponentsAndModulesExtendObject(context)
  }

  private fun checkStaticInjectionPoints(context: InjectionContext) {
    for (injectableTarget in context.injectableTargets) {
      injectableTarget.injectionPoints.forEach { injectionPoint ->
        when (injectionPoint) {
          is InjectionPoint.Field ->
              if (injectionPoint.field.isStatic) {
                errorReporter.reportError("Static field injection is not supported yet: " + injectionPoint.field)
              }
          is InjectionPoint.Method ->
            if (injectionPoint.method.isStatic) {
              errorReporter.reportError("Static method injection is not supported yet: " + injectionPoint.method)
            }
        }
      }
    }
  }

  private fun checkProvidableTargetsAreConstructable(context: InjectionContext) {
    for (providableTarget in context.providableTargets) {
      checkProvidableTargetIsConstructable(providableTarget.type)
    }
  }

  private fun checkProviderMethodsReturnValues(context: InjectionContext) {
    for (component in context.components) {
      for (module in component.modules) {
        for (provider in module.providers) {
          if (!provider.isConstructorProvider && provider.dependency.type.rawType == Type.Primitive.Void) {
            errorReporter.reportError("Provider returns void: " + provider.provisionPoint)
          }
        }
      }
    }
  }

  private fun checkProvidableTargetIsConstructable(providableTarget: Type.Object) {
    val mirror = classRegistry.getClassMirror(providableTarget)
    checkProvidableTargetAccessFlagNotSet(mirror, Opcodes.ACC_INTERFACE)
    checkProvidableTargetAccessFlagNotSet(mirror, Opcodes.ACC_ABSTRACT)
    checkProvidableTargetAccessFlagNotSet(mirror, Opcodes.ACC_ENUM)
    checkProvidableTargetAccessFlagNotSet(mirror, Opcodes.ACC_ANNOTATION)
  }

  private fun checkProvidableTargetAccessFlagNotSet(mirror: ClassMirror, flag: Int) {
    if ((mirror.access and flag) != 0) {
      errorReporter.reportError(
          "Providable class cannot be ${AccessFlagStringifier.classAccessFlagToString(flag)}: ${mirror.type}")
    }
  }

  private fun checkSubcomponentsAreComponents(context: InjectionContext) {
    for (component in context.components) {
      for (subcomponent in component.subcomponents) {
        if (context.findComponentByType(subcomponent) == null) {
          errorReporter.reportError("Subcomponent is not annotated with @Component: ${subcomponent.className}")
        }
      }
    }
  }

  private fun checkComponentsAndModulesExtendObject(context: InjectionContext) {
    for (component in context.components) {
      checkClassExtendsObject(component.type)
      for (module in component.modules) {
        checkClassExtendsObject(module.type)
      }
    }
  }

  private fun checkClassExtendsObject(type: Type.Object) {
    val mirror = classRegistry.getClassMirror(type)
    if (mirror.superType != Types.OBJECT_TYPE) {
      errorReporter.reportError("${type.className} has a super type of ${mirror.type.className} instead of Object")
    }
  }
}
