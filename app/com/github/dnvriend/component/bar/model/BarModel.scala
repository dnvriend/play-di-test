package com.github.dnvriend.component.bar
package model

import akka.actor.{ActorLogging, ReceiveTimeout}
import akka.actor.SupervisorStrategy.Stop
import akka.cluster.sharding.ShardRegion.{HashCodeMessageExtractor, Passivate}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import com.github.dnvriend.component.bar.facade.BarFacade

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

object BarModel {
  def messageExtractor(maxNumberOfShards: Int) = new HashCodeMessageExtractor(maxNumberOfShards) {
    override def entityId(message: Any): String = message match {
      case MessageEnvelope(id, _) => id
    }
    override def entityMessage(message: Any): Any = message match {
      case MessageEnvelope(_, cmd) => cmd
    }
  }
}

class BarModel(pidName: String, facade: BarFacade, receiveTimeout: Duration)(implicit ec: ExecutionContext) extends PersistentActor with ActorLogging {
  override val persistenceId: String = s"$pidName-${self.path.name}"
  override def preStart(): Unit = {
    println(s"[$persistenceId] ==> Created a new BarModel, state is: $state")
    context.setReceiveTimeout(receiveTimeout)
  }

  var state = BarState(0)
  override def receiveRecover: Receive = {
    case event: BarEvent =>
      state = state.handleEvent(event)
    case RecoveryCompleted =>
      println(s"[$persistenceId] ==> RecoveryCompleted, state is: $state")
  }

  override def receiveCommand: Receive = {
    case cmd: BarCommand =>
      persist(facade.handleCommand(cmd)) { event =>
        val oldState = state
        state = state.handleEvent(event)
        println(s"[$persistenceId] ==> Handled state, old state is: $oldState, new state is: $state")
        sender() ! event
      }

    case ReceiveTimeout =>
      println(s"[$persistenceId] ==> ReceiveTimeout, state is: $state")
      context.parent ! Passivate(stopMessage = Stop)

    case Stop =>
      println(s"[$persistenceId] ==> Stopping, state is: $state")
      context.stop(self)
  }
}
