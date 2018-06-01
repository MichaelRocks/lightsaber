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

package io.michaelrocks.lightsaber;

import io.michaelrocks.lightsaber.internal.LightweightHashMap;

import java.util.Map;

public class InjectionDispatcher {
  private static final Map<Class<?>, InjectorConfigurator> injectorConfigurators =
      new LightweightHashMap<Class<?>, InjectorConfigurator>();
  private static final Map<Class<?>, MembersInjector<?>> membersInjectors =
      new LightweightHashMap<Class<?>, MembersInjector<?>>();

  static {
    /*%STATIC_INITIALIZER%*/
  }

  public static void configureInjector(final Object injectorObject, final Object component) {
    if (!(injectorObject instanceof LightsaberInjector)) {
      throw new ConfigurationException("Cannot configure a non-Lightsaber injector: " + injectorObject);
    }

    final LightsaberInjector injector = (LightsaberInjector) injectorObject;
    final InjectorConfigurator injectorConfigurator = injectorConfigurators.get(component.getClass());
    if (injectorConfigurator == null) {
      throw new ConfigurationException("The component hasn't been processed with Lightsaber: " + component);
    }

    injectorConfigurator.configureInjector(injector, component);
  }

  public static void injectMembers(final Object injectorObject, final Object object) {
    final Injector injector = (Injector) injectorObject;
    injectFieldsIntoObject(injector, object, object.getClass());
    injectMethodsIntoObject(injector, object, object.getClass());
  }

  private static void injectFieldsIntoObject(final Injector injector, final Object object, final Class<?> type) {
    if (type == Object.class) {
      return;
    }

    injectFieldsIntoObject(injector, object, type.getSuperclass());
    // noinspection unchecked
    final MembersInjector<Object> membersInjector = (MembersInjector<Object>) membersInjectors.get(type);
    if (membersInjector != null) {
      membersInjector.injectFields(injector, object);
    }
  }

  private static void injectMethodsIntoObject(final Injector injector, final Object object, final Class<?> type) {
    if (type == Object.class) {
      return;
    }

    injectMethodsIntoObject(injector, object, type.getSuperclass());
    // noinspection unchecked
    final MembersInjector<Object> membersInjector = (MembersInjector<Object>) membersInjectors.get(type);
    if (membersInjector != null) {
      membersInjector.injectMethods(injector, object);
    }
  }
}
