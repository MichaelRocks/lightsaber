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

package io.michaelrocks.lightsaber.processor.analysis

import io.michaelrocks.lightsaber.processor.ProcessorClassVisitor
import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.descriptors.InjectionTargetDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import org.apache.commons.lang3.StringUtils
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type

class InjectionTargetAnalyzer(processorContext: ProcessorContext) : ProcessorClassVisitor(processorContext) {
  private lateinit var injectionTargetBuilder: InjectionTargetDescriptor.Builder

  override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?,
      interfaces: Array<String>?) {
    injectionTargetBuilder = InjectionTargetDescriptor.Builder(Type.getObjectType(name))
    super.visit(version, access, name, signature, superName, interfaces)
  }

  override fun visitAnnotation(desc: String, visible: Boolean): AnnotationVisitor? {
    processorContext.findScopeByAnnotationType(Type.getType(desc))?.let { injectionTargetBuilder.setScope(it) }
    return null
  }

  override fun visitMethod(access: Int, name: String, desc: String, signature: String?,
      exceptions: Array<String>?): MethodVisitor {
    if (MethodDescriptor.isDefaultConstructor(name, desc)) {
      injectionTargetBuilder.setHasDefaultConstructor(true)
    }

    return InjectionMethodAnalyzer(processorContext, injectionTargetBuilder, access, name, desc, signature)
  }

  override fun visitField(access: Int, name: String, desc: String, signature: String?, value: Any?): FieldVisitor {
    return InjectionFieldAnalyzer(processorContext, injectionTargetBuilder, access, name, desc, signature)
  }

  override fun visitEnd() {
    val injectionTarget = injectionTargetBuilder.build()
    if (!injectionTarget.injectableFields.isEmpty() || !injectionTarget.injectableMethods.isEmpty()) {
      processorContext.addInjectableTarget(injectionTarget)
    }
    if (injectionTarget.injectableConstructors.size > 1) {
      val separator = System.lineSeparator() + "  "
      val constructors = StringUtils.join(injectionTarget.injectableConstructors, separator)
      reportError("Class has multiple injectable constructors:" + separator + constructors)
    } else if (!injectionTarget.injectableConstructors.isEmpty()) {
      processorContext.addProvidableTarget(injectionTarget)
    }
  }
}
