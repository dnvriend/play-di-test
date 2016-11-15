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

import akka.testkit.TestProbe
import com.github.dnvriend.component.client.ClientTestSpec
import com.github.dnvriend.component.client.jwt.mock.StartSession
import pdi.jwt.JwtAlgorithm

import scala.concurrent.duration._

class SessionStateManagerTest extends ClientTestSpec {
  it should "return a session when a session could successfully be retrieved" in withSessionStateManager(Session(Token("token"), Long.MaxValue), numFailures = 0) { (jwtClientTp, mgr) =>
    val tp = TestProbe()
    tp.send(mgr, SessionStateManager.GetSession)
    tp.expectMsg(Session(Token("token"), Long.MaxValue))
    jwtClientTp.expectMsg(StartSession("", "", "", "", JwtAlgorithm.RS256))

    tp.send(mgr, SessionStateManager.GetSession)
    tp.expectMsg(Session(Token("token"), Long.MaxValue))
    jwtClientTp.expectNoMsg(300.millis)
  }

  it should "retry requesting a session when there is an error" in withSessionStateManager(session, numFailures = 1) { (jwtClientTp, mgr) =>
    val tp = TestProbe()
    tp.send(mgr, SessionStateManager.GetSession)
    tp.expectMsg(session)
    jwtClientTp.expectMsg(StartSession("", "", "", "", JwtAlgorithm.RS256))
  }
}
