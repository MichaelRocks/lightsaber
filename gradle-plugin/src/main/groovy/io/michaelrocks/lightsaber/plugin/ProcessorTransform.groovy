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

package io.michaelrocks.lightsaber.plugin

import com.android.build.transform.api.*
import com.google.common.collect.Iterables
import io.michaelrocks.lightsaber.processor.LightsaberParameters
import io.michaelrocks.lightsaber.processor.LightsaberProcessor

public class ProcessorTransform implements AsInputTransform {
    private final boolean verbose

    ProcessorTransform(final boolean verbose) {
        this.verbose = verbose
    }

    @Override
    void transform(final Map<TransformInput, TransformOutput> inputs,
            final Collection<TransformInput> referencedInputs,
            final boolean isIncremental) throws IOException, TransformException, InterruptedException {
        final def parameters = new LightsaberParameters()
        final TransformInput input = Iterables.getOnlyElement(inputs.keySet())
        final TransformOutput output = Iterables.getOnlyElement(inputs.values())
        parameters.classes = Iterables.getOnlyElement(input.files)
        parameters.output = output.getOutFile()
        parameters.libs = referencedInputs.collect { it.files }.flatten()
        parameters.verbose = verbose
        System.out.println("Starting Lightsaber processor: $parameters")
        final def processor = new LightsaberProcessor(parameters)
        try {
            processor.process()
            System.out.println("Lightsaber finished processing")
        } catch (final IOException exception) {
            System.out.println("Lightsaber failed: $exception")
            throw exception
        } catch (final Exception exception) {
            System.out.println("Lightsaber failed: $exception")
            throw new TransformException(exception)
        }
    }

    @Override
    String getName() {
        return "lightsaber"
    }

    @Override
    Set<ScopedContent.ContentType> getInputTypes() {
        return Collections.singleton(ScopedContent.ContentType.CLASSES)
    }

    @Override
    Set<ScopedContent.ContentType> getOutputTypes() {
        return EnumSet.of(ScopedContent.ContentType.CLASSES)
    }

    @Override
    Set<ScopedContent.Scope> getScopes() {
        return EnumSet.of(ScopedContent.Scope.PROJECT)
    }

    @Override
    Set<ScopedContent.Scope> getReferencedScopes() {
        return EnumSet.of(
                ScopedContent.Scope.PROJECT_LOCAL_DEPS,
                ScopedContent.Scope.SUB_PROJECTS,
                ScopedContent.Scope.SUB_PROJECTS_LOCAL_DEPS,
                ScopedContent.Scope.EXTERNAL_LIBRARIES,
                ScopedContent.Scope.PROVIDED_ONLY)
    }

    @Override
    Transform.Type getTransformType() {
        return Transform.Type.AS_INPUT
    }

    @Override
    ScopedContent.Format getOutputFormat() {
        return ScopedContent.Format.SINGLE_FOLDER
    }

    @Override
    Collection<File> getSecondaryFileInputs() {
        return Collections.emptyList()
    }

    @Override
    Collection<File> getSecondaryFileOutputs() {
        return Collections.emptyList()
    }

    @Override
    Collection<File> getSecondaryFolderOutputs() {
        return Collections.emptyList()
    }

    @Override
    Map<String, Object> getParameterInputs() {
        return Collections.emptyMap()
    }

    @Override
    boolean isIncremental() {
        return false
    }
}
