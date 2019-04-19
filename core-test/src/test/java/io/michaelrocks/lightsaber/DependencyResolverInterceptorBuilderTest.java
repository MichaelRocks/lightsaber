/*
 * Copyright 2019 Michael Rozumyanskiy
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

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DependencyResolverInterceptorBuilderTest {
  @Test
  public void testAddInstanceForClass() {
    @SuppressWarnings("unchecked")
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, "Instance")
        .build();

    assertInstanceIntercepted(interceptor, String.class, "Instance");
    assertInstanceIntercepted(interceptor, Key.of(String.class), "Instance");
    assertInstanceNotIntercepted(interceptor, Object.class);
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class));
  }

  @Test
  public void testAddInstanceForType() {
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), Collections.singletonList("Instance"))
        .build();

    assertInstanceIntercepted(interceptor, new TypeToken<List<String>>() {}.getType(), Collections.singletonList("Instance"));
    assertInstanceIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType()), Collections.singletonList("Instance"));
    assertInstanceNotIntercepted(interceptor, new TypeToken<List<Object>>() {}.getType());
    assertInstanceNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType()));
  }

  @Test
  public void testAddInstanceForKey() {
    @SuppressWarnings("unchecked")
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addInstanceForKey(Key.of(String.class), "Instance")
        .build();

    assertInstanceIntercepted(interceptor, Key.of(String.class), "Instance");
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class));
  }

  @Test
  public void testAddInstanceForClassWithAnnotation() {
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, "Instance1")
        .addInstanceForClass(String.class, Annotated.class, "Instance2")
        .addInstanceForClass(String.class, createAnnotation("explicit"), "Instance3")
        .build();

    assertInstanceIntercepted(interceptor, String.class, "Instance1");
    assertInstanceIntercepted(interceptor, Key.of(String.class), "Instance1");
    assertInstanceIntercepted(interceptor, Key.of(String.class, createAnnotation()), "Instance2");
    assertInstanceIntercepted(interceptor, Key.of(String.class, createAnnotation("explicit")), "Instance3");
    assertInstanceNotIntercepted(interceptor, Object.class);
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class));
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class, createAnnotation()));
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class, createAnnotation("explicit")));
  }

  @Test
  public void testAddInstanceForTypeWithAnnotation() {
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), Collections.singletonList("Instance1"))
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, Collections.singletonList("Instance2"))
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), Collections.singletonList("Instance3"))
        .build();

    assertInstanceIntercepted(interceptor, new TypeToken<List<String>>() {}.getType(), Collections.singletonList("Instance1"));
    assertInstanceIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType()), Collections.singletonList("Instance1"));
    assertInstanceIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()),
        Collections.singletonList("Instance2"));
    assertInstanceIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")),
        Collections.singletonList("Instance3"));
    assertInstanceNotIntercepted(interceptor, new TypeToken<List<Object>>() {}.getType());
    assertInstanceNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType()));
    assertInstanceNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation()));
    assertInstanceNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation("explicit")));
  }

  @Test
  public void testAddInstanceForKeyWithAnnotation() {
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addInstanceForKey(Key.of(String.class), "Instance1")
        .addInstanceForKey(Key.of(String.class, createAnnotation()), "Instance2")
        .addInstanceForKey(Key.of(String.class, createAnnotation("explicit")), "Instance3")
        .build();

    assertInstanceIntercepted(interceptor, String.class, "Instance1");
    assertInstanceIntercepted(interceptor, Key.of(String.class), "Instance1");
    assertInstanceIntercepted(interceptor, Key.of(String.class, createAnnotation()), "Instance2");
    assertInstanceIntercepted(interceptor, Key.of(String.class, createAnnotation("explicit")), "Instance3");
    assertInstanceNotIntercepted(interceptor, Object.class);
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class));
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class, createAnnotation()));
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class, createAnnotation("explicit")));
  }

  @Test
  public void testAddInstanceForClassTypeAndKeyWithAnnotation() {
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, "Instance1")
        .addInstanceForClass(String.class, Annotated.class, "Instance2")
        .addInstanceForKey(Key.of(String.class, createAnnotation("explicit")), "Instance3")
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), "Instance4")
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, "Instance5")
        .addInstanceForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), "Instance6")
        .build();

    assertInstanceIntercepted(interceptor, String.class, "Instance1");
    assertInstanceIntercepted(interceptor, Key.of(String.class), "Instance1");
    assertInstanceIntercepted(interceptor, Key.of(String.class, createAnnotation()), "Instance2");
    assertInstanceIntercepted(interceptor, Key.of(String.class, createAnnotation("explicit")), "Instance3");
    assertInstanceIntercepted(interceptor, new TypeToken<List<String>>() {}.getType(), "Instance4");
    assertInstanceIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType()), "Instance4");
    assertInstanceIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()), "Instance5");
    assertInstanceIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), "Instance6");
    assertInstanceNotIntercepted(interceptor, Object.class);
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class));
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class, createAnnotation()));
    assertInstanceNotIntercepted(interceptor, Key.of(Object.class, createAnnotation("explicit")));
    assertInstanceNotIntercepted(interceptor, new TypeToken<List<Object>>() {}.getType());
    assertInstanceNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType()));
    assertInstanceNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation()));
    assertInstanceNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation("explicit")));
  }

  @Test
  public void testAddProviderForClass() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addProviderForClass(String.class, provider)
        .build();

    assertProviderIntercepted(interceptor, String.class, provider);
    assertProviderIntercepted(interceptor, Key.of(String.class), provider);
    assertProviderNotIntercepted(interceptor, Object.class);
    assertProviderNotIntercepted(interceptor, Key.of(Object.class));
  }

  @Test
  public void testAddProviderForType() {
    @SuppressWarnings("unchecked")
    final Provider<List<String>> provider = mock(Provider.class);
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider)
        .build();

    assertProviderIntercepted(interceptor, new TypeToken<List<String>>() {}.getType(), provider);
    assertProviderIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType()), provider);
    assertProviderNotIntercepted(interceptor, new TypeToken<List<Object>>() {}.getType());
    assertProviderNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType()));
  }

  @Test
  public void testAddProviderForKey() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addProviderForKey(Key.of(String.class), provider)
        .build();

    assertProviderIntercepted(interceptor, Key.of(String.class), provider);
    assertProviderNotIntercepted(interceptor, Key.of(Object.class));
  }

  @Test
  public void testAddProviderForClassWithAnnotation() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider1 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider2 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider3 = mock(Provider.class);

    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addProviderForClass(String.class, provider1)
        .addProviderForClass(String.class, Annotated.class, provider2)
        .addProviderForClass(String.class, createAnnotation("explicit"), provider3)
        .build();

    assertProviderIntercepted(interceptor, String.class, provider1);
    assertProviderIntercepted(interceptor, Key.of(String.class), provider1);
    assertProviderIntercepted(interceptor, Key.of(String.class, createAnnotation()), provider2);
    assertProviderIntercepted(interceptor, Key.of(String.class, createAnnotation("explicit")), provider3);
    assertProviderNotIntercepted(interceptor, Object.class);
    assertProviderNotIntercepted(interceptor, Key.of(Object.class));
    assertProviderNotIntercepted(interceptor, Key.of(Object.class, createAnnotation()));
    assertProviderNotIntercepted(interceptor, Key.of(Object.class, createAnnotation("explicit")));
  }

  @Test
  public void testAddProviderForTypeWithAnnotation() {
    @SuppressWarnings("unchecked")
    final Provider<List<String>> provider1 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<List<String>> provider2 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<List<String>> provider3 = mock(Provider.class);

    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider1)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider2)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), provider3)
        .build();

    assertProviderIntercepted(interceptor, new TypeToken<List<String>>() {}.getType(), provider1);
    assertProviderIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType()), provider1);
    assertProviderIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()), provider2);
    assertProviderIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), provider3);
    assertProviderNotIntercepted(interceptor, new TypeToken<List<Object>>() {}.getType());
    assertProviderNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType()));
    assertProviderNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation()));
    assertProviderNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation("explicit")));
  }

  @Test
  public void testAddProviderForKeyWithAnnotation() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider1 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider2 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider3 = mock(Provider.class);

    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addProviderForKey(Key.of(String.class), provider1)
        .addProviderForKey(Key.of(String.class, createAnnotation()), provider2)
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider3)
        .build();

    assertProviderIntercepted(interceptor, String.class, provider1);
    assertProviderIntercepted(interceptor, Key.of(String.class), provider1);
    assertProviderIntercepted(interceptor, Key.of(String.class, createAnnotation()), provider2);
    assertProviderIntercepted(interceptor, Key.of(String.class, createAnnotation("explicit")), provider3);
    assertProviderNotIntercepted(interceptor, Object.class);
    assertProviderNotIntercepted(interceptor, Key.of(Object.class));
    assertProviderNotIntercepted(interceptor, Key.of(Object.class, createAnnotation()));
    assertProviderNotIntercepted(interceptor, Key.of(Object.class, createAnnotation("explicit")));
  }

  @Test
  public void testAddProviderForClassTypeAndKeyWithAnnotation() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider1 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider2 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider3 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider4 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider5 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider6 = mock(Provider.class);

    final DependencyResolverInterceptor interceptor = new DependencyResolverInterceptorBuilder()
        .addProviderForClass(String.class, provider1)
        .addProviderForClass(String.class, Annotated.class, provider2)
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider3)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider4)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider5)
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), provider6)
        .build();

    assertProviderIntercepted(interceptor, String.class, provider1);
    assertProviderIntercepted(interceptor, Key.of(String.class), provider1);
    assertProviderIntercepted(interceptor, Key.of(String.class, createAnnotation()), provider2);
    assertProviderIntercepted(interceptor, Key.of(String.class, createAnnotation("explicit")), provider3);
    assertProviderIntercepted(interceptor, new TypeToken<List<String>>() {}.getType(), provider4);
    assertProviderIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType()), provider4);
    assertProviderIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()), provider5);
    assertProviderIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), provider6);
    assertProviderNotIntercepted(interceptor, Object.class);
    assertProviderNotIntercepted(interceptor, Key.of(Object.class));
    assertProviderNotIntercepted(interceptor, Key.of(Object.class, createAnnotation()));
    assertProviderNotIntercepted(interceptor, Key.of(Object.class, createAnnotation("explicit")));
    assertProviderNotIntercepted(interceptor, new TypeToken<List<Object>>() {}.getType());
    assertProviderNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType()));
    assertProviderNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation()));
    assertProviderNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation("explicit")));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForSameClassThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, "Instance")
        .addInstanceForClass(String.class, "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForSameClassWithImplicitAnnotationThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, Annotated.class, "Instance")
        .addInstanceForClass(String.class, Annotated.class, "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForSameClassWithExplicitAnnotationThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, createAnnotation("explicit"), "Instance")
        .addInstanceForClass(String.class, createAnnotation("explicit"), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForSameTypeThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), "Instance")
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForSameTypeWithImplicitAnnotationThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, "Instance")
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForSameTypeWithExplicitAnnotationThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), "Instance")
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForSameKeyThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForKey(Key.of(String.class), "Instance")
        .addInstanceForKey(Key.of(String.class), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForSameKeyWithExplicitAnnotationThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForKey(Key.of(String.class, createAnnotation("explicit")), "Instance")
        .addInstanceForKey(Key.of(String.class, createAnnotation("explicit")), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForClassAndEqualKeyThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, "Instance")
        .addInstanceForKey(Key.of(String.class), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForClassAndEqualKeyWithImplicitAnnotationThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, Annotated.class, "Instance")
        .addInstanceForKey(Key.of(String.class, createAnnotation()), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForClassAndEqualKeyWithExplicitAnnotationThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, createAnnotation("explicit"), "Instance")
        .addInstanceForKey(Key.of(String.class, createAnnotation("explicit")), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForTypeAndEqualKeyThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), "Instance")
        .addInstanceForKey(Key.of(new TypeToken<List<String>>() {}.getType()), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForTypeAndEqualKeyWithImplicitAnnotationThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, "Instance")
        .addInstanceForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceForTypeAndEqualKeyWithExplicitAnnotationThrowsException() {
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), "Instance")
        .addInstanceForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), "Instance")
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameClassThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForClass(String.class, provider)
        .addProviderForClass(String.class, provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameClassWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForClass(String.class, Annotated.class, provider)
        .addProviderForClass(String.class, Annotated.class, provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameClassWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForClass(String.class, createAnnotation("explicit"), provider)
        .addProviderForClass(String.class, createAnnotation("explicit"), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameTypeThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameTypeWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameTypeWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), provider)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameKeyThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForKey(Key.of(String.class), provider)
        .addProviderForKey(Key.of(String.class), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameKeyWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider)
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForClassAndEqualKeyThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForClass(String.class, provider)
        .addProviderForKey(Key.of(String.class), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForClassAndEqualKeyWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForClass(String.class, Annotated.class, provider)
        .addProviderForKey(Key.of(String.class, createAnnotation()), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForClassAndEqualKeyWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForClass(String.class, createAnnotation("explicit"), provider)
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForTypeAndEqualKeyThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider)
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType()), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForTypeAndEqualKeyWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider)
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForTypeAndEqualKeyWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), provider)
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), provider)
        .build();
  }


  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForSameClassThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, "Instance")
        .addProviderForClass(String.class, provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForSameClassWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, Annotated.class, "Instance")
        .addProviderForClass(String.class, Annotated.class, provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForSameClassWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, createAnnotation("explicit"), "Instance")
        .addProviderForClass(String.class, createAnnotation("explicit"), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForSameTypeThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), "Instance")
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForSameTypeWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, "Instance")
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForSameTypeWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), "Instance")
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForSameKeyThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForKey(Key.of(String.class), "Instance")
        .addProviderForKey(Key.of(String.class), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForSameKeyWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForKey(Key.of(String.class, createAnnotation("explicit")), "Instance")
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForClassAndEqualKeyThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, "Instance")
        .addProviderForKey(Key.of(String.class), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForClassAndEqualKeyWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, Annotated.class, "Instance")
        .addProviderForKey(Key.of(String.class, createAnnotation()), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForClassAndEqualKeyWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForClass(String.class, createAnnotation("explicit"), "Instance")
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForTypeAndEqualKeyThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), "Instance")
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType()), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForTypeAndEqualKeyWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, "Instance")
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddInstanceAndProviderForTypeAndEqualKeyWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new DependencyResolverInterceptorBuilder()
        .addInstanceForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), "Instance")
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), provider)
        .build();
  }

  private Annotated createAnnotation() {
    return new AnnotationBuilder<Annotated>(Annotated.class).build();
  }

  @SuppressWarnings("SameParameterValue")
  private Annotated createAnnotation(final String value) {
    return new AnnotationBuilder<Annotated>(Annotated.class).addMember("value", value).build();
  }

  private void assertInstanceIntercepted(final DependencyResolverInterceptor interceptor, final Object key, final Object expectedInstance) {
    final Injector injector = mock(Injector.class);
    final DependencyResolver resolver = mock(DependencyResolver.class);
    if (key instanceof Class<?>) {
      assertEquals(expectedInstance, interceptor.intercept(injector, resolver).getInstance((Class<?>) key));
    } else if (key instanceof Type) {
      assertEquals(expectedInstance, interceptor.intercept(injector, resolver).getInstance((Type) key));
    } else {
      assertEquals(expectedInstance, interceptor.intercept(injector, resolver).getInstance((Key<?>) key));
    }
    verifyZeroInteractions(resolver);
  }

  private void assertInstanceNotIntercepted(final DependencyResolverInterceptor interceptor, final Object key) {
    final Injector injector = mock(Injector.class);
    final DependencyResolver resolver = mock(DependencyResolver.class);

    final Object instance = new Object();
    final Answer<Object> instanceAnswer = new Answer<Object>() {
      @Override
      public Object answer(final InvocationOnMock invocation) {
        return instance;
      }
    };

    if (key instanceof Class<?>) {
      final Class<?> type = (Class<?>) key;
      when(resolver.getInstance(Mockito.<Class<?>>any())).thenAnswer(instanceAnswer);
      assertSame(instance, interceptor.intercept(injector, resolver).getInstance(type));
      verify(resolver, only()).getInstance(type);
    } else if (key instanceof Type) {
      final Type type = (Type) key;
      when(resolver.getInstance(Mockito.<Type>any())).thenAnswer(instanceAnswer);
      assertSame(instance, interceptor.intercept(injector, resolver).getInstance(type));
      verify(resolver, only()).getInstance(type);
    } else {
      when(resolver.getInstance(Mockito.<Key<?>>any())).thenAnswer(instanceAnswer);
      assertSame(instance, interceptor.intercept(injector, resolver).getInstance((Key<?>) key));
      verify(resolver, only()).getInstance((Key<?>) key);
    }
  }

  private void assertProviderIntercepted(final DependencyResolverInterceptor interceptor, final Object key, final Provider<?> expectedProvider) {
    final Injector injector = mock(Injector.class);
    final DependencyResolver resolver = mock(DependencyResolver.class);
    if (key instanceof Class<?>) {
      assertSame(expectedProvider, interceptor.intercept(injector, resolver).getProvider((Class<?>) key));
    } else if (key instanceof Type) {
      assertSame(expectedProvider, interceptor.intercept(injector, resolver).getProvider((Type) key));
    } else {
      assertSame(expectedProvider, interceptor.intercept(injector, resolver).getProvider((Key<?>) key));
    }
    verifyZeroInteractions(resolver);
  }

  private void assertProviderNotIntercepted(final DependencyResolverInterceptor interceptor, final Object key) {
    final Injector injector = mock(Injector.class);
    final DependencyResolver resolver = mock(DependencyResolver.class);

    final Provider<?> provider = mock(Provider.class);
    final Answer<Provider<?>> providerAnswer = new Answer<Provider<?>>() {
      @Override
      public Provider<?> answer(final InvocationOnMock invocation) {
        return provider;
      }
    };

    if (key instanceof Class<?>) {
      final Class<?> type = (Class<?>) key;
      when(resolver.getProvider(Mockito.<Class<?>>any())).thenAnswer(providerAnswer);
      assertSame(provider, interceptor.intercept(injector, resolver).getProvider(type));
      verify(resolver, only()).getProvider(type);
    } else if (key instanceof Type) {
      final Type type = (Type) key;
      when(resolver.getProvider(Mockito.<Type>any())).thenAnswer(providerAnswer);
      assertSame(provider, interceptor.intercept(injector, resolver).getProvider(type));
      verify(resolver, only()).getProvider(type);
    } else {
      when(resolver.getProvider(Mockito.<Key<?>>any())).thenAnswer(providerAnswer);
      assertSame(provider, interceptor.intercept(injector, resolver).getProvider((Key<?>) key));
      verify(resolver, only()).getProvider((Key<?>) key);
    }
  }

  @SuppressWarnings("unused")
  private static abstract class TypeToken<T> {
    Type getType() {
      return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
  }

  @SuppressWarnings("unused")
  private @interface Annotated {
    String value() default "default";
  }
}
