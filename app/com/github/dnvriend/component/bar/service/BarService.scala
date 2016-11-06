package com.github.dnvriend.component.bar
package service

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.google.inject._
import com.google.inject.name.Named

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@ImplementedBy(classOf[BarServiceImpl])
trait BarService {
  def process(cmd: BarCommand): Future[BarEvent]
}

@Singleton
class BarServiceImpl @Inject() (@Named("bar-model") barActor: ActorRef)(implicit system: ActorSystem, ec: ExecutionContext, mat: Materializer, timeout: Timeout) extends BarService {
  // poor man's typed actors
  override def process(cmd: BarCommand): Future[BarEvent] =
    (barActor ? MessageEnvelope(Random.nextInt(10).toString, DoBar(1))).mapTo[BarEvent]
}
