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

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.github.dnvriend.component.bar.facade.BarFacade
import com.github.dnvriend.component.bar.model.BarModel
import com.google.inject.{Inject, Provider}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

class BarModelProvider @Inject() (facade: BarFacade, system: ActorSystem)(implicit ec: ExecutionContext) extends Provider[ActorRef] {
  val instance: ActorRef = ClusterSharding(system).start(
    typeName = "Bar",
    entityProps = Props(classOf[BarModel], "Bar", facade, Duration("10s"), ec),
    settings = ClusterShardingSettings(system),
    messageExtractor = BarModel.messageExtractor(maxNumberOfShards = 15)
  )
  override def get(): ActorRef = instance
}
