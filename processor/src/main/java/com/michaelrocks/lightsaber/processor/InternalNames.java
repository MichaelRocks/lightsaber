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

package com.michaelrocks.lightsaber.processor;

import com.michaelrocks.lightsaber.Module;
import com.michaelrocks.lightsaber.internal.InternalModule;
import com.michaelrocks.lightsaber.internal.LightsaberInjector;
import org.objectweb.asm.Type;

final class InternalNames {
    public static final String CLASS_MODULE = Type.getInternalName(Module.class);
    public static final String CLASS_INTERNAL_MODULE = Type.getInternalName(InternalModule.class);
    public static final String CLASS_LIGHTSABER_INJECTOR = Type.getInternalName(LightsaberInjector.class);

    public static final String METHOD_CONFIGURE_INJECTOR = "configureInjector";
    public static final String METHOD_REGISTER_PROVIDER = "registerProvider";

    private InternalNames() {
    }
}
