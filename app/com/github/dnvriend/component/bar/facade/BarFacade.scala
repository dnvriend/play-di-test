package com.github.dnvriend.component.bar
package facade

import com.google.inject._
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[BarFacadeImpl])
trait BarFacade {
  def handleCommand(command: BarCommand): BarEvent
}

@Singleton
class BarFacadeImpl @Inject() (wsClient: WSClient)(implicit ec: ExecutionContext) extends BarFacade {
  override def handleCommand(cmd: BarCommand): BarEvent = cmd match {
    case DoBar(x) =>
      // so some complicated logic here
      // call some subsystems
      // hey we're done!
      BarDone(x)
  }
}
