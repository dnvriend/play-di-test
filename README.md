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

## Environment and Configuration
The `play.api.Environment` class that can be provided by Module The environment The environment for the application.

Captures concerns relating to the classloader and the filesystem for the application.

A full configuration set.

The underlying implementation is provided by https://github.com/typesafehub/config.

## Play akka configuration
Play's default configuration will look for the akka configuration in the 'normal' location in the Typesafe configuration
which is the `akka` root node as can be seen in the configuration below:

The play akka configuration is defined in the library `com.typesafe.play:play:2.5.x` in reference.conf:

```
play {
  akka {
      # The name of the actor system that Play creates
      actor-system = "application"

      # How long Play should wait for Akka to shutdown before timing it.  If null, waits indefinitely.
      shutdown-timeout = null

      # The location to read Play's Akka configuration from
      config = "akka"

      # The blocking IO dispatcher, used for serving files/resources from the file system or classpath.
      blockingIoDispatcher {
        fork-join-executor {
          parallelism-factor = 3.0
        }
      }

      # The dev mode actor system. Play typically uses the application actor system, however, in dev mode, an actor
      # system is needed that outlives the application actor system, since the HTTP server will need to use this, and it
      # lives through many application (and therefore actor system) restarts.
      dev-mode {
        # Turn off dead letters until Akka HTTP server is stable
        log-dead-letters = off
        # Disable Akka-HTTP's transparent HEAD handling. so that play's HEAD handling can take action
        http.server.transparent-head-requests = false
      }
    }
}
```

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


| JSR-330 (javax.inject) | Guide (com.google.inject) | Description |
| ---------------------- | ------------------------- | ----------- |
| @Inject                | @Inject                   | Interchangeable with constraints see note 1 |
| @Named                 | @Named                    | Interchangeable |
| @Qualifier             | @BindingAnnotation        | Interchangeable |
| @Scope                 | @ScopeAnnotation          | Interchangeable |
| @Singleton             | @Singleton                | Interchangeable |

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

