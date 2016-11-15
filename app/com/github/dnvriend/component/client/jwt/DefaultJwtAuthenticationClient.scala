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

import java.util.logging.Logger

import com.github.dnvriend.component.client.jwt.exception.AuthenticationException
import com.github.dnvriend.component.client.jwt.model.{Credentials, Session, Token}
import com.github.dnvriend.component.client.wsclient.WsClientProxy
import pdi.jwt.JwtJson
import pdi.jwt.algorithms.JwtRSAAlgorithm
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object DefaultJwtAuthenticationClient {
  def makeRequest(request: WSRequest, username: String, password: String, publicKey: String, algorithm: JwtRSAAlgorithm, requestTimeout: FiniteDuration)(implicit ec: ExecutionContext): Future[Session] = {
    request.withHeaders("Accept" -> "application/json")
      .withRequestTimeout(requestTimeout)
      .post(usernamePasswordClaim(username, password))
      .flatMap(mapAuthenticateResponse)
      .flatMap(token => decode(token, publicKey, algorithm))
      .recoverWith {
        case throwable =>
          Future.failed(AuthenticationException(s"Making request failed: ${throwable.getMessage}"))
      }
  }

  def decode(token: String, publicKey: String, algorithm: JwtRSAAlgorithm)(implicit ec: ExecutionContext): Future[Session] = {
    val decodedSession: Try[Session] = JwtJson.decode(token, publicKey, Seq(algorithm)).map { jwtClaim =>
      Session(Token(token), jwtClaim.expiration.getOrElse(0))
    }
    Future.fromTry(decodedSession).recoverWith {
      case throwable =>
        Future.failed(AuthenticationException(s"Decoding token failed: ${throwable.getMessage}"))
    }
  }

  def usernamePasswordClaim(username: String, password: String): JsValue =
    Json.toJson(Credentials(username, password))

  def mapAuthenticateResponse(response: WSResponse): Future[String] =
    if (response.status == Status.OK)
      response.json.asOpt[Token]
        .map(token => Future.successful(token.token))
        .getOrElse(Future.failed(AuthenticationException(s"Token deserialization failed: ${response.body}")))
    else Future.failed(AuthenticationException(s"Creating token call failed with status code ${response.status}"))
}

class DefaultJwtAuthenticationClient(wsClient: WsClientProxy, requestTimeout: FiniteDuration)(implicit ec: ExecutionContext) extends JwtAuthenticationClient {
  override def startSession(url: String, username: String, password: String, publicKey: String, algorithm: JwtRSAAlgorithm): Future[Session] = {
    wsClient.url(url)
      .flatMap(request => DefaultJwtAuthenticationClient.makeRequest(request, username, password, publicKey, algorithm, requestTimeout))
  }
}
