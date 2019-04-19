[![Build Status](https://travis-ci.org/MichaelRocks/lightsaber.svg?branch=develop)](https://travis-ci.org/MichaelRocks/lightsaber)

Lightsaber
==========

Compile time dependency injection framework for JVM languages. Especially for Kotlin.

Why?
----

This framework is inspired by two projects: Guice and Dagger. While Guice is a quite small but a very powerful library
it's not efficient enough on Android as it relies on reflection at runtime. On the other hand Dagger makes all
its magic at compile time and thus is very efficient. However, Dagger uses APT under the hood, what may become a problem
when used not from Java.

The goal of Lightsaber is to provide lightning-fast compile time dependency injection and not to rely on APT at the same
time so that the library can be used with almost any JVM language and on Android.

Usage
-----

### Configuration

```groovy
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'io.michaelrocks:lightsaber-gradle-plugin:0.12.0-beta'
  }
}

// For Android projects.
apply plugin: 'com.android.application'
apply plugin: 'io.michaelrocks.lightsaber.android'

// For other projects.
apply plugin: 'java'
apply plugin: 'io.michaelrocks.lightsaber'

// Optional, just if you need Kotlin extension functions.
dependencies {
  implementation 'io.michaelrocks:lightsaber-core-kotlin:0.12.0-beta'
}
```

### Declaring dependencies

The primary goal of a DI framework is to inject dependencies into your code. Lightsaber can do that with constructor,
field, and method injection. In order to make injection work you have to annotate a method or a field with the `@Inject`
annotation and [provide dependencies](#providing-dependencies) in other parts of the project.

#### Constructor injection

Constructor injection is the most proper way of performing injection. All you have to do is to annotate a constructor of
a class with `@Inject`. Lightsaber will be able to provide values for the arguments of the constructor and to create
an instance of the class using this constructor. Moreover, when using constructor injection the class becomes eligible
for provision, that is this class itself can be used as a dependency. Lightsaber requires neither the class, nor the
injectable constructor to be `public`.

```java
public class Droid {
  @Inject
  public Droid(Battery battery) {
  }
}
```

#### Field injection

Sometimes you don't manage instantiation of a class. In this case you cannot use constructor injection. But you can
still use dependency injection for such classes. The easiest way to do that is to inject dependencies right into fields
of your class. To inform Lightsaber which fields it needs to inject you have to annotate them with `@Inject`.
Again, Lightsaber doesn't require the injectable field to be `public` or `final`.

```java
public class Droid {
  @Inject
  private Battery battery;
}
```

#### Method injection

In some cases you may want Lightsaber to call a method of a class after all fields of the class have been injected.
Just annotate the method with `@Inject` and Lightsaber will provide values for the arguments of the method and invoke
it. And as always, Lightsaber doesn't need the method to be `public`.

```java
public class Droid {
  private Battery battery;

  @Inject
  public void setBattery(Battery battery) {
    this.battery = battery;
  }
}
```

#### Injection order

Let's assume there's a class with constructor, fields, and methods marked as injectable . This class may have ancestor
classes with injectable fields and methods. When instantiating this class Lightsaber will perform injection in the
following order.

1. Instantiate the class via its injectable constructor.
2. Inject fields starting from ancestor classes.
3. Invoke injectable methods starting from ancestor classes. The order of injectable method invocations is undefined.

### Providing dependencies

In order to be able to inject a dependency you have to provide this dependency first. In other words you have to tell
Lightsaber what it have to return when requested a dependency of some type. This can be done in two ways: using modules
and their provider methods and via injectable constructors mentioned earlier.

#### Provider methods

Lightsaber requires provider methods to be defined in modules that need to be combined into components.

##### Modules

A module is a logical unit responsible for providing dependencies belonging to the module. Module classes must be
annotated with the `@Module` annotation. A module can contain a number of provider methods. Lightsaber treats a method
as a provider method if it's annotated with the `@Provide` annotation. When a type is provided by a provider method
it can be injected into a class in other parts of the project. Neither the module nor its provider methods are required
to be `public`.

```java
@Module
public class DroidModule {
  @Provide
  public Droid provideDroid() {
    return new Droid();
  }
}
```

Note that when manually creating a dependency Lightsaber doesn't perform field and method injection into the returned
instance. But you can do that via [manual injection](#manual-injection) or by creating a dependency with an
[injectable constructor](#injectable-constructors).

##### Components

To make Lightsaber aware of modules and their provided dependencies the modules have to be organized into a component.
A component is just a class annotated with the `@Component` annotation. The goal of this class is to import modules
to Lightsaber. Every method that imports a module must be annotated with `@Import`. Neither the component class
itself not its provider methods have to be `public`.

```java
@Component
public class DroidComponent {
  @Import
  public DroidModule importDroidModule() {
    return new DroidModule();
  }
}
```

One of the reasons why you need a component is that its instance should be passed as an arguments to a method that
creates an `Injector`. Finally, when a component is defined you can create an injector with this component.

```
Lightsaber lightsaber = new Lightsaber.Builder().build(); 
Injector injector = lightsaber.createInjector(DroidComponent());
```

The `createInjector()` method accepts a single component and returns an injector that can provide any dependency from
any module of the component and from any class with an [injectable constructor](#injectable-constructors).

##### Nested modules 

Not only components can import modules but modules can import other modules too. So if you have a reusable module
with some common dependencies you can import it to another module the same way you import it to a component:

```java
@Module
public class CommonDroidModule {
  @Provide
  public Battery provideBattery() {
    return new Battery();
  }
}

@Module
public class DroidModule {
  @Import
  public CommonDroidModule importCommonDroidModule() {
    return new CommonDroidModule();
  }
}
```  

#### Injectable constructors

A class may have one and only one injectable constructor. This constructor must be annotated with `@Inject` and can
have any number of arguments. When instantiating a class with an injectable constructor via an injector the injector
must be able to provide instances for every argument of the constructor.

Classes with injectable constructors should be bound to a module and thus to a component that provides the module.
This binding can be defined by annotating the class with `@ProvidedBy` annotation and specifying module classes in its
default parameter.

```java
@ProvidedBy(DroidModule.class)
public class Droid {
  @Inject
  public Droid(Battery battery) {
  }
}
```

If you have a module that should provide most of the dependencies you can make this module default by setting
`isDefault` parameter in the `@Module` annotation to `true` and avoid using `@ProvidedBy` annotation on classes that
need to be provided by this module.

When providing a dependency using an injectable constructor Lightsaber will perform field and method injection into
the provided instance.

### Manual injection

Manual injection is a way to create an instance of a provided type or to perform field and method injection into an
existing object. An instance can be obtained by retrieving the injector's `DepencyResolver` and calling its
`getInstance()` method:

```
Droid droid = 
    injector.getGeneralDependencyResolver().getInstance(Droid.class);
```

If you need a factory that provides instances of a given type you can get a `Provider` object from the `Injector`.
Then you'll be able to get an instance from the `Provider` by calling its `get()` method: 

```
Provider<Droid> droidProvider = 
    injector.getGeneralDependencyResolver().getProvider(Droid.class);
Droid droid = droidProvider.get();
```

When creating an instance of a dependency manually Lightsaber performs field and method injection for this instance.
But sometimes you already have an instance and want to inject dependencies into it. You can do that by calling the
`injectMember()` method of the `Injector` passing the instance to it.

```java
public class DroidController {
  @Inject
  private Droid droid;

  public void initialize(Injector injector) {
    injector.injectMembers(this);
  }
}
```

Consider the following example. We have a `Droid` interface and its implementation and we want to provide `Droid` as a
dependency.

```java
public interface Droid {
  /* ... */
}

public class ElectricalDroid implements Droid {
  @Inject
  private Battery battery;

  /* ... */
}
```

If we just create an `ElectricalDroid` instance and return it from a provider method the `battery` field will not be
initialized because Lightsaber doesn't perform injection into instances it doesn't manage. But we can fix that by
manually injecting dependencies into the instance using the `injectMembers()` method.

```java
@Module
public class DroidModule {
  @Provide
  public Droid provideDroid(Injector injector) {
    Droid droid = new ElectircalDroid();
    injector.injectMemebers(droid);
    return droid;
  }
}
```

While this is a working example it can be refactored to using constructor injection. In this case manual injection
becomes unnecessary.

```java
class ElectricalDroid implements Droid {
  private Battery battery;

  @Inject
  public ElectricalDroid(Battery battery) {
    this.battery = battery;
  }

  /* ... */
}
```

```java
@Module
public class DroidModule {
  @Provide
  public Droid provideDroid(ElectricalDroid droid) {
    return droid;
  }
}
```

### Singleton injection

By default Lightsaber creates a new instance every time a dependency is requested. This behavior can be changed so that
Lightsaber will return a single instance of the dependency for a given injector. All you need to do is to apply the
`@Singleton` annotation to a class with an injectable constructor or to a provider method.

```java
@Singleton
class ElectricalDroid implements Droid {
  /* ... */
}
```

```java
@Module
public class DroidModule {
  @Provide
  @Singleton
  public Droid provideDroid(ElectricalDroid droid) {
    return droid;
  }
}
```

In the example above you can annotate just a class or just a provider method or both the class and the provider method
with the `@Singleton` annotation and behavior will be very similar but not exactly the same.

If the `ElectricalDroid` is a singleton then one and only one instance of this class will be created per an injector
instance. And even if the `provideDroid()` method is not annotated with `@Singleton` it will return the same instance
every time it's called because it returns a singleton instance of `ElectricalDroid`.

On the other hand, if the `ElectricalDroid` class isn't a singleton the `provideDroid()` method annotated with
`@Singleton` will return a cached instance of `ElectricalDroid` so the instance will always be the same. But if
`ElectricalDroid` is injected somewhere else a new instance of this class will be created.

### Lazy injection

Instead of creating a dependency instance at injection time its instantiation can be deferred until the object is really
needed. For this purpose Lightsaber has a generic `Lazy` interface that can be injected instead of the dependency.

```java
public class Droid {
  @Inject
  private Lazy<Battery> battery;

  public void charge() {
    battery.get().charge();
  }
}
```

In this example a `Battery` instance will be created only when `battery.get()` is called.

### Provider injection

Provider injection is somewhat similar to lazy injection with one major difference: when `Provider.get()` is called
multiple times you can receive either the same instance of a dependency or a different instance on each invocation of
the `get()` method. Provider injection is useful when you need to pass some arguments to a constructor of an object
while other arguments should be provider by an injector.

```java
public class Droid {
  public Droid(Battery battery, Adapter adapter) {
    /* ... */
  }
}
```

```java
public class DroidFactory {
  @Inject
  private Provider<Battery> batteryProvider;

  public Droid createDroidWithAdapter(Adapter adapter) {
    return new Droid(batteryProvider.get(), adapter);
  }
}
```

### Qualified injection

Sometimes you may want to provide different implementations of a single dependency type. You can do that by applying a
qualifier annotation to a class with an injectable constructor or to a provider method. Then you need to apply the same
qualifier annotation to the provided dependency at the injection point. A dependency may have either no qualifiers or a
single one.

In the next example we will create a module that provides two different instances of the `Droid` class. To make
Lightsaber distinguish between these dependencies we will annotate them with the built-in `@Named` qualifier.

```java
@Module
public class DroidModule {
  @Provide
  @Singleton
  @Named("R2-D2")
  public Droid provideR2D2() {
    return new Droid("R2-D2");
  }

  @Provide
  @Singleton
  @Named("C-3PO")
  public Droid provideC3PO() {
    return new Droid("C-3PO");
  }

  @Provide
  @Singleton
  public Droid provideUnknownDroid() {
    return new Droid("Unknown");
  }
}
```

```java
public class DroidParty {
  @Inject
  @Named("R2-D2")
  private Droid r2d2;

  @Inject
  @Named("C-3PO")
  private Droid c3po;

  @Inject
  private Droid unknownDroid;
}
```

#### Custom qualifiers

Besides using the `@Named` qualifier you can create you own one. To do that you need to create an annotation and
annotate it with the `@Qualifier` annotation.

```java
public enum DroidType { R2D2, C3PO }

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({
    ElementType.TYPE,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.PARAMETER
})
public @interface Model {
  DroidType value();
}
```

```java
@Module
public class DroidModule {
  @Provide
  @Singleton
  @Model(DroidType.R2D2)
  public Droid provideR2D2() {
    return new Droid("R2-D2");
  }

  @Provide
  @Singleton
  @Model(DroidType.C3PO)
  public Droid provideC3PO() {
    return new Droid("C-3PO");
  }
}
```

```java
public class DroidParty {
  @Inject
  @Model(DroidType.R2D2)
  private Droid r2d2;

  @Inject
  @Model(DroidType.C3PO)
  private Droid c3po;
}
```

Custom qualifiers are allowed to have any number of properties of any type. When resolving dependencies Lightsaber
compares qualifiers by their types and equality of all their properties.

### Generic injection

With Lightsaber you can inject dependencies of generic types. The generic dependency has to be a parameterized type
and its type parameters cannot contain wildcards and type variables.

For example, these types you can use for injection:

- `List<String>`
- `Map<String, Collection<String>>`
- `Collection<int[]>`

And these types you cannot use:

- `List<? extends CharSequence>`
- `Map<String, T>`

### Child injection

When defining a component you can specify any number of parent components of the component. Given an injector created
with one of the parent components you can create a child injector by passing an instance of the child component to
the `createChildInjector()` method of the `Injector` interface.

The child injector inherits all the dependencies of its ancestor components, overrides the `Injector` dependency with
itself, and adds dependencies defined in its component. At the moment Lightsaber doesn't support dependency overriding
so all the components in a component chain must have distinct dependencies provided.

Consider the following case. In different parts of an application we need to construct droids. But depending on
a construction point we need to inject a battery of the corresponding type into a droid.

The following classes define a component that provides droids. Each droid accepts a `Battery` as a dependency.

```java
class ElectricalDroid implements Droid {
  private Battery battery;

  @Inject
  public ElectricalDroid(Battery battery) {
    this.battery = battery;
  }

  /* ... */
}
```

```java
@Module
public class DroidModule {
  @Provide
  @Singleton
  public Droid provideDroid(ElectricalDroid droid) {
    return droid;
  }
}
```

```java
@Component
public class DroidComponent {
  @Import
  public DroidModule importDroidModule() {
    return new DroidModule();
  }
}
```

As you can see the `Battery` is not provided anywhere. Here's our trivial `Battery` class.

```java
public class Battery {
  private String name;

  public Battery(String name) {
    this.name = name;
  }

  /* ... */
}
```

Let's define a component that provides a `Battery` with a given name.

```java
@Module
public class BatteryModule {
  private String name;

  public BatteryModule(String name) {
    this.name = name;
  }

  @Provide
  public Droid provideBattery() {
    return new Battery(name);
  }
}
```

```java
@Component(parent = DroidComponent.class)
public class BatteryComponent {
  private String name;

  public BatteryComponent(String name) {
    this.name = name;
  }

  @Import
  public BatteryModule importBatteryModule() {
    return new BatteryModule(name);
  }
}
```

Now we can create child injectors passing different instances of the `BatteryComponent` class to the
`createChildInjector()` method.

```
Lightsaber lightsaber = new Lightsaber.Builder().build();
Injector droidInjector = lightsaber.createInjector(new DroidComponent());
Injector nuclearBatteryInjector = 
    droidInjector.createChildInjector(new BatteryComponent("Nuclear"));
Injector plasmBatteryInjector =
    droidInjector.createChildInjector(new BatteryComponent("Plasm"));

Droid nuclearBatteryDroid =
    nuclearBatteryInjector.getGeneralDependencyResolver().getInstance(Droid.class);
Droid plasmBatteryDroid = 
    plasmBatteryInjector.getGeneralDependencyResolver().getInstance(Droid.class);
```

In the example above we created two singleton instances of the `ElectricalDroid` class passing different instances of
the `Battery` class to them. Please, note that if for some reason a singleton dependency was instantiated via a parent
injector and then child injectors were created the child injectors would return the same singleton instance created by
the parent injector.

### Factories (assisted injection)

In some cases you may want to instantiate an object passing some arguments to its constructor from an injector and
provide some other arguments manually at the instantiation site.

Let's define a `Droid` class that has a constructor with two parameters: a battery and a model:

```java
public class Droid {
  private final Battery battery;
  private final String model;
  
  @Factory.Inject
  public Droid(Battery battery, @Factory.Parameter String model) {
    this.battery = battery;
    this.model = model;
  }
  
  /* ... */
}
```

`Droid`'s constructor is annotated with `@Factory.Inject` annotation. This annotation means that this constructor can be
used for injections but some of its arguments aren't provided by injector's component. Now let's define a module that
will be used for providing a `Battery` for the `Droid`:

```java
@Module
public class DroidModule {
  @Provide
  public Battery provideBattery() {
    return new Battery();
  }
}
```

As you can see no `String` dependency is provided by the module. In order to create a `Droid` we have to provide a model
name indirectly at the instantiation site. Lightsaber offers a way to achieve that by supporting factories that can
accept any arguments and pass them to injectable constructors. 

```java
@Factory
@ProvidedBy(DroidModule.class)
public interface DroidFactory {
  Droid assembleDroid(String model);
}
```

The factory must be an interface annotated with `@Factory` annotation and may contain any number of factory methods. 
The factory method may contain any number of parameters with unique types. If you need the factory method to contain 
multiple parameters of the same type they have to be annotated with different qualifiers like `@Named("parameterName")`.
Lightsaber matches factory method's parameters with constructor's parameters annotated with `@Factory.Parameter` by
a type and a qualifier. A component that provides a factory must provide dependencies for all constructor's parameters
that aren't annotated with `@Factory.Parameter`.

After the factory is defined as shown above it can be injected or retrieved manually from an injector as any other
dependency:

```java
public class DroidParty {
  @Inject
  public DroidParty(DroidFactory factory) {
    Droid r2d2 = factory.assembleDroid("R2-D2");
    Droid c3po = factory.assembleDroid("C-3PO");
  }
}
```

### Provider interceptors

When writing tests you may need to substitute a real dependency with a mock. To be able to do that you can register a `ProviderInterceptor` when
creating a `Lightsaber` instance and replace a provider with the one that returns mocks:

```java
Lightsaber lightsaber = new Lightsaber.Builder()
    .addProviderInterceptor(
        new ProviderInterceptor() {
          @Override
          public Provider<?> intercept(ProviderInterceptor.Chain chain, Key<?> key) {
            if (key.getType() == Battery.class) {
              return new Provider<Object>() {
                @Override
                public Object get() {
                  return new TestBattery();
                }
              };
            } else {
              return chain.proceed(key);
            }
          }
        }
    )
    .build();
``` 

### Testing

To simplify unit testing and dependency substitution you can add a special testing module to your project's configuration:

```groovy
dependencies {
  testImplementation 'io.michaelrocks:lightsaber-test:0.12.0-beta'
}
```

This module allows you to build a `ProviderInterceptor` using a convenient builder API. Moreover, it supports creation of annotation proxies at
runtime, so you'll be able to deal with qualified dependencies easily.

```java
// Create a provider of Battery instances.
Provider<Battery> provider = new Provider<Battery>() {
  @Override
  public Battery get() {
    return new TestBattery();
  }
};

// Create a proxy for @Named("primary") annotation.
Named annotation = new AnnotationBuilder<Named>(Named.class)
    .addMember("value", "primary")
    .build();

// Create a provider interceptor that replaces the primary battery with the test one.
ProviderInterceptor interceptor = new ProviderInterceptorBuilder()
    .addProviderForClass(Battery.class, annotation, provider)
    .build();

// Create a Lightsaber instance for unit testing. 
Lightsaber lightsaber = new Lightsaber.Builder()
    .addProviderInterceptor(interceptor)
    .build();
``` 

License
-------

    Copyright 2019 Michael Rozumyanskiy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
