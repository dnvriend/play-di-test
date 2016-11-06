# play-di-test

[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

A small study project on how play's dependency injection framework works.

## Launch
To launch the project type:

```bash
sbt run
http :9000
```

## Play's DI
Play uses Google Guice, a lightweight dependency injection container from Bob Lee and Kevin Bourrillion from Google.

## Architecture
The project has the following break-down:

container: 
 - Play: the container for components

Components implement business functionality:
  - Foo: implements Foo functionality
  - cross-cutting components: (none in this project) but a JDBC connection 
    or Kafka Producer/Consumer or a SecurityProvider could be cross cutting components that can be injected into other components as a dependency.

classes:
 - Foo uses facade, service, actor and these classes are all wired together with Guice either via annotations (yuck) and 
   a Guice Module, that uses Providers to provide instances. Modules wire a component together. 

## Documentation
- [Google Guice Wiki](https://github.com/google/guice/wiki)
- [Guice vs Spring](https://github.com/google/guice/wiki/SpringComparison)
- [Guicing up your testing - Dick Wall](http://www.developer.com/design/article.php/3684656/Guicing-Up-Your-Testing.htm)

## Dependency Injection with Guice
With dependency injection, objects accept dependencies in their constructors. To construct an object, you first build its
dependencies. But to build each dependency, you need its dependencies, and so on. So when you build an object, you really
need to build an object graph.

Building object graphs by hand is labour intensive, error prone, and makes testing difficult. Instead, Guice can build the
object graph for you. But first, Guice needs to be configured to build the graph exactly as you want it.

The dependency injection pattern leads to code that's modular and testable, and Guice makes it easy to write.
To use Guice we first need to tell it how to map our interfaces to their implementations.
This configuration is done in a Guice module, which is any class that implements the `com.google.inject.Module` interface:

```scala
import com.google.inject._
import com.google.inject.name.Names
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("bar-model"))
      .toProvider(classOf[BarModelProvider])
      .asEagerSingleton()
  }
}
```

Guice uses bindings to map types to their implementations. A module is a collection of bindings specified using fluent,
English-like method calls.

The modules are the building blocks of an injector, which is Guice's object-graph builder. First we create the injector,
and then we can use that to build instances

The injector's job is to assemble graphs of objects. You request an instance of a given type, and it figures out what
to build, resolves dependencies, and wires everything together. To specify how dependencies are resolved, configure
your injector with bindings.

To create bindings, extend AbstractModule and override its configure method. In the method body, call bind() to
specify each binding. These methods are type checked so the compiler can report errors if you use the wrong types.
Once you've created your modules, pass them as arguments to `Guice.createInjector()` or add the fqcn to `application.conf`
to build an injector:

```
play.modules {
  enabled += "com.github.dnvriend.component.foo.Module"
  enabled += "com.github.dnvriend.component.bar.Module"
}
```

We add @Inject to the class constructor, which directs Guice to use it. Guice will inspect the annotated constructor,
and lookup values for each parameter.

## Binding Annotations
Occasionally you'll want multiple bindings for a same type. To enable this, bindings support an optional binding annotation.
The annotation and type together uniquely identify a binding. This pair is called a key.

Guice comes with a built-in binding annotation `@Named` that uses a string. Since the compiler can't check the string,
we recommend using @Named sparingly.

## Instance Bindings
You can bind a type to a specific instance of that type. This is usually only useful only for objects that don't have
dependencies of their own, such as value objects:

```
bind(classOf[String])
   .annotatedWith(Names.named("JDBC URL"))
   .toInstance("jdbc:mysql://localhost/pizza");
bind(classOf[Integer])
   .annotatedWith(Names.named("login timeout seconds"))
   .toInstance(10)
```

Avoid using .toInstance with objects that are complicated to create, since it can slow down application startup.
You can use an `@Provides` method instead.

## Provides Methods
When you need code to create an object, use an `@Provides` method. The method must be defined within a module,
and it must have an @Provides annotation. The method's return type is the bound type. Whenever the injector needs
an instance of that type, it will invoke the method.

Dependencies can be passed in as parameters to the method. The injector will exercise the bindings for each of
these before invoking the method.

## Just In Time Bindings
There are two JIT-bindings that make it possible do do without Modules and just use annotations to look up either
implementations or providers.

The `@ImplementedBy` annotation tells the injector what their default implementation of an interface is.
The `@ImplementedBy` annotation acts like a linked binding, specifying the subtype to use when building a type and
should be defined on the interface on the type that should be injected.

The `@ProvidedBy` annotation tells the injector about a Provider class that produces instances and should be defined
on the interface of the type that should be injected.

## JSR-330
JSR-330 standardizes annotations like @Inject and the Provider interfaces for Java platforms. It doesn't currently
specify how applications are configured, so it has no analog to Guice's modules.

| JSR-330      | Guide               | Description |
| javax.inject | com.google.inject   |             |
| ------------ | ------------------- | ----------- |
| @Inject      | @Inject             | Interchangeable with constraints see note 1 |
| @Named       | @Named              | Interchangeable |
| @Qualifier   | @BindingAnnotation  | Interchangeable |
| @Scope       | @ScopeAnnotation    | Interchangeable |
| @Singleton   | @Singleton          | Interchangeable |


__Note 1:__ JSR-330 places additional constraints on injection points. Fields must be non-final.
Optional injection is not supported. Methods must be non-abstract and not have type parameters of their own.
Additionally, method overriding differs in a key way: If a class being injected overrides a method where the
superclass' method was annotated with javax.inject.Inject, but the subclass method is not annotated,
then the method will not be injected.

__Note 2:__ Guice's Provider extends JSR-330's Provider. Use Providers.guicify() to convert a
JSR-330 Provider into a Guice Provider.

__Best Practice:__ Prefer JSR-330's annotations and Provider interface.

## Injecting Actors
Play provides supupport for binding actors with Guice with `play.api.libs.concurrent.AkkaGuiceSupport`:

```scala
class MyModule extends AbstractModule with AkkaGuiceSupport {
  def configure = {
    bindActor[MyActor]("myActor")
  }
}
```

Then to use the above actor in your application, add a qualified injected dependency, like so:

```scala
class MyController @Inject() (@Named("myActor") myActor: ActorRef) extends Controller {

}
```

## Play, Slick and Databases
The [Play Slick module](https://www.playframework.com/documentation/2.5.x/PlaySlick) makes [Slick](http://slick.typesafe.com/) 
a first-class citizen of Play. Its a good idea to read this guide

## Database Configuration
To have the Play Slick module handling the lifecycle of Slick databases, it is important that you never create database’s
instances explicitly in your code. Rather, you should provide a valid Slick driver and database configuration in your
`application.conf` (by convention the default Slick database must be called default):

```
# Default database configuration
slick.dbs.default.driver="slick.driver.H2Driver$"
slick.dbs.default.db.driver="org.h2.Driver"
slick.dbs.default.db.url="jdbc:h2:mem:play"
```

The `slick.dbs` prefix before the database’s name is configurable. You may change it by overriding the value of
the configuration key `play.slick.db.config`.

In the above configuration `slick.dbs.default.driver` is used to configure the `Slick driver` (the slick SQL Dialect engine),
while `slick.dbs.default.db.driver` is the underlying `JDBC driver` used by Slick’s backend.

Slick does not support the `DATABASE_URL` environment variable in the same way as the default
Play JBDC connection pool. But starting in version 3.0.3, Slick provides a `DatabaseUrlDataSource`
specifically for parsing the environment variable:

```
slick.dbs.default.driver="slick.driver.PostgresDriver$"
slick.dbs.default.db.dataSourceClass = "slick.jdbc.DatabaseUrlDataSource"
slick.dbs.default.db.properties.driver = "org.postgresql.Driver"
```

## DatabaseConfigProvider
After having properly configured a Slick database, you can obtain a `slick.backend.DatabaseConfig`
(which is a Slick type bundling a __database__ and __driver__) in two different ways. Either by using dependency injection,
or through a global lookup via the `DatabaseConfigProvider` singleton.

## DatabaseConfig via Dependency Injection
The following example show how to inject a `DatabaseConfig` instance for the default database
(i.e., the database named `default` in your configuration):

```scala
class FooController @Inject()(dbConfigProvider: DatabaseConfigProvider) extends Controller {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
}
```

Injecting a DatabaseConfig instance for a different database is also easy. Simply prepend the annotation
`@NamedDatabase("<db-name>")` to the dbConfigProvider constructor parameter:

```scala
class BarController @Inject()(@NamedDatabase("<db-name>") dbConfigProvider: DatabaseConfigProvider) extends Controller {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
}
```

Of course, you should replace the string "<db-name>" with the name of the database’s configuration you want to use.



- Integration of Slick into Play’s application lifecycle.
- Support for [Play database evolutions](https://www.playframework.com/documentation/2.5.x/Evolutions).
To have the [Play Slick module](https://www.playframework.com/documentation/2.5.x/PlaySlick) handling the lifecycle of Slick databases, it is important that you never create database’s instances explicitly in your code. Rather, you should provide a valid Slick driver and database configuration in your application.conf (by convention the default Slick database must be called default):