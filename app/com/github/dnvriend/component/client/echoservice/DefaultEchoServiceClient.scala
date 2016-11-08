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

import play.api.libs.json.{Format, Json}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest}

import scala.concurrent.{ExecutionContext, Future}

private[echoservice] class DefaultEchoServiceClient @Inject() (wsClient: WSClient)(implicit ec: ExecutionContext) extends EchoServiceClient {
  override def get(): Future[Int] = {
    val request: WSRequest = wsClient.url(getUrl("/get"))
    request.get().map(_.status)
  }

  override def getTls(): Future[Int] =
    wsClient.url(getUrl("/get", tls = true)).get().map(_.status)

  override def basicAuth(username: String, password: String): Future[Int] =
    wsClient.url(getUrl("/basic-auth/foo/bar"))
      .withAuth(username, password, WSAuthScheme.BASIC)
      .get().map(_.status)

  override def basicAuthTls(username: String, password: String): Future[Int] =
    wsClient.url(getUrl("/basic-auth/foo/bar", tls = true))
      .withAuth(username, password, WSAuthScheme.BASIC)
      .get().map(_.status)

  override def post[A: Format](a: A): Future[Int] =
    wsClient.url(getUrl("/post"))
      .post(Json.toJson(a)).map(_.status)

  override def postTls[A: Format](a: A): Future[Int] =
    wsClient.url(getUrl("/post", tls = true))
      .post(Json.toJson(a)).map(_.status)

  override def put[A: Format](a: A): Future[Int] =
    wsClient.url(getUrl("/put"))
      .put(Json.toJson(a)).map(_.status)

  override def delete(): Future[Int] =
    wsClient.url(getUrl("/delete"))
      .delete().map(_.status)

  override def patch[A: Format](a: A): Future[Int] =
    wsClient.url(getUrl("/patch"))
      .patch(Json.toJson(a)).map(_.status)

  override def echo[A: Format](a: A): Future[A] =
    wsClient.url(getUrl("/post"))
      .post(Json.toJson(a))
      .map(response => (response.json \ "data").as[String])
      .map(jsonString => Json.parse(jsonString).as[A])
}
