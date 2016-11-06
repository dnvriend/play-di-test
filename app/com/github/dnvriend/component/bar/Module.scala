package com.github.dnvriend.component.bar

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.util.Timeout
import com.github.dnvriend.component.bar.facade.BarFacade
import com.github.dnvriend.component.bar.model.BarModel
import com.google.inject._
import com.google.inject.name.Names
import play.api.libs.concurrent.AkkaGuiceSupport
import play.api.{Configuration, Environment}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class Module(environment: Environment, configuration: Configuration) extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    // bind ServiceB interface to the implementation, as alternative
    // to annotations
    bind(classOf[ActorRef])
      .annotatedWith(Names.named("bar-model"))
      .toProvider(classOf[BarModelProvider])
      .asEagerSingleton()
  }
}

@Singleton
class BarModelProvider @Inject() (facade: BarFacade, system: ActorSystem)(implicit ec: ExecutionContext) extends Provider[ActorRef] {
  val instance: ActorRef = ClusterSharding(system).start(
    typeName = "Bar",
    entityProps = Props(classOf[BarModel], "Bar", facade, Duration("10s"), ec),
    settings = ClusterShardingSettings(system),
    messageExtractor = BarModel.messageExtractor(maxNumberOfShards = 15)
  )
  override def get(): ActorRef = instance
}

@Singleton
class TimeoutProvider extends Provider[Timeout] {
  val instance = Timeout(10.seconds)

  override def get(): Timeout = instance
}

