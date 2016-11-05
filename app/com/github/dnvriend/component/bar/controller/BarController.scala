package com.github.dnvriend.component.bar.controller

import javax.inject._

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.github.dnvriend.component.bar.actor.DoBar
import com.google.inject.name.Named
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class BarController @Inject()( @Named("bar-actor") barActor: ActorRef)(implicit system: ActorSystem, ec: ExecutionContext, timeout: Timeout = Timeout(10.seconds)) extends Controller {
  def get = Action.async {
    (barActor ? DoBar(1)).map { result =>
      Ok(result.toString)
    }
  }
}
