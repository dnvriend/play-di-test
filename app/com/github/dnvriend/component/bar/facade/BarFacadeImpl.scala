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

package com.github.dnvriend.component.bar.facade

import com.github.dnvriend.component.bar.{BarCommand, BarDone, BarEvent, DoBar}
import com.google.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

@Singleton
private[facade] class BarFacadeImpl @Inject() (wsClient: WSClient)(implicit ec: ExecutionContext) extends BarFacade {
  override def handleCommand(cmd: BarCommand): BarEvent = cmd match {
    case DoBar(x) =>
      // so some complicated logic here
      // call some subsystems
      // hey we're done!
      BarDone(x)
  }
}
