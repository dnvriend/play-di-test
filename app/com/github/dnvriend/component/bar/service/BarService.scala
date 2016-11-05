package com.github.dnvriend.component.bar.service

import javax.inject._

import com.github.dnvriend.component.bar.actor._
import com.google.inject.ImplementedBy
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[BarServiceImpl])
trait BarService {
  def handleCommand(command: BarCommand): BarEvent
}

@Singleton
class BarServiceImpl @Inject()(wsClient: WSClient)(implicit ec: ExecutionContext) extends BarService {
  override def handleCommand(cmd: BarCommand): BarEvent = cmd match {
    case DoBar(x) =>
      // so some complicated logic here
      // call some subsystems
      // hey we're done!
      BarDone(x)
  }
}
