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

package io.michaelrocks.lightsaber.processor.analysis

import io.michaelrocks.grip.mirrors.Type
import io.michaelrocks.grip.mirrors.getMethodType
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor

class BridgeRegistry {
  private val reservedMethods = HashSet<MethodDescriptor>()
  private val descriptorToBridgeMap = LinkedHashMap<Any, MethodDescriptor>()

  fun reserveMethod(method: MethodDescriptor) {
    reservedMethods += method
  }

  fun addBridge(field: FieldDescriptor): MethodDescriptor {
    return descriptorToBridgeMap.getOrPut(field) { createBridgeMethod(field) }
  }

  fun addBridge(method: MethodDescriptor): MethodDescriptor {
    return descriptorToBridgeMap.getOrPut(method) { createBridgeMethod(method) }
  }

  fun getBridges(): Collection<Map.Entry<Any, MethodDescriptor>> {
    return descriptorToBridgeMap.entries
  }

  fun getBridge(descriptor: Any): MethodDescriptor? {
    return descriptorToBridgeMap[descriptor]
  }

  fun clear() {
    reservedMethods.clear()
    descriptorToBridgeMap.clear()
  }

  private fun createBridgeMethod(field: FieldDescriptor): MethodDescriptor {
    return reserveBridge(field.name, 0, getMethodType(field.type))
  }

  private fun createBridgeMethod(method: MethodDescriptor): MethodDescriptor {
    return reserveBridge(method.name, 0, method.type)
  }

  private tailrec fun reserveBridge(baseName: String, index: Int, type: Type.Method): MethodDescriptor {
    val bridgeName ="$baseName\$Lightsaber\$$index"
    val bridge = MethodDescriptor(bridgeName, type)
    if (reservedMethods.add(bridge)) {
      return bridge
    }

    return reserveBridge(baseName, index + 1, type)
  }
}
