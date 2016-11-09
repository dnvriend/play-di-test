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

package com.github.dnvriend

import akka.actor.{ActorRef, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.util.Timeout
import com.github.dnvriend.component.bar.BarModelProvider
import com.github.dnvriend.component.client.echoservice.{EchoServiceClient, EchoServiceClientProvider}
import com.github.dnvriend.component.foo.ServiceCProvider
import com.github.dnvriend.component.foo.actor.FooActor
import com.github.dnvriend.component.foo.service.{ServiceB, ServiceBImpl, ServiceC}
import com.github.dnvriend.component.slick.SlickExecutionContext
import com.google.inject.{AbstractModule, Provides}
import com.google.inject.name.Names
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.duration._

class Module extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    // bind ServiceB interface to the implementation, as alternative
    // to annotations
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("bar-model"))
      .toProvider(classOf[BarModelProvider])
      .asEagerSingleton()

    //    bind(classOf[Boolean])
    //      .annotatedWith(Names.named("clustered-bar-model"))
    //      .toInstance(false)

    bind(classOf[EchoServiceClient])
      .toProvider(classOf[EchoServiceClientProvider])
      .asEagerSingleton()

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
   * java.util.logging.Logger that can be injected
   * into any class that needs a logger
   */
  @Provides
  def loggingAdapter(system: ActorSystem): LoggingAdapter = {
    println(" !! ==> !! Providing a LoggingAdapter")
    Logging(system, this.getClass)
  }

  @Provides
  def slickExecutionContextProvider(system: ActorSystem): SlickExecutionContext = {
    println(" !! ==> !! Providing a SlickExecutionContext")
    val ec = system.dispatchers.lookup("slick.database-dispatcher")
    new SlickExecutionContext(ec)
  }
}

//class FooActorProvider @Inject() (system: ActorSystem)(implicit ec: ExecutionContext) extends Provider[ActorRef] {
//  val instance = system.actorOf(Props(new FooActor))
//  override def get(): ActorRef = instance
//}

//class TimeoutProvider extends Provider[Timeout] {
//  val instance = Timeout(10.seconds)
//  override def get(): Timeout = instance
//}

//class LoggingAdapterProvider @Inject() (system: ActorSystem) extends Provider[LoggingAdapter] {
//  val instance = Logging(system, this.getClass)
//  override def get(): LoggingAdapter = instance
//}
