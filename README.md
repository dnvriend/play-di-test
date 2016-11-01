# play-di-test

[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

A small study project on how play's dependency injection framework works.

# Launch
To launch the project type:

```bash
sbt run
http :9000
```

# Architecture
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


