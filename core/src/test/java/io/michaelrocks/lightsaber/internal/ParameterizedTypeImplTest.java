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

package io.michaelrocks.lightsaber.internal;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ParameterizedTypeImplTest {
  @Test
  public void testToStringWithSingleTypeArgument() {
    final ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(
        null,
        List.class,
        NamedType.create("Argument1")
    );

    assertEquals("java.util.List<Argument1>", parameterizedType.toString());
  }

  @Test
  public void testToStringWithMultipleTypeArgument() {
    final ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(
        null,
        Map.class,
        NamedType.create("Argument1"),
        NamedType.create("Argument2")
    );

    assertEquals("java.util.Map<Argument1, Argument2>", parameterizedType.toString());
  }

  @Test
  public void testToStringWithOwner() {
    final ParameterizedTypeImpl parameterizedType = new ParameterizedTypeImpl(
        Map.class,
        Map.Entry.class,
        NamedType.create("Argument1"),
        NamedType.create("Argument2")
    );

    assertEquals("java.util.Map.Entry<Argument1, Argument2>", parameterizedType.toString());
  }

}