## ConductR
[Lightbend ConductR](https://conductr.lightbend.com/) is for managing microservices with the "batteries included".

## ConductR v2.x.x
[ConductR v2.0.x](http://conductr.lightbend.com/docs/2.0.x/) is being released.

The difference between ConductR v1 and v2 is that ConductR now consists of `conductr-core` and `conductr-agent`.
Conductr-core and conductr-agent run as separate processes and are separate services

### ConductR Core
ConductR core provides cluster and application information as well as its control interface via the REST API.
ConductR core is responsible for cluster-wide scaling and replication decisions as well as hosting the application
files or the bundles. By default, `conductr-core` exposes its Control REST interface at port `9005`.

By default, the `conductr-core` service runs under the `conductr` user along with the `conductr` group. Its pid file
is written to `/var/run/conductr/running.pid` and its install location is `/usr/share/conductr`.

### ConductR Agent
The ConductR agent is responsible for executing the application process. By default, the `conductr-agent` runs under the
`conductr-agent` user along with the `conductr-agent` group. Its pud file is written to `/var/run/conductr-agent/running.pid`
and its install location is `/usr/share/conductr-agent`.

### Seed node
The first `conductr-core` process that runs on a machine is called the `seed` node, and is the initial contact point
for all other `conductr-core` nodes that need to join the cluster. So all new `conductr-core` processes will need
a `--seed` configuration set. Afterwards new nodes can join

## DC/OS
ConductR can run as a Framework within DC/OS. A Mesos framework receives resource offers and decides whether to accept these
resources and schedule work on them, or to decline the resource offer.

## What is conductr?
1. Clustered Akka application with special features:
- haproxy for location transparency (your apps can resolve other apps through ConductR API)
- CLI and REST API
- Docker support

2. Play application (visual console)

3. Sbt Plugin
- local development and deploy
- build environment deploy

As the end user of ConductR, you are expected to install it in a network, and use the sbt plugin to build your executable bundle.

An __Application__ in ConductR is a collection of one or more __Bundles__. The developer decides what bundles make up an Application, and then aggregates them with a configuration attribute ("system").

Each Bundle can contain one or more __Components__, typically just one. This represents a process in ConductR's lifecycle management terms.

When you package your Application, a uniquely named ZIP file (shazam) will be created for each bundle, containing a manifest.

Here is an example of bundle configuration with one Component as specified in build.sbt:

```scala
enablePlugins(JavaAppPackaging,ConductRPlugin)
BundleKeys.nrOfCpus := 1.0
BundleKeys.memory := 10.MiB
BundleKeys.diskSpace := 5.MB
BundleKeys.endpoints := Map("singlemicro" -> Endpoint("http",0,services = Set(URI("http:/singlemicro"))))
```

This will result in the following __bundle.conf__ manifest that will be included in your .zip artifact:

```bash
version    = "1.0.0"
name       = "singlemicro"
system     = "singlemicro-1.0.0"
nrOfCpus   = 1.0
memory     = 67108864
diskSpace  = 5000000
roles      = ["backend"]
components = {
  "singlemicro-1.0.0" = {
    description      = "singlemicro"
    file-system-type = "universal"
    start-command    = ["singlemicro-1.0.0/bin/singlemicro", "-J-Xms67108864", "-J-Xmx67108864"]
    endpoints        = {
      "singlemicro" = {
        bind-protocol  = "http"
        bind-port = 0
        services  = ["http:/singlemicro"]
      }
    }
  }
}
```

## Conductr v1.x.x
The following is for ConductR v1.x.x

## Installing Conductr
1. You'll have to install [Python 3](https://www.python.org/downloads/mac-osx/), so install it on your system because the Conductr
CLI tools need it. Also you need pip3 (the package manager for Python) to be installed because it will install Conductr.

2. Installing the conductr-cli:

```bash
sudo pip3 install conductr-cli
```

Upgrading the conductr-cli:

```bash
sudo pip3 install conductr-cli --upgrade
```

3. You should get a __free__ [Developer account](https://www.lightbend.com/account/login) at Lightbend.

4. Use the provided scripts to launch the sandbox environment.

## /etc/hosts file
You should add an alias 'boot2docker' to the `/etc/hosts` file. If you don't, the conductr containers will not communicate and the sandbox will not work. Please add the following:

```bash
127.0.0.1	localhost boot2docker
```

## Docker for mac
When running with Docker for mac, you will have a whale icon at the top of your screen indicating that Docker has been installed. It can also be used for configuring the docker instance eg. memory and cpu usage.

The sandbox can be launched in Docker for mac with the following command:

```bash
sandbox run 1.1.10 --feature visualization --nr-of-containers 3
```

This will start a conductr environment consisting of three nodes. It will also start the visualizer, which that allows you to see a visualization of the current state of the cluster, such as nodes or bundles. The visualizer is available at:

```bash
http://boot2docker:9999
```

## conductr-cli
Conductr provides a REST api to query the information on loaded bundles and running services and to
manage the lifecycle of said bundles (load, run, stop, unload). The [conductr-cli](http://conductr.lightbend.com/docs/2.0.x/CLI) is a handy tool, implemented in Python, to let you operate on Conductr using the comfort of you command-line interface.

| Command          | Description                                            |
| ---------------- | ------------------------------------------------------ |
| conduct version  | print the conductr-cli version that has been installed |
| conduct info     | print cluster information ie. what has been installed on the cluster |
| conduct services | print service information that a bundle exposes        |
| conduct acls tcp / http | print request ACL information for allowing a bundle's service to be accessed or not |
| conduct load path | load a bundle |
| conduct run id | run a bundle |
| conduct stop id | stop a bundle |
| conduct unload id | unload a bundle |
| conduct events id | show events of a bundle |
| conduct logs id | show logs of a bundle |

## sbt-conductr
[sbt-conductr](https://github.com/typesafehub/sbt-conductr) is a sbt plugin that provides commands in sbt to:

- Produce a ConductR bundle
- Start and stop a local ConductR cluster
- Manage a ConductR cluster within a sbt session

| Command     | Description                                                                      |
| ----------- | -------------------------------------------------------------------------------- |
| bundle:dist |	Produce a ConductR bundle for all projects that have the native packager enabled |
| conduct load | Loads a bundle and an optional configuration to the ConductR                    |

# Port Configuration
One of the difficult things in creating clustered services is the port and host configuration. These settings are not static and can change per environment and will change when scaling up or down a clustered application.

ConductR addresses this with its Endpoint configuration declaration:

```scala
BundleKeys.endpoints := Map("singlemicro" -> Endpoint("http", 0, Set(URI("http:/singlemicroservice"))))
```

When this bundle is run by ConductR, two system env properties are created called `SINGLEMICRO_BIND_IP` and `SINGLEMICRO_BIND_PORT`.

These are available to your app, both in application.conf:

```bash
singlemicro {
  ip = "127.0.0.1"
  ip = ${?SINGLEMICRO_BIND_IP}
  port = 8096
  port = ${?SINGLEMICRO_BIND_PORT}
}
```

and programatically:

```scala
sys.env.get("SINGLEMICRO_BIND_IP")
```

If you need these env properties passed in to your app's main as args, use the startCommand attribute:

```scala
BundleKeys.startCommand += "-Dhttp.address=$SINGLEMICRO_BIND_IP -Dhttp.port=$SINGLEMICRO_BIND_PORT"
```

## Conductr Ports
Conductr uses the following ports on each node:

- 9004: Akka remoting
- 9005: REST Control API
- 9006: Bundle streaming between ConductR nodes
- 10000 - 10999: default range of ports allocated for bundle endpoints

## Control API
Conductr (core) exposes a control API, which is a REST interface that exposes [the following functionality](http://conductr.lightbend.com/docs/2.0.x/ControlAPI):

__Bundle API:__
- load a bundle
- scale a bundle
- unload a bundle
- query bundle state
- query logs by bundle
- query events by bundle

__Cluster Membership API:__
- query membership state

The Control API is available at port `9005` by default and can be queried eg. the sandbox with:

```bash
$ http :9005/v2/members

HTTP/1.1 200 OK
Content-Length: 109
Content-Type: application/json
Date: Wed, 26 Oct 2016 16:48:53 GMT
Server: akka-http/2.4.10

{
    "members": [],
    "selfNode": {
        "address": "akka.tcp://conductr@172.17.0.2:9004",
        "uid": -918763336
    },
    "unreachable": []
}

$ http :9015/v2/members

HTTP/1.1 200 OK
Content-Length: 108
Content-Type: application/json
Date: Wed, 26 Oct 2016 16:54:21 GMT
Server: akka-http/2.4.10

{
    "members": [],
    "selfNode": {
        "address": "akka.tcp://conductr@172.17.0.3:9004",
        "uid": 587595788
    },
    "unreachable": []
}

$ http :9025/v2/members

HTTP/1.1 200 OK
Content-Length: 109
Content-Type: application/json
Date: Wed, 26 Oct 2016 16:54:03 GMT
Server: akka-http/2.4.10

{
    "members": [],
    "selfNode": {
        "address": "akka.tcp://conductr@172.17.0.4:9004",
        "uid": -463293053
    },
    "unreachable": []
}
```


## Conductr Manual
You should read the [Conductr manual](https://conductr.lightbend.com/docs/1.1.x/Home) to get confortable with the Conductr concepts (the terms and such) so you know what a bundle is, which CLI and SBT tools there are and which commands to use to do a certain thing. The manual is great, so go and read!


You should launch the conductr sandbox which is explained in the root of this project. Afterwards you can just type `install` in the sbt console of the `hello-play` project.
Sbt will introspect the project and any sub projects, generate `bundles` and their configuration, restart the sandbox to ensure a clean state and then load and run the application.
You can then access your application at `http://docker-host-ip:9000`, eg. `http://localhost:9000`

## Scheduling Parameters
[Scheduling parameters](http://conductr.lightbend.com/docs/1.1.x/CreatingBundles#Producing-a-bundle) are parameters that describe what resources are used by your application or service and are used
to determine which machine they will run on.

The Play and Lagom bundle plugins provide [default scheduling parameters](https://github.com/typesafehub/sbt-conductr/blob/master/README.md#scheduling-parameters), i.e. it is not mandatory to declare scheduling parameters for
these kind of applications. However, it is recommended to define custom settings for each of your application. The defaults are:

```scala
import ByteConversions._

javaOptions in Universal := Seq(
  "-J-Xmx128m",
  "-J-Xms128m"
)

BundleKeys.nrOfCpus := 0.1
BundleKeys.memory := 384.MiB
BundleKeys.diskSpace := 200.MB
```

### com.typesafe.conductr.bundlelib.play.api.StatusService
Conduct's StatusService is required by a bundle component in order to signal when it has started. A successful startup is anything that the application is required to do to become available for processing. For play applications this is done [automatically](http://conductr.lightbend.com/docs/1.1.x/DevQuickStart#Signaling-application-state).

### com.typesafe.conductr.bundlelib.play.api.LocationService
ConductR's LocationService is able to respond with a URI declaring where a given service (as named by a bundle component's endpoint) resides:

### com.typesafe.conductr.bundlelib.play.api.Env

## Injection
The conductr services above are expected to be injected using Play 2.5's dependency injection mechanism:

```scala
class MyGreatController @Inject() (locationService: LocationService, locationCache: CacheLike) extends Controller {
  ...
  locationService.lookup("known", URI(""), locationCache)
  ...
}
```

The following components are available for injection:

- CacheLike
- ConnectionContext
- LocationService
- StatusService

## Environment Variables
Please read the [environment variables reference](http://conductr.lightbend.com/docs/1.1.x/BundleEnvironmentVariables#Standard-environment-variables) to review which variables are available to a bundle component at runtime.


## Testing
Writing tests for your application can be an involved process. [Play provides helpers and application stubs](https://www.playframework.com/documentation/2.5.x/ScalaTestingWithScalaTest),
and ScalaTest called `ScalaTestPlusPlay`, to make testing your application as easy as possible.

You’ll need to add the following dependency to build.sbt:

```scala
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
```

The library provides the `org.scalatestplus.play.PlaySpec` class that the [play-documentation](https://www.playframework.com/documentation/2.5.x/ScalaTestingWithScalaTest)
describes but `PlaySpec` uses `WordSpec with MustMatchers` that results in a testing DSL that uses a specification style
that I dislike, which (over uses) the `must` word, but `scalatestplus-play` also provides `org.scalatestplus.play.WsScalaTestClient` that is
very handy in tests.

Therefor I will be using my own (well-known) TestSpec class `TestSpec` that extends `FlatSpec with Matchers` resulting in
a more relaxed DSL that is 'should' based. This could be cultural thing, but its a better of style and taste.
You should choose what fits your thought process best (saw what I did there!).

## ScalaTest
Why use ScalaTest? Well, ScalaTest is the most flexible and most popular testing tool in the Scala ecosystem.
With ScalaTest, you can test Scala, Java and Scala.js (JavaScript) code. So lets use it!

Some definitions:

- The central concept in ScalaTest is the `suite`, a collection of zero to many `tests`.
- A test can be anything with a name that can start and either `succeed`, `fail`, be `pending`, or `canceled`.
- The central unit of composition in ScalaTest is `Suite`, which represents a `suite of tests`.
- Trait `Suite` declares `run` and other "lifecycle" methods that define a default way to write and run tests.
- These lifecycle methods can be overridden to customize how tests are written and run.
- ScalaTest offers `style traits` that extend `Suite` and override lifecycle methods to support different testing styles.
- ScalaTest provides `mixin traits` that override lifecycle methods of the style traits to address particular testing needs.
- You define test classes by composing `Suite` style and mixin traits.
- You define test suites by composing `Suite `instances.

## TestSpec
ScalaTest is a testing toolkit: it consists of focused, lightweight traits that you can mix together to solve the problem at hand.
This approach minimizes the potential for naming and implicit conflicts and helps speed compiles.

Instead of duplicating code by mixing the same traits together repeatedly, we recommend you create abstract base classes
for your project that mix together the features you use the most. For example, you might create a TestSpec class
(not trait, for speedier compiles) for unit tests that looks like:

```scala
package com.github.dnvriend.component

import org.scalatest.{FlatSpec, Matchers, OptionValues}
import org.scalatestplus.play.WsScalaTestClient

abstract class TestSpec extends FlatSpec with Matchers with OptionValues with WsScalaTestClient
```

Most projects end up with multiple base classes, each focused on different kinds of tests. You might have a base class
for integration tests that require a database (perhaps named DbSpec), another for integration tests that require an
actor system (perhaps named ActorSysSpec), and another for integration test that require both a database and an actor system
(perhaps named DbActorSysSpec), and so on.

## Suite vs Spec
In ScalaTest, you define tests inside classes that extend a base class defined for your project like `TestSpec`.

```scala
class MyTest extends TestSpec {
  // tests go here...
}
```

Each `test` in `TestSpec` is composed of a sentence that specifies a bit of required behavior and a block of code that tests it.
The sentence needs a subject, such as "A Stack"; a verb, `should` and the rest of the sentence. Here's an example:

```scala
"A Stack" should "pop values in last-in-first-out order"
```

If you have multiple tests about the same subject, you can use `it` to refer to the previous subject:

```scala
it should "throw NoSuchElementException if an empty stack is popped"
```

After the sentence you put the word `in` followed by the body of the test in curly braces. Here's a complete example:

```scala
import collection.mutable.Stack

class StackTest extends TestSpec {

  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1)
    stack.push(2)
    assert(stack.pop() === 2)
    assert(stack.pop() === 1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[String]
    assertThrows[NoSuchElementException] {
      emptyStack.pop()
    }
  }
}
```

So StackTest contains multiple tests (collection of test) and is therefor called a `Suite`.

StackTest can be tested with `sbt`:

```
testOnly *StackTest
```

## OneAppPerSuite
The trait `org.scalatestplus.play.OneAppPerSuite` provides a new `Application` instance per ScalaTest `Suite`.
Know that a ScalaTest `Suite` is "a collection of zero to many tests" which means for the example above, the
application is shared between all tests defined in `StackTest` because StackTest is a Suite.

## OneAppPerTest
The trait `org.scalatestplus.play.OneAppPerTest` provides a new `Application` instance for each test.
Know that in ScalaTest a `Suite` is "a collection of zero to many tests" which means for the example above,
a new application is provided for each and every test in `StackTest`.

## OneBrowserPerSuite
The trait `org.scalatestplus.play.OneBrowserPerSuite` provides a new Selenium `WebDriver` instance for each test executed in a ScalaTest `Suite`.

## OneBrowserPerTest
The trait `org.scalatestplus.play.OneBrowserPerTest` creates a new `WebDriver` instance before each test, and ensure it
is closed after the test has completed. The `WebDriver` is available (implicitly) from method `webDriver`.

## Fixtures
A test fixture is composed of the objects and other artifacts (files, sockets, database connections, etc.) tests use to
do their work. When multiple tests need to work with the same fixtures, it is important to try and avoid duplicating
the fixture code across those tests. The more code duplication you have in your tests, the greater drag the tests will
have on refactoring the actual production. The [ScalaTest Sharing Fixtures](http://www.scalatest.org/user_guide/sharing_fixtures)
page explains several strategies for creating fixtures, I give you one more.

When structuring your application (architecture means 'structurizing a solution') using the 'components' ie. a
play application can be called a `container`, and a container consists of `components` and each component consists
of `classes`. Each component has a `public api` that must be provided by means of a `trait/interface`.

Other components can depend on that public interface to communicate with that component. Moveover, using Play/Guide,
the factory pattern can be leveraged to create the whole object graph that ultimately will become
"the-application-of-the-modelled-business-processes", when applied by some data eg. by a HTTP, Kafka or ActiveMQ request.

An application that has been structured using this `application -> (1:n) -> component -> (1:n) -> class` architecture
has an impact how to test it. Each component would have its own `ComponentTestSpec` and that testspec can have its own
fixtures, eg. put in its package object. These fixtures all start with the `withXYZ` keyword and return `curried` functions
rather than a `tuple` or the `single argument` strategy eg:

```scala
final case class Foo(x: Int)
final case class Bar(y: Int)
def withFooBar(f: Foo => Bar => Unit): Unit = {
    val foo = Foo(1)
    val bar = Bar(2)
    f(foo)(bar)
}
```

Such a fixture can be used like so:

```scala
it should "foobar" in withFooBar { foo => bar =>
  foo shouldBe Foo(1)
  bar shouldBe Bar(2)
}
```

The great thing about this is that:

- Typing is more efficient
- You have direct reference to the arguments

## Assertions
Apart from the Matchers, which are a great way to encode your test with assertions using an English DSL, ScalaTest makes
three assertions available by default in any style trait. You can use:

- __assert__: for general assertions
- __assertResult__: to differentiate expected from actual values
- __assertThrows__:  to ensure a bit of code throws an expected exception

ScalaTest's assertions are defined in trait `Assertions`, which is extended by `Suite`, the supertrait to __all style traits__.

Trait Assertions also provides:

- __assume__: to conditionally cancel a test
- __fail__: to fail a test unconditionally
- __cancel__: to cancel a test unconditionally
- __succeed__: to make a test succeed unconditionally
- __intercept__: to ensure a bit of code throws an expected exception and then make assertions about the exception
- __assertDoesNotCompile__: to ensure a bit of code does not compile
- __assertCompiles:__ to ensure a bit of code does compile
- __assertTypeError:__ to ensure a bit of code does not compile because of a type (not parse) error
- __withClue:__ to add more information about a failure

