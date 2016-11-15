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

package com.github.dnvriend.component.client.mock

import akka.actor.ActorRef
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws._

import scala.concurrent.Future
import scala.concurrent.duration.Duration

final case class MockRequest(url: String, method: String, body: String, headers: Map[String, Seq[String]] = Map.empty)

final case class MockWSRequest(tp: ActorRef, response: WSResponse, url: String, method: String = "", body: WSBody = EmptyBody, headers: Map[String, Seq[String]] = Map.empty) extends WSRequest {
  override val queryString: Map[String, Seq[String]] = Map.empty
  override val calc: Option[WSSignatureCalculator] = None
  override val auth: Option[(String, String, WSAuthScheme)] = None
  override val followRedirects: Option[Boolean] = None
  override val requestTimeout: Option[Int] = None
  override val virtualHost: Option[String] = None
  override val proxyServer: Option[WSProxyServer] = None

  override def execute(): Future[WSResponse] = {
    println("execute")
    val request = body match {
      case b: InMemoryBody =>
        val request = MockRequest(url, method, new String(b.bytes.toArray, "UTF-8"), headers)
        tp ! request
        request
      case EmptyBody =>
        val request = MockRequest(url, method, "", headers)
        tp ! request
        request
    }
    println("Request: " + request)
    println("Response: " + response)
    Future.successful(response)
  }

  override def sign(calc: WSSignatureCalculator): WSRequest = {
    println("sign")
    this
  }

  override def withAuth(username: String, password: String, scheme: WSAuthScheme): WSRequest = {
    println("withAuth")
    this
  }

  override def withHeaders(hdrs: (String, String)*): WSRequest = {
    println("withHeaders: " + hdrs)
    val m = hdrs.toList.foldLeft(headers) { (c, e) => c ++ Map(e._1 -> (c.withDefaultValue(Seq.empty[String])(e._1) :+ e._2)) }
    copy(headers = m)
  }

  override def withQueryString(parameters: (String, String)*): WSRequest = {
    println("withQueryString")
    this
  }

  override def withFollowRedirects(follow: Boolean): WSRequest = {
    println("withFollowRedirects")
    this
  }

  override def withRequestTimeout(timeout: Duration): WSRequest = {
    println("withRequestTimeout: " + timeout)
    this
  }

  override def withRequestFilter(filter: WSRequestFilter): WSRequest = {
    println("withRequestFilter")
    this
  }

  override def withVirtualHost(vh: String): WSRequest = {
    println("withVirtualHost")
    this
  }

  override def withProxyServer(proxyServer: WSProxyServer): WSRequest = {
    println("withProxyServer")
    this
  }

  override def withBody(body: WSBody): WSRequest = {
    println("withBody: " + body.getClass.getName)
    copy(body = body)
  }

  override def withMethod(method: String): WSRequest = {
    println("withMethod: " + method)
    copy(method = method)
  }

  override def stream(): Future[StreamedResponse] = {
    println("stream")
    ???
  }

  @scala.deprecated("Use `WS.stream()` instead.")
  override def streamWithEnumerator(): Future[(WSResponseHeaders, Enumerator[Array[Byte]])] = {
    println("streamWithEnumerator")
    ???
  }
}
