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

package com.github.dnvriend.component.client

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.CircuitBreaker
import akka.testkit.TestProbe
import com.github.dnvriend.component.ComponentTestSpec
import com.github.dnvriend.component.client.jwt.mock.MockJwtAuthenticationClient
import com.github.dnvriend.component.client.jwt.model.{Session, SessionStateManager, Token}
import com.github.dnvriend.component.client.jwt.{DefaultJwtAuthenticationClient, DefaultSessionService, JwtAuthenticationClient, SessionService}
import com.github.dnvriend.component.client.mock.MockWsClientProxy
import pdi.jwt.JwtAlgorithm
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.WSResponse
import com.github.dnvriend.util.Base64Ops._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ClientTestSpec extends ComponentTestSpec {
  val session = Session(Token("token"), Long.MaxValue)

  def withCircuitBreaker(
    maxFailures: Int = Int.MaxValue,
    callTimeout: FiniteDuration = 24.hours,
    resetTimeout: FiniteDuration = 1.minute
  )(f: CircuitBreaker => Unit)(implicit system: ActorSystem, ec: ExecutionContext): Unit =
    f(new CircuitBreaker(system.scheduler, maxFailures, callTimeout, resetTimeout))

  def withSessionStateManager(session: Session, numFailures: Int)(f: (TestProbe, ActorRef) => Unit): Unit =
    withCircuitBreaker() { cb =>
      val jwtClientTp = TestProbe()
      val jwtClient = MockJwtAuthenticationClient(jwtClientTp.ref, session, numFailures)
      val ref = system.actorOf(Props(new SessionStateManager("", "", "", "", JwtAlgorithm.RS256, jwtClient, cb, 1.second)))
      try f(jwtClientTp, ref) finally killActors(ref)
    }

  def withSessionService(session: Session, numFailures: Int)(f: (TestProbe, SessionService) => Unit): Unit =
    withSessionStateManager(session, numFailures) { (jwtClientTp, sessionStateManager) =>
      val sessionService = new DefaultSessionService(sessionStateManager)
      f(jwtClientTp, sessionService)
    }

  def withJwtAuthenticationClient(response: WSResponse)(f: (TestProbe, JwtAuthenticationClient) => Unit) = {
    val mockWsClientTp = TestProbe()
    val mockWsClient = new MockWsClientProxy(mockWsClientTp.ref, response)
    val client = new DefaultJwtAuthenticationClient(mockWsClient, 1.minute)
    f(mockWsClientTp, client)
  }

  def withPublicAndPrivateKeyAndJwtToken(f: String => String => String => Unit): Unit = {
    // SHA-256 is a perfectly good secure hashing algorithm, quite suitable for use on certificates
    // 2048-bit RSA is a good signing algorithm (signing is not the same as encrypting).
    // Using 2048-bit RSA with SHA-256 is a secure signing scheme for a certificate.

    // RSASSA using SHA-256 algorithm
    // see: http://pauldijou.fr/jwt-scala/
    java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
    import java.security._
    import java.security.spec.{ECGenParameterSpec, PKCS8EncodedKeySpec, X509EncodedKeySpec}

    import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}

    def privateKeyToString(privateKey: PrivateKey): String = {
      val fact = KeyFactory.getInstance("RSA")
      val spec = fact.getKeySpec(privateKey, classOf[PKCS8EncodedKeySpec])
      spec.getEncoded().encodeBase64
    }

    def publicKeyToString(publicKey: PublicKey) = {
      val fact = KeyFactory.getInstance("RSA")
      val spec = fact.getKeySpec(publicKey, classOf[X509EncodedKeySpec])
      spec.getEncoded().encodeBase64
    }

    // We specify the curve we want to use
    val ecGenSpec = new ECGenParameterSpec("P-521")
    val generatorEC = KeyPairGenerator.getInstance("RSA")
    generatorEC.initialize(2048, new SecureRandom())
    val ecKey = generatorEC.generateKeyPair()
    val jwtToken = JwtJson.encode(JwtClaim(expiration = Option(scala.compat.Platform.currentTime)), ecKey.getPrivate, JwtAlgorithm.RS256)
    JwtJson.decode(jwtToken, ecKey.getPublic, Seq(JwtAlgorithm.RS256))
    val publicKey: String = publicKeyToString(ecKey.getPublic)
    val privateKey: String = privateKeyToString(ecKey.getPrivate)
    println(s"PublicKey:\n$publicKey\n")
    println(s"PrivateKey:\n$privateKey\n")
    println(s"JwtToken:\n$jwtToken\n")
    f(publicKey)(privateKey)(jwtToken)
  }

  def toJson[A: Format](a: A): String =
    Json.toJson(a).toString
}
