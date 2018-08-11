package io.michaelrocks.lightsaber.processor.commons

import io.michaelrocks.grip.mirrors.signature.GenericType

fun GenericType.boxed(): GenericType {
  if (this !is GenericType.Raw) {
    return this
  }

  val boxedType = type.boxed()
  if (boxedType === type) {
    return this
  }

  return GenericType.Raw(boxedType)
}
