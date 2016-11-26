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

package com.github.dnvriend.component.client.echoservice

import javax.inject.Inject

import akka.pattern.CircuitBreaker
import com.github.dnvriend.component.client.wsclient.WsClientProxy
import play.api.Logger
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.WSAuthScheme

import scala.concurrent.{ExecutionContext, Future}

class DefaultEchoServiceClient @Inject() (wsClient: WsClientProxy, breaker: CircuitBreaker)(implicit ec: ExecutionContext) extends EchoServiceClient {
  val logger = Logger(this.getClass)

  override def get(): Future[Int] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/get"))
        .flatMap(_.get().map(_.status))
    )

  override def getTls(): Future[Int] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/get", tls = true))
        .flatMap(_.get().map(_.status))
    )

  override def basicAuth(username: String, password: String): Future[Int] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/basic-auth/foo/bar"))
        .flatMap(_.withAuth(username, password, WSAuthScheme.BASIC).get().map(_.status))
    )

  override def basicAuthTls(username: String, password: String): Future[Int] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/basic-auth/foo/bar", tls = true))
        .flatMap(_.withAuth(username, password, WSAuthScheme.BASIC).get().map(_.status))
    )

  override def post[A: Format](a: A): Future[Int] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/post"))
        .flatMap(_.post(Json.toJson(a)).map(_.status))
    )

  override def postTls[A: Format](a: A): Future[Int] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/post", tls = true))
        .flatMap(_.post(Json.toJson(a)).map(_.status))
    )

  override def put[A: Format](a: A): Future[Int] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/put"))
        .flatMap(_.put(Json.toJson(a)).map(_.status))
    )

  override def delete(): Future[Int] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/delete"))
        .flatMap(_.delete().map(_.status))
    )

  override def patch[A: Format](a: A): Future[Int] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/patch"))
        .flatMap(_.patch(Json.toJson(a)).map(_.status))
    )

  override def echo[A: Format](a: A): Future[A] =
    breaker.withCircuitBreaker(
      wsClient.url(getUrl("/post"))
        .flatMap(_.post(Json.toJson(a))
          .map(response => (response.json \ "data").as[String])
          .map(jsonString => Json.parse(jsonString).as[A]))
    )
}