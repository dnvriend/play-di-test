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

package com.github.dnvriend.component.client.jwt

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.github.dnvriend.component.client.jwt.model.{Session, SessionStateManager}

import scala.concurrent.Future

class DefaultSessionService(sessionStateManager: ActorRef)(implicit timeout: Timeout) extends SessionService {
  override def getSession(): Future[Session] =
    (sessionStateManager ? SessionStateManager.GetSession).mapTo[Session]
}
