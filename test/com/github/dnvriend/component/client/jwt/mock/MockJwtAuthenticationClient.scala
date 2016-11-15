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

package com.github.dnvriend.component.client.jwt.mock

import akka.actor.ActorRef
import com.github.dnvriend.component.client.jwt.JwtAuthenticationClient
import com.github.dnvriend.component.client.jwt.exception.AuthenticationException
import com.github.dnvriend.component.client.jwt.model.Session
import pdi.jwt.algorithms.JwtRSAAlgorithm

import scala.concurrent.Future

final case class StartSession(authProviderHost: String, username: String, password: String, publicKey: String, algorithm: JwtRSAAlgorithm)

case class MockJwtAuthenticationClient(tp: ActorRef, response: Session, numFailures: Int) extends JwtAuthenticationClient {
  var failures: Int = 0
  override def startSession(authProviderHost: String, username: String, password: String, publicKey: String, algorithm: JwtRSAAlgorithm): Future[Session] = {
    tp ! StartSession(authProviderHost, username, password, publicKey, algorithm)
    if (numFailures == Int.MaxValue) Future.failed(AuthenticationException("Mock failure"))
    else if (failures < numFailures) {
      failures += 1
      Future.failed(AuthenticationException("Mock failure"))
    } else Future.successful(response)
  }
}
