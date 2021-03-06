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
import com.github.dnvriend.component.client.wsclient.WsClientProxy
import play.api.libs.ws.{WSRequest, WSResponse}

import scala.concurrent.Future

class MockWsClientProxy(tp: ActorRef, response: WSResponse) extends WsClientProxy {
  override def url(url: String): Future[WSRequest] = {
    Future.successful(MockWSRequest(tp, response, url))
  }
}
