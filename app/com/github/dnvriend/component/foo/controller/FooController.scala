
package com.github.dnvriend.component.foo.controller

import javax.inject._
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem
import com.github.dnvriend.component.foo.facade._

class FooController @Inject() (facade: FooFacade)(implicit system: ActorSystem, ec: ExecutionContext) extends Controller {
  def get = Action.async {
    facade.foo().map { _ =>
      Ok(s"foo, ${ec.getClass.getName}, ${system.getClass.getName}")
    }
  }
}