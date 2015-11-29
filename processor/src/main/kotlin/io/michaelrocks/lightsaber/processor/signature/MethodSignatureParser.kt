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
import java.util.*

class MethodSignatureParser(private val processorContext: ProcessorContext) : SignatureVisitor(ASM5) {
  companion object {
    fun parseMethodSignature(processorContext: ProcessorContext, signature: String?, methodType: Type) =
        signature?.let { signature ->
          val signatureReader = SignatureReader(signature)
          val signatureParser = MethodSignatureParser(processorContext)
          signatureReader.accept(signatureParser)
          signatureParser.getMethodSignature()
        } ?: MethodSignature(methodType)
  }

  private val argumentTypeParsers = ArrayList<TypeSignatureParser>()
  private var returnTypeParser: TypeSignatureParser? = null
  private var isValid = true

  private var methodSignature: MethodSignature? = null

  fun getMethodSignature(): MethodSignature? {
    if (isValid && methodSignature == null) {
      methodSignature = createMethodSignature()
      isValid = methodSignature != null
    }
    return methodSignature
  }

  private fun createMethodSignature(): MethodSignature? {
    return returnTypeParser?.let { returnTypeParser ->
      val returnType = returnTypeParser.getTypeSignature()

      val argumentTypes = ArrayList<TypeSignature>(argumentTypeParsers.size)
      for (argumentTypeParser in argumentTypeParsers) {
        val argumentType = argumentTypeParser.getTypeSignature()
        argumentTypes.add(argumentType)
      }

      MethodSignature(returnType, argumentTypes)
    }
  }

  override fun visitParameterType(): SignatureVisitor {
    val argumentTypeParser = TypeSignatureParser(processorContext)
    argumentTypeParsers.add(argumentTypeParser)
    return argumentTypeParser
  }

  override fun visitReturnType(): SignatureVisitor {
    if (returnTypeParser != null) {
      reportError("Return type has already been parsed")
    }
    returnTypeParser = TypeSignatureParser(processorContext)
    return returnTypeParser!!
  }

  // Prohibited callbacks.

  override fun visitFormalTypeParameter(name: String?) {
    reportError("Injectable methods cannot have type parameters")
  }

  private fun reportError(message: String) {
    processorContext.reportError(message)
    isValid = false
  }
}
