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

import com.github.dnvriend.Base64Ops._
import com.github.dnvriend.component.client.ClientTestSpec
import com.github.dnvriend.component.client.jwt.model.{Session, Token}
import com.github.dnvriend.component.client.mock.{MockRequest, MockResponse}
import pdi.jwt.JwtAlgorithm
import play.api.libs.json.Json

class JwtAuthenticationClientTest extends ClientTestSpec {
  it should "should accept a non expired token received from the AuthProvider" in withPublicAndPrivateKeyAndJwtToken { publicKey => privateKey => jwtToken =>
    withJwtAuthenticationClient(MockResponse(200, Json.toJson(Token(jwtToken)).toString)) { (mockWsClientTp, client) =>

      // the header is base64 encoded and is transmitted as such
      // it can be easily decoded and inspected
      val header = jwtToken.split('.').head.parseBase64.asUtf8String
      header shouldBe """{"typ":"JWT","alg":"RS256"}"""

      // the payload is base64 encoded and is transmitted as such
      // it can be easily decoded and inspected
      val payload = jwtToken.split('.').drop(1).head.parseBase64.asUtf8String
      payload should include("""{"exp":""")

      val url = "http://www.auth.com"
      client.startSession(url, "username", "password", publicKey, JwtAlgorithm.RS256).futureValue should matchPattern {
        case Session(Token(_: String), _: Long) =>
      }

      // the wsClient should have sent a request
      mockWsClientTp.expectMsg(MockRequest("http://www.auth.com", "POST", """{"username":"username","password":"password"}""", Map("Accept" -> List("application/json"), "Content-Type" -> List("application/json"))))
    }
  }
}
