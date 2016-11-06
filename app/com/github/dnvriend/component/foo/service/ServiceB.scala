package com.github.dnvriend.component.foo.service

import javax.inject._
import scala.concurrent._
import play.api.libs.ws._

trait ServiceB {
  def doB(): Future[Unit]
}

class ServiceBImpl @Inject() (ws: WSClient)(implicit ec: ExecutionContext) extends ServiceB {
  println("Creating B")
  override def doB(): Future[Unit] =
    Future.successful(println("doing B"))
}