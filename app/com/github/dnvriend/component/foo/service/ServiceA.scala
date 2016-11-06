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

package com.github.dnvriend.component.foo.service

import com.google.inject._
import scala.concurrent._
import play.api.libs.ws._
import akka.stream._
import akka.event._

@ImplementedBy(classOf[ServiceAImpl])
trait ServiceA {
  def doA(): Future[Unit]
}

@Singleton
class ServiceAImpl @Inject() (ws: WSClient, log: LoggingAdapter)(implicit ec: ExecutionContext, mat: Materializer) extends ServiceA {
  println("Creating A")
  override def doA(): Future[Unit] = {
    log.info("Doing A")
    Future.successful(println("doing A"))
  }
}
