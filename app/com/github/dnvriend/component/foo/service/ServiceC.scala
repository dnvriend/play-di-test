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

import scala.concurrent._
import play.api.libs.ws._

/**
 * No injection, this service is being provided by [[com.github.dnvriend.component.foo.ServiceCProvider]]
 */
trait ServiceC {
  def doC(): Future[Unit]
}

class ServiceCImpl(ws: WSClient)(implicit ec: ExecutionContext) extends ServiceC {
  println("Creating C")
  override def doC(): Future[Unit] =
    Future.successful(println("doing C"))
}
