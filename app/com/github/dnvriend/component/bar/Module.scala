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

package com.github.dnvriend.component.bar

import akka.actor.ActorRef
import com.github.dnvriend.component.client.echoservice.{EchoServiceClient, EchoServiceClientProvider}
import com.google.inject._
import com.google.inject.name.Names
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
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
  }
}
