package com.github.dnvriend.component.bar.actor

import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.github.dnvriend.component.bar.service.BarService

import scala.concurrent.ExecutionContext

trait BarCommand
final case class DoBar(x: Int) extends BarCommand

trait BarEvent
final case class BarDone(x: Int) extends BarEvent

class BarActor(service: BarService)(implicit ec: ExecutionContext) extends PersistentActor {
  override val persistenceId: String = "bar"

  var recoverState = BarState(0)
  override val receiveRecover: Receive = {
    case event: BarEvent => recoverState =
      recoverState.handleEvent(event)
    case RecoveryCompleted =>
      println("==> RecoveryCompleted: " + recoverState)
      context.become(handleCommand(recoverState))
  }

  override val receiveCommand: Receive = PartialFunction.empty

  def handleCommand(state: BarState): Receive = {
    case cmd: BarCommand => persist(service.handleCommand(cmd)) { event =>
      sender() ! "ack"
      val newState = state.handleEvent(event)
      println("==> Handled state, new state is: " + newState)
      context.become(handleCommand(newState))
    }
  }
}
