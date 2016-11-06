package com.github.dnvriend.component.bar
package controller

import com.google.inject._

import akka.actor.ActorSystem
import akka.util.Timeout
import com.github.dnvriend.component.bar.service.BarService
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class BarController @Inject() (barService: BarService)(implicit system: ActorSystem, ec: ExecutionContext, timeout: Timeout = Timeout(10.seconds)) extends Controller {
  def get = Action.async {
    barService.process(DoBar(1)).map { result =>
      Ok(result.toString)
    }
  }
}
