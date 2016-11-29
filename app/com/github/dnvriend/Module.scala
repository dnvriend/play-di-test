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

import javax.inject.Inject

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.event.{Logging, LoggingAdapter}
import akka.pattern.CircuitBreaker
import akka.util.Timeout
import com.github.dnvriend.component.cache.Cache
import com.github.dnvriend.component.client.echoservice.{DefaultEchoServiceClient, EchoServiceClient}
import com.github.dnvriend.component.client.wsclient.WsClientProxy
import com.github.dnvriend.component.slick.SlickExecutionContext
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Provider, Provides}
import play.api.Logger
import play.api.libs.concurrent.AkkaGuiceSupport

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Module extends AbstractModule with AkkaGuiceSupport {
  val logger = Logger(this.getClass)

  override def configure(): Unit = {
    bind(classOf[Int])
      .annotatedWith(Names.named("test.port"))
      .toInstance(9001)

    bind(classOf[Timeout])
      .toInstance(Timeout(10.seconds))
  }

  /**
   * You don't really need this because Guice provides
   * java.util.logging.Logger that can be injected
   * into any class that needs a logger
   *
   * Also the best way to use logging is to use a combination
   * - When in Actors extend the trait `ActorLogging`
   * - When in Play then use `play.api.Logger` and use the companion object's method
   *   to log to the logger with name `Application`.
   * - If you want a named logger, then create a new logger by instantiating a play.api.Logger with
   *   the name of the class eg. val logger = Logger(this.getClass) and use that
   * - You could also use org.slf4j.Logger of course so instantiating that is also an option
   *
   * Phew, who knew logging could be so complicated
   */
  @Provides
  def loggingAdapter(system: ActorSystem): LoggingAdapter = {
    Logging(system, this.getClass)
  }

  /**
   * Only necessary if you use Slick. Having a choice is good and Play also supports
   * Anorm and ScalikeJdbc so choose your poison!
   */
  @Provides
  def slickExecutionContextProvider(system: ActorSystem): SlickExecutionContext = {
    val ec = system.dispatchers.lookup("slick.database-dispatcher")
    new SlickExecutionContext(ec)
  }
}

class CacheProvider(system: ActorSystem, cacheActorName: String) extends Provider[ActorRef] {
  override def get(): ActorRef =
    system.actorOf(Props(new Cache(cacheActorName)))
}

class EchoServiceClientProvider @Inject() (wsClient: WsClientProxy)(implicit system: ActorSystem, ec: ExecutionContext) extends Provider[EchoServiceClient] {
  override def get(): EchoServiceClient = {
    val breaker = new CircuitBreaker(system.scheduler, 5, 10.seconds, 1.minute)
    new DefaultEchoServiceClient(wsClient, breaker)
  }
}