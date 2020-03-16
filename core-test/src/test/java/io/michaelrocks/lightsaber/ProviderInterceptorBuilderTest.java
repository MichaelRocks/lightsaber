/*
 * Copyright 2020 Michael Rozumyanskiy
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
import java.util.List;

import javax.inject.Provider;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ProviderInterceptorBuilderTest {
  @Test
  public void testAddProviderForClass() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    final ProviderInterceptor interceptor = new ProviderInterceptorBuilder()
        .addProviderForClass(String.class, provider)
        .build();

    assertIntercepted(interceptor, Key.of(String.class), provider);
    assertNotIntercepted(interceptor, Key.of(Object.class));
  }

  @Test
  public void testAddProviderForType() {
    @SuppressWarnings("unchecked")
    final Provider<List<String>> provider = mock(Provider.class);
    final ProviderInterceptor interceptor = new ProviderInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider)
        .build();

    assertIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType()), provider);
    assertNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType()));
  }

  @Test
  public void testAddProviderForKey() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    final ProviderInterceptor interceptor = new ProviderInterceptorBuilder()
        .addProviderForKey(Key.of(String.class), provider)
        .build();

    assertIntercepted(interceptor, Key.of(String.class), provider);
    assertNotIntercepted(interceptor, Key.of(Object.class));
  }

  @Test
  public void testAddProviderForClassWithAnnotation() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider1 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider2 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider3 = mock(Provider.class);

    final ProviderInterceptor interceptor = new ProviderInterceptorBuilder()
        .addProviderForClass(String.class, provider1)
        .addProviderForClass(String.class, Annotated.class, provider2)
        .addProviderForClass(String.class, createAnnotation("explicit"), provider3)
        .build();

    assertIntercepted(interceptor, Key.of(String.class), provider1);
    assertIntercepted(interceptor, Key.of(String.class, createAnnotation()), provider2);
    assertIntercepted(interceptor, Key.of(String.class, createAnnotation("explicit")), provider3);
    assertNotIntercepted(interceptor, Key.of(Object.class));
    assertNotIntercepted(interceptor, Key.of(Object.class, createAnnotation()));
    assertNotIntercepted(interceptor, Key.of(Object.class, createAnnotation("explicit")));
  }

  @Test
  public void testAddProviderForTypeWithAnnotation() {
    @SuppressWarnings("unchecked")
    final Provider<List<String>> provider1 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<List<String>> provider2 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<List<String>> provider3 = mock(Provider.class);

    final ProviderInterceptor interceptor = new ProviderInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider1)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider2)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), provider3)
        .build();

    assertIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType()), provider1);
    assertIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()), provider2);
    assertIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), provider3);
    assertNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType()));
    assertNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation()));
    assertNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation("explicit")));
  }

  @Test
  public void testAddProviderForKeyWithAnnotation() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider1 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider2 = mock(Provider.class);
    @SuppressWarnings("unchecked")
    final Provider<String> provider3 = mock(Provider.class);

    final ProviderInterceptor interceptor = new ProviderInterceptorBuilder()
        .addProviderForKey(Key.of(String.class), provider1)
        .addProviderForKey(Key.of(String.class, createAnnotation()), provider2)
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider3)
        .build();

    assertIntercepted(interceptor, Key.of(String.class), provider1);
    assertIntercepted(interceptor, Key.of(String.class, createAnnotation()), provider2);
    assertIntercepted(interceptor, Key.of(String.class, createAnnotation("explicit")), provider3);
    assertNotIntercepted(interceptor, Key.of(Object.class));
    assertNotIntercepted(interceptor, Key.of(Object.class, createAnnotation()));
    assertNotIntercepted(interceptor, Key.of(Object.class, createAnnotation("explicit")));
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

    final ProviderInterceptor interceptor = new ProviderInterceptorBuilder()
        .addProviderForClass(String.class, provider1)
        .addProviderForClass(String.class, Annotated.class, provider2)
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider3)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider4)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider5)
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), provider6)
        .build();

    assertIntercepted(interceptor, Key.of(String.class), provider1);
    assertIntercepted(interceptor, Key.of(String.class, createAnnotation()), provider2);
    assertIntercepted(interceptor, Key.of(String.class, createAnnotation("explicit")), provider3);
    assertIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType()), provider4);
    assertIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()), provider5);
    assertIntercepted(interceptor, Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit")), provider6);
    assertNotIntercepted(interceptor, Key.of(Object.class));
    assertNotIntercepted(interceptor, Key.of(Object.class, createAnnotation()));
    assertNotIntercepted(interceptor, Key.of(Object.class, createAnnotation("explicit")));
    assertNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType()));
    assertNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation()));
    assertNotIntercepted(interceptor, Key.of(new TypeToken<List<Object>>() {}.getType(), createAnnotation("explicit")));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameClassThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForClass(String.class, provider)
        .addProviderForClass(String.class, provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameClassWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForClass(String.class, Annotated.class, provider)
        .addProviderForClass(String.class, Annotated.class, provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameClassWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForClass(String.class, createAnnotation("explicit"), provider)
        .addProviderForClass(String.class, createAnnotation("explicit"), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameTypeThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameTypeWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameTypeWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), provider)
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameKeyThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForKey(Key.of(String.class), provider)
        .addProviderForKey(Key.of(String.class), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForSameKeyWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider)
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForClassAndEqualKeyThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForClass(String.class, provider)
        .addProviderForKey(Key.of(String.class), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForClassAndEqualKeyWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForClass(String.class, Annotated.class, provider)
        .addProviderForKey(Key.of(String.class, createAnnotation()), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForClassAndEqualKeyWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForClass(String.class, createAnnotation("explicit"), provider)
        .addProviderForKey(Key.of(String.class, createAnnotation("explicit")), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForTypeAndEqualKeyThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), provider)
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType()), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForTypeAndEqualKeyWithImplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), Annotated.class, provider)
        .addProviderForKey(Key.of(new TypeToken<List<String>>() {}.getType(), createAnnotation()), provider)
        .build();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testAddProviderForTypeAndEqualKeyWithExplicitAnnotationThrowsException() {
    @SuppressWarnings("unchecked")
    final Provider<String> provider = mock(Provider.class);
    new ProviderInterceptorBuilder()
        .addProviderForType(new TypeToken<List<String>>() {}.getType(), createAnnotation("explicit"), provider)
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

  private void assertIntercepted(final ProviderInterceptor interceptor, final Key<?> key, final Provider<?> expectedProvider) {
    final ProviderInterceptor.Chain chain = mock(ProviderInterceptor.Chain.class);
    assertSame(expectedProvider, interceptor.intercept(chain, key));
    verifyNoMoreInteractions(chain);
  }

  private void assertNotIntercepted(final ProviderInterceptor interceptor, final Key<?> key) {
    final Provider<?> provider = mock(Provider.class);
    final ProviderInterceptor.Chain chain = mock(ProviderInterceptor.Chain.class);
    when(chain.proceed(Mockito.<Key<Object>>any())).thenAnswer(
        new Answer<Provider<?>>() {
          @Override
          public Provider<?> answer(final InvocationOnMock invocation) {
            return provider;
          }
        }
    );

    assertSame(provider, interceptor.intercept(chain, key));
    verify(chain, only()).proceed(key);
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
