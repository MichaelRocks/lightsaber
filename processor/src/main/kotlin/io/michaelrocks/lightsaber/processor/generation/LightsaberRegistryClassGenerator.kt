/*
 * Copyright 2016 Michael Rozumyanskiy
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

package io.michaelrocks.lightsaber.processor.generation

import io.michaelrocks.lightsaber.processor.ProcessorContext
import io.michaelrocks.lightsaber.processor.commons.GeneratorAdapter
import io.michaelrocks.lightsaber.processor.commons.StandaloneClassWriter
import io.michaelrocks.lightsaber.processor.commons.Types
import io.michaelrocks.lightsaber.processor.commons.getType
import io.michaelrocks.lightsaber.processor.descriptors.FieldDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.MethodDescriptor
import io.michaelrocks.lightsaber.processor.descriptors.descriptor
import io.michaelrocks.lightsaber.processor.watermark.WatermarkClassVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import java.util.*

class LightsaberRegistryClassGenerator(
    private val classProducer: ClassProducer,
    private val processorContext: ProcessorContext
) {
  companion object {
    private val LIGHTSABER_REGISTRY_TYPE = Type.getObjectType("io/michaelrocks/lightsaber/LightsaberRegistry")
    private val LIST_TYPE = getType<List<*>>()
    private val ARRAY_LIST_TYPE = getType<ArrayList<*>>()
    private val MAP_TYPE = getType<Map<*, *>>()
    private val HASH_MAP_TYPE = getType<HashMap<*, *>>()

    private val PACKAGE_INJECTOR_CONFIGURATORS_FIELD = FieldDescriptor("packageInjectorConfigurators", LIST_TYPE)
    private val INJECTOR_CONFIGURATORS_FIELD = FieldDescriptor("injectorConfigurators", MAP_TYPE)
    private val MEMBERS_INJECTORS_FIELD = FieldDescriptor("membersInjectors", MAP_TYPE)

    private val ARRAY_LIST_CONSTRUCTOR = MethodDescriptor.forConstructor(Type.INT_TYPE)
    private val ADD_METHOD = MethodDescriptor.forMethod("add", Type.BOOLEAN_TYPE, Types.OBJECT_TYPE)
    private val HASH_MAP_CONSTRUCTOR = MethodDescriptor.forConstructor(Type.INT_TYPE)
    private val PUT_METHOD = MethodDescriptor.forMethod("put", Types.OBJECT_TYPE, Types.OBJECT_TYPE, Types.OBJECT_TYPE)

    private val GET_PACKAGE_INJECTOR_CONFIGURATORS_METHOD =
        MethodDescriptor.forMethod("getPackageInjectorConfigurators", LIST_TYPE)
    private val GET_INJECTOR_CONFIGURATORS_METHOD = MethodDescriptor.forMethod("getInjectorConfigurators", MAP_TYPE)
    private val GET_MEMBERS_INJECTORS_METHOD = MethodDescriptor.forMethod("getMembersInjectors", MAP_TYPE)
  }

  fun generate() {
    val classWriter =
        StandaloneClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS, processorContext.classRegistry)
    val classVisitor = WatermarkClassVisitor(classWriter, true)
    classVisitor.visit(
        V1_6,
        ACC_PUBLIC or ACC_SUPER,
        LIGHTSABER_REGISTRY_TYPE.internalName,
        null,
        Types.OBJECT_TYPE.internalName,
        null)

    generateFields(classVisitor)
    generateStaticInitializer(classVisitor)
    generateMethods(classVisitor)

    classVisitor.visitEnd()
    val classBytes = classWriter.toByteArray()
    classProducer.produceClass(LIGHTSABER_REGISTRY_TYPE.internalName, classBytes)
  }

  private fun generateFields(classVisitor: ClassVisitor) {
    generateInjectorConfiguratorsField(classVisitor)
    generateMembersInjectorsField(classVisitor)
    generatePackageModulesField(classVisitor)
  }

  private fun generateInjectorConfiguratorsField(classVisitor: ClassVisitor) {
    val fieldVisitor = classVisitor.visitField(
        ACC_PRIVATE or ACC_STATIC or ACC_FINAL,
        INJECTOR_CONFIGURATORS_FIELD.name,
        INJECTOR_CONFIGURATORS_FIELD.descriptor,
        null,
        null)
    fieldVisitor.visitEnd()
  }

  private fun generateMembersInjectorsField(classVisitor: ClassVisitor) {
    val fieldVisitor = classVisitor.visitField(
        ACC_PRIVATE or ACC_STATIC or ACC_FINAL,
        MEMBERS_INJECTORS_FIELD.name,
        MEMBERS_INJECTORS_FIELD.descriptor,
        null,
        null)
    fieldVisitor.visitEnd()
  }

  private fun generatePackageModulesField(classVisitor: ClassVisitor) {
    val fieldVisitor = classVisitor.visitField(
        ACC_PRIVATE or ACC_STATIC or ACC_FINAL,
        PACKAGE_INJECTOR_CONFIGURATORS_FIELD.name,
        PACKAGE_INJECTOR_CONFIGURATORS_FIELD.descriptor,
        null,
        null)
    fieldVisitor.visitEnd()
  }

  private fun generateStaticInitializer(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_STATIC, MethodDescriptor.forStaticInitializer())
    generator.visitCode()

    populatePackageInjectorConfiguratorsMethod(generator)
    populateInjectorConfigurators(generator)
    populateMembersInjectors(generator)

    generator.returnValue()
    generator.endMethod()
  }

  private fun populatePackageInjectorConfiguratorsMethod(generator: GeneratorAdapter) {
    val configurators = processorContext.getPackageInjectorConfigurators()
    generator.newInstance(ARRAY_LIST_TYPE)
    generator.dup()
    generator.push(configurators.size)
    generator.invokeConstructor(ARRAY_LIST_TYPE, ARRAY_LIST_CONSTRUCTOR)

    for (configurator in configurators) {
      generator.dup()
      generator.newInstance(configurator.type)
      generator.dup()
      generator.invokeConstructor(configurator.type, MethodDescriptor.forDefaultConstructor())
      generator.invokeInterface(LIST_TYPE, ADD_METHOD)
      generator.pop()
    }

    generator.putStatic(LIGHTSABER_REGISTRY_TYPE, PACKAGE_INJECTOR_CONFIGURATORS_FIELD)
  }

  private fun populateInjectorConfigurators(generator: GeneratorAdapter) {
    val configurators = processorContext.getInjectorConfigurators()
    generator.newInstance(HASH_MAP_TYPE)
    generator.dup()
    generator.push(configurators.size)
    generator.invokeConstructor(HASH_MAP_TYPE, HASH_MAP_CONSTRUCTOR)

    for (configurator in configurators) {
      generator.dup()
      generator.push(configurator.module.type)
      generator.newInstance(configurator.type)
      generator.dup()
      generator.invokeConstructor(configurator.type, MethodDescriptor.forDefaultConstructor())
      generator.invokeInterface(MAP_TYPE, PUT_METHOD)
      generator.pop()
    }

    generator.putStatic(LIGHTSABER_REGISTRY_TYPE, INJECTOR_CONFIGURATORS_FIELD)
  }

  private fun populateMembersInjectors(generator: GeneratorAdapter) {
    val injectors = processorContext.getMembersInjectors()
    generator.newInstance(HASH_MAP_TYPE)
    generator.dup()
    generator.push(injectors.size)
    generator.invokeConstructor(HASH_MAP_TYPE, HASH_MAP_CONSTRUCTOR)

    for (injector in injectors) {
      generator.dup()
      generator.push(injector.target.type)
      generator.newInstance(injector.type)
      generator.dup()
      generator.invokeConstructor(injector.type, MethodDescriptor.forDefaultConstructor())
      generator.invokeInterface(MAP_TYPE, PUT_METHOD)
      generator.pop()
    }

    generator.putStatic(LIGHTSABER_REGISTRY_TYPE, MEMBERS_INJECTORS_FIELD)
  }

  private fun generateMethods(classVisitor: ClassVisitor) {
    generateGetInjectorGonfiguratorsMethod(classVisitor)
    generateGetMembersInjectorsMethod(classVisitor)
    generateGetPackageModulesMethod(classVisitor)
  }

  private fun generateGetInjectorGonfiguratorsMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC or ACC_STATIC, GET_INJECTOR_CONFIGURATORS_METHOD)
    generator.visitCode()
    generator.getStatic(LIGHTSABER_REGISTRY_TYPE, INJECTOR_CONFIGURATORS_FIELD)
    generator.returnValue()
    generator.endMethod()
  }

  private fun generateGetMembersInjectorsMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC or ACC_STATIC, GET_MEMBERS_INJECTORS_METHOD)
    generator.visitCode()
    generator.getStatic(LIGHTSABER_REGISTRY_TYPE, MEMBERS_INJECTORS_FIELD)
    generator.returnValue()
    generator.endMethod()
  }

  private fun generateGetPackageModulesMethod(classVisitor: ClassVisitor) {
    val generator = GeneratorAdapter(classVisitor, ACC_PUBLIC or ACC_STATIC, GET_PACKAGE_INJECTOR_CONFIGURATORS_METHOD)
    generator.visitCode()
    generator.getStatic(LIGHTSABER_REGISTRY_TYPE, PACKAGE_INJECTOR_CONFIGURATORS_FIELD)
    generator.returnValue()
    generator.endMethod()
  }
}
