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

package com.github.dnvriend.component.client.jwt.model

import akka.actor.{Actor, ActorLogging, Stash}
import akka.pattern.{CircuitBreaker, pipe}
import com.github.dnvriend.component.client.jwt.JwtAuthenticationClient
import com.github.dnvriend.component.client.jwt.exception.AuthenticationException
import pdi.jwt.algorithms.JwtRSAAlgorithm

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object SessionStateManager {
  case object StartSession
  case object GetSession
  case class SessionError(message: String)
}

class SessionStateManager(authProviderHost: String, username: String, password: String, publicKey: String, algorithm: JwtRSAAlgorithm, jwtAuthenticationClient: JwtAuthenticationClient, breaker: CircuitBreaker, scheduleDelay: FiniteDuration)(implicit ec: ExecutionContext) extends Actor with Stash with ActorLogging {
  import SessionStateManager._
  override def receive: Receive = waitToken
  override def preStart(): Unit =
    self ! StartSession

  def waitToken: Receive = {
    case StartSession =>
      breaker.withCircuitBreaker(jwtAuthenticationClient.startSession(authProviderHost, username, password, publicKey, algorithm)).recover {
        case AuthenticationException(message) => SessionError(message)
        case throwable                        => SessionError(throwable.getMessage)
      }.pipeTo(self)

    case session: Session =>
      log.info(s"Token received:\n{}\nSession started.", session.token)
      context.become(authenticated(session))
      unstashAll()

    case SessionError(message) =>
      log.error(s"Service authentication failed {} It will be retried in {}", message, scheduleDelay)
      context.system.scheduler.scheduleOnce(scheduleDelay, self, StartSession)

    case _ =>
      log.info(s"Request for session received while waiting for token, stashed request")
      stash()
  }

  def authenticated(session: Session): Receive = {
    case _ if session.isExpired =>
      log.info(s"Session is expired, starting a new session.")
      stash()
      self ! StartSession
      context.become(waitToken)

    case _ =>
      sender() ! session
  }
}
