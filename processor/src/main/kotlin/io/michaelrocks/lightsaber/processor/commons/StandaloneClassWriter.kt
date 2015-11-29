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

package io.michaelrocks.lightsaber.processor.commons

import io.michaelrocks.lightsaber.processor.graph.TypeGraph
import io.michaelrocks.lightsaber.processor.logging.getLogger
import org.apache.commons.collections4.iterators.IteratorIterable
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import java.util.*

class StandaloneClassWriter : ClassWriter {
  private val logger = getLogger()
  private val typeGraph: TypeGraph

  constructor(flags: Int, typeGraph: TypeGraph) : super(flags) {
    this.typeGraph = typeGraph
  }

  constructor(classReader: ClassReader, flags: Int, typeGraph: TypeGraph) : super(classReader, flags) {
    this.typeGraph = typeGraph
  }

  override fun getCommonSuperClass(type1: String, type2: String): String {
    val hierarchy = HashSet<Type>()
    for (type in traverseTypeHierarchy(Type.getObjectType(type1))) {
      hierarchy.add(type)
    }

    for (type in traverseTypeHierarchy(Type.getObjectType(type2))) {
      if (hierarchy.containsRaw(type)) {
        logger.debug("[getCommonSuperClass]: {} & {} = {}", type1, type2, type)
        return type.internalName
      }
    }

    logger.warn("[getCommonSuperClass]: {} & {} = NOT FOUND ", type1, type2)
    return Types.OBJECT_TYPE.internalName
  }

  private fun traverseTypeHierarchy(type: Type): Iterable<Type> {
    return IteratorIterable(TypeHierarchyIterator(typeGraph, type))
  }

  private class TypeHierarchyIterator(private val typeGraph: TypeGraph, private var type: Type?) : Iterator<Type> {
    override fun hasNext(): Boolean = type != null

    override fun next(): Type {
      val result = type ?: throw NoSuchElementException()
      type = if (Types.OBJECT_TYPE == type) null else typeGraph.findSuperType(result)
      return result
    }
  }
}
