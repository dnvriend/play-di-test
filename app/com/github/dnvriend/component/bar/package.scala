/*
 * Copyright 2016 Dennis Vriend
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.dnvriend.component

package object bar {
  trait ShardMessage
  final case class MessageEnvelope(id: String, cmd: BarCommand) extends ShardMessage

  trait BarCommand
  final case class DoBar(x: Int) extends BarCommand

  trait BarEvent
  final case class BarDone(x: Int) extends BarEvent
}
