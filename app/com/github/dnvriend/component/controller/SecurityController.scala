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

package com.github.dnvriend.component.controller

import java.text.SimpleDateFormat
import java.util.Date

import com.github.dnvriend.component.jwt.Security
import com.google.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._

object JwtSecurity {
  implicit val format = Json.format[JwtSecurity]
}
final case class JwtSecurity(publicKey: String, privateKey: String, jwtToken: String)

object Token {
  implicit val format = Json.format[Token]
}
final case class Token(token: String)

object TestToken {
  implicit val format = Json.format[TestToken]
}
final case class TestToken(token: String, publicKey: String)

object SecurityController {
  def format(time: Long): String =
    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date(time * 1000)) // times '1000' as time is in seconds
}

class SecurityController @Inject() (security: Security) extends Controller {
  val logger = Logger(this.getClass)

  def getJwtSecurity = Action {
    val keypair = security.generateKeyPair
    val publicKey = security.publicKeyToString(keypair.getPublic)
    val privateKey = security.generateJwtToken(keypair.getPrivate)
    val jwtToken: String = security.generateJwtToken(keypair.getPrivate)
    security.testJwtToken(jwtToken, keypair.getPublic).flatMap { jwtClaim =>
      logger.info(s"JwtClaim seems fine, ")
      jwtClaim.expiration.foreach { time =>
        logger.info(s"it will expire at ${SecurityController.format(time)}")
      }
      security.decode(jwtToken, publicKey).map { _ =>
        logger.info("Your token seems fine, put the publicKey in a .pub file")
        Ok(Json.toJson(JwtSecurity(publicKey, privateKey, jwtToken)))
      }
    }.recover {
      case t: Throwable =>
        InternalServerError(t.getClass.getName + " - " + t.getMessage)
    }.getOrElse(InternalServerError("Unknown error"))
  }

  def testToken = Action { request =>
    logger.info("Testing your jwtToken with publicKey")
    request.body.asJson.map(_.as[TestToken]).flatMap { testToken =>
      logger.info("Got token: " + testToken)
      security.decode(testToken.token, testToken.publicKey).map { jwtClaim =>
        logger.info("Got jwtClaim " + jwtClaim)
        jwtClaim.expiration.foreach { time =>
          logger.info(s"it will expire at ${SecurityController.format(time)}")
        }
        logger.info("Your token seems fine, put the publicKey in a .pub file")
        Ok(Json.toJson(testToken))
      }.recover {
        case t: Throwable =>
          BadRequest("Test failed: " + t.getMessage)
      }.toOption
    }.getOrElse(InternalServerError("Unknown error"))
  }
}