package com.github.dnvriend.component.bar

import akka.actor.{ActorRef, ActorSystem, Props}
import com.github.dnvriend.component.bar.actor.BarActor
import com.github.dnvriend.component.bar.service.BarService
import com.google.inject._
import com.google.inject.name.Names
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}

import scala.concurrent.ExecutionContext

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    // bind ServiceB interface to the implementation, as alternative
    // to annotations
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("bar-actor"))
      .toProvider(classOf[BarActorProvider])
      .asEagerSingleton()
  }
}

@Singleton
class BarActorProvider @Inject() (service: BarService, system: ActorSystem)(implicit ec: ExecutionContext) extends Provider[ActorRef] {
  val instance = system.actorOf(Props(new BarActor(service)))
  override def get(): ActorRef = instance
}
