package com.github.dnvriend.component.jwt

import java.security._
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import com.github.nscala_time.time.Imports._

import com.github.dnvriend.util.Base64Ops._
import pdi.jwt.{JwtAlgorithm, JwtClaim, JwtJson}
import play.api.Logger

object Security {
  java.security.Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider())
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
    val jwtToken = JwtJson.encode(JwtClaim(expiration = Option((DateTime.now() + 1.week).getMillis)), privateKey, JwtAlgorithm.RS256)
    logger.debug(s"JwtToken:\n$jwtToken\n")
    jwtToken
  }

  def testJwtToken(jwtToken: String, publicKey: PublicKey) = {
    JwtJson.decode(jwtToken, publicKey, Seq(JwtAlgorithm.RS256))
  }

  def generateKeyPair: KeyPair = {
    val generatorEC = KeyPairGenerator.getInstance("RSA")
    generatorEC.initialize(2048, new SecureRandom())
    val keyPair: KeyPair = generatorEC.generateKeyPair()
    val publicKey: String = publicKeyToString(keyPair.getPublic)
    val privateKey: String = privateKeyToString(keyPair.getPrivate)
    logger.debug(s"PublicKey:\n$publicKey\n")
    logger.debug(s"PrivateKey:\n$privateKey\n")
    keyPair
  }
}
