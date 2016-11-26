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

package com.github.dnvriend.component.cache

import akka.actor.Actor

object Cache {
  case object Clear
  case object Get
}

class Cache(name: String) extends Actor {
  override def receive: Receive = empty

  def dump(x: Any, ys: List[_]): String =
    s"""
    |===================================================================================
    |[Cache: $name]
    |===================================================================================
    |Received:
    |$x
    |===================================================================================
    |The cache contains:
    |${ys.mkString("\n-----\n")}
    |===================================================================================
  """.stripMargin

  def empty: Receive = {
    case Cache.Get =>
      sender() ! Nil
    case Cache.Clear =>
      sender() ! "ack"
    case x =>
      val ys = List(x)
      println(dump(x, ys))
      context.become(nonEmpty(ys))
      sender() ! x
  }

  def nonEmpty(xs: List[Any]): Receive = {
    case Cache.Get =>
      sender() ! xs
    case Cache.Clear =>
      context.become(empty)
      sender() ! "ack"
    case x =>
      val ys = xs :+ x
      println(dump(x, ys))
      context.become(nonEmpty(ys))
      sender() ! x
  }
}
