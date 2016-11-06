package com.github.dnvriend.component

package object bar {
  trait ShardMessage
  final case class MessageEnvelope(id: String, cmd: BarCommand) extends ShardMessage

  trait BarCommand
  final case class DoBar(x: Int) extends BarCommand

  trait BarEvent
  final case class BarDone(x: Int) extends BarEvent
}
