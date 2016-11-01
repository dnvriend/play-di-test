package com.github.dnvriend.component.foo.service

import scala.concurrent._
import play.api.libs.ws._

/**
 * No injection, this service is being provided by [[com.github.dnvriend.component.foo.ServiceCProvider]]
 */
trait ServiceC {
    def doC(): Future[Unit]
}

class ServiceCImpl(ws: WSClient)(implicit ec: ExecutionContext) extends ServiceC {
    println("Creating C")
    override def doC(): Future[Unit] = 
      Future.successful(println("doing C"))
}