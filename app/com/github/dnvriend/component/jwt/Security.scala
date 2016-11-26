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

package com.github.dnvriend.component.jwt

import java.security._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}

import com.github.nscala_time.time.Imports._
import com.github.dnvriend.util.Base64Ops._
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.Logger

import scala.util.{Failure, Try}

object Security {
  java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
  final val Algorithm = JwtAlgorithm.RS256
}

// SHA-256 is a perfectly good secure hashing algorithm, quite suitable for use on certificates
// 2048-bit RSA is a good signing algorithm (signing is not the same as encrypting).
// Using 2048-bit RSA with SHA-256 is a secure signing scheme for a certificate.

// RSASSA using SHA-256 algorithm
// see: http://pauldijou.fr/jwt-scala/
// see: https://github.com/pauldijou/jwt-scala
class Security {
  val logger = Logger(this.getClass)

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

  def generateJwtToken(privateKey: PrivateKey): String = {
    val expiration: Long = (DateTime.now() + 1.week).getMillis
    val jwtToken = JwtJson.encode(JwtClaim(expiration = Option(expiration / 1000)), privateKey, Security.Algorithm)
    logger.debug(s"JwtToken:\n$jwtToken\n")
    jwtToken
  }

  def testJwtToken(jwtToken: String, publicKey: PublicKey): Try[JwtClaim] = {
    JwtJson.decode(jwtToken, publicKey, Seq(Security.Algorithm))
  }

  def generateKeyPair: KeyPair = {
    val generatorEC = KeyPairGenerator.getInstance("RSA")
    generatorEC.initialize(2048, new SecureRandom())
    val keyPair: KeyPair = generatorEC.generateKeyPair()
    keyPair
  }

  def isExpired(jwtClaim: JwtClaim): Boolean = {
    def expired(expirationTimeInSeconds: Long): Boolean =
      expirationTimeInSeconds < (DateTime.now().getMillis / 1000)
    jwtClaim.expiration.exists(expired)
  }

  def decode(token: String, publicKey: String): Try[JwtClaim] = {
    JwtJson.decode(token, publicKey, Seq(Security.Algorithm)).map { jwtClaim =>
      logger.info("JwtClaim: " + jwtClaim)
      jwtClaim
    }.recoverWith {
      case t: Throwable =>
        logger.error(s"Decoding token failed: ${t.getMessage}", t)
        Failure(t)
    }
  }
}
