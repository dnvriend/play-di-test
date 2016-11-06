package com.github.dnvriend.component.foo.actor

import scala.concurrent._
import akka.actor._
import akka.event._

class FooActor(implicit ec: ExecutionContext) extends Actor with ActorLogging {
  println("Creating FooActor")
  override def receive = LoggingReceive {
    case str =>
      log.info("Received: {}", str)
      sender() ! str
  }
}