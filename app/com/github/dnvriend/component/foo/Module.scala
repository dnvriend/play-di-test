/*
 * Copyright 2016 Dennis Vriend
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

package com.github.dnvriend.component.foo

import akka.actor._
import akka.event._
import akka.util.Timeout
import com.github.dnvriend.component.foo.actor._
import com.github.dnvriend.component.foo.service._
import com.google.inject.{Inject, _}
import com.google.inject.name._
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.libs.ws._
import play.api.{Configuration, Environment}

import scala.concurrent._
import scala.concurrent.duration._

/**
 * This class is a Guice module that tells Guice how to bind several
 * different types. This Guice module is created when the Play
 * application starts.
 *
 * Play will automatically use any class called `Module` that is in
 * the root package. You can create modules in other locations by
 * adding `play.modules.enabled` settings to the `application.conf`
 * configuration file.
 *
 * environment: The environment for the application. Captures concerns relating to the classloader and the filesystem for the application.
 * configuration: The typesafe configuration
 */
class Module(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    // bind ServiceB interface to the implementation, as alternative to annotations
    bind(classOf[ServiceB]).to(classOf[ServiceBImpl]).asEagerSingleton()
    bind(classOf[ServiceC]).toProvider(classOf[ServiceCProvider])
    //    bind(classOf[Timeout]).toProvider(classOf[TimeoutProvider])
    //    bind(classOf[LoggingAdapter]).toProvider(classOf[LoggingAdapterProvider])
    bind(classOf[Timeout]).toInstance(Timeout(10.seconds))
    //    bind(classOf[ActorRef])
    //      .annotatedWith(Names.named("foo-actor"))
    //      .toProvider(classOf[FooActorProvider])
    //      .asEagerSingleton()
    bindActor[FooActor]("foo-actor")
  }

  /**
   * You don't really need this because Guice provides
   * [[java.util.logging.Logger]] that can be injected
   * into any class that needs a logger
   */
  @Provides
  def loggingAdapter(system: ActorSystem): LoggingAdapter =
    Logging(system, this.getClass)
}

@Singleton
class ServiceCProvider @Inject() (ws: WSClient)(implicit ec: ExecutionContext) extends Provider[ServiceC] {
  val instance = new ServiceCImpl(ws)
  override def get(): ServiceC = instance
}

//@Singleton
//class FooActorProvider @Inject() (system: ActorSystem)(implicit ec: ExecutionContext) extends Provider[ActorRef] {
//  val instance = system.actorOf(Props(new FooActor))
//  override def get(): ActorRef = instance
//}

//@Singleton
//class TimeoutProvider extends Provider[Timeout] {
//  val instance = Timeout(10.seconds)
//  override def get(): Timeout = instance
//}

//@Singleton
//class LoggingAdapterProvider @Inject() (system: ActorSystem) extends Provider[LoggingAdapter] {
//  val instance = Logging(system, this.getClass)
//  override def get(): LoggingAdapter = instance
//}
