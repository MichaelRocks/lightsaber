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

package io.michaelrocks.lightsaber.processor.signature

import io.michaelrocks.lightsaber.processor.ProcessorContext
import org.objectweb.asm.Opcodes.ASM5
import org.objectweb.asm.Type
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureVisitor

class TypeSignatureParser(private val processorContext: ProcessorContext) : SignatureVisitor(ASM5) {
  companion object {
    fun parseTypeSignature(processorContext: ProcessorContext, signature: String?, fieldType: Type) =
        signature?.let {
          val signatureReader = SignatureReader(signature)
          val signatureParser = TypeSignatureParser(processorContext)
          signatureReader.acceptType(signatureParser)
          signatureParser.getTypeSignature()
        } ?: TypeSignature(fieldType)
  }

  private var classType: Type? = null
  private var classTypeParameter: Type? = null
  private var isValid = true

  private var typeSignature: TypeSignature? = null

  fun getTypeSignature(): TypeSignature {
    if (isValid && typeSignature == null) {
      typeSignature = TypeSignature(classType!!, classTypeParameter)
    }
    return typeSignature!!
  }

  override fun visitClassType(name: String?) {
    visitType(Type.getObjectType(name))
  }

  override fun visitBaseType(descriptor: Char) {
    visitType(Type.getType(Character.toString(descriptor)))
  }

  private fun visitType(type: Type) {
    if (classType == null) {
      classType = type
    } else if (classTypeParameter == null) {
      classTypeParameter = type
    } else {
      reportError("$classType has multiple type arguments")
    }
  }

  override fun visitTypeArgument(wildcard: Char): SignatureVisitor {
    if (wildcard == SignatureVisitor.INSTANCEOF) {
      return this
    }

    reportError("Injectable type cannot have wildcards in its signature")
    return this
  }

  // Prohibited callbacks.

  override fun visitInnerClassType(name: String?) {
    reportError("Injectable type cannot have an inner class type in its signature")
  }

  override fun visitArrayType(): SignatureVisitor {
    reportError("Injectable type cannot be an array")
    return this
  }

  override fun visitTypeVariable(name: String?) {
    reportError("Injectable type cannot have type variables in its signature")
  }

  override fun visitTypeArgument() {
    reportError("Injectable type cannot have unbounded type arguments in its signature")
  }

  private fun reportError(message: String) {
    processorContext.reportError(message)
    isValid = false
  }
}
