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

package com.github.dnvriend.component.bar
package service

import java.util.logging.Logger

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.stream.Materializer
import akka.util.Timeout
import com.github.dnvriend.component.bar.dao.BarDao
import com.google.inject._
import com.google.inject.name.Named

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@ImplementedBy(classOf[BarServiceImpl])
trait BarService {
  def process(cmd: BarCommand): Future[BarEvent]
}

@Singleton
class BarServiceImpl @Inject() (@Named("bar-model") barActor: ActorRef, dao: BarDao, logger: Logger)(implicit system: ActorSystem, ec: ExecutionContext, mat: Materializer, timeout: Timeout) extends BarService {
  // poor man's typed actors
  override def process(cmd: BarCommand): Future[BarEvent] = for {
    now <- dao.now
    event <- (barActor ? MessageEnvelope(Random.nextInt(10).toString, DoBar(Random.nextInt(20)))).mapTo[BarEvent]
  } yield {
    println(s"[BarService] ==> According to H2, its now: $now, the received event is: $event")
    event
  }
}
