package com.github.dnvriend.component.foo.service

import com.google.inject._
import scala.concurrent._
import play.api.libs.ws._
import akka.stream._
import akka.event._

@ImplementedBy(classOf[ServiceAImpl])
trait ServiceA {
  def doA(): Future[Unit]
}

@Singleton
class ServiceAImpl @Inject() (ws: WSClient, log: LoggingAdapter)(implicit ec: ExecutionContext, mat: Materializer) extends ServiceA {
  println("Creating A")
  override def doA(): Future[Unit] = {
    log.info("Doing A")
    Future.successful(println("doing A"))
  }
}