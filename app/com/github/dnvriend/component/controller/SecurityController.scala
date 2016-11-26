package com.github.dnvriend.component.controller

import com.github.dnvriend.component.jwt.Security
import com.google.inject.Inject
import play.api.libs.json.Json
import play.api.mvc._

object JwtSecurity {
  implicit val format = Json.format[JwtSecurity]
}

final case class JwtSecurity(publicKey: String, privateKey: String, jwtToken: String)

class SecurityController @Inject()(security: Security) extends Controller {
  def getJwtSecurity = Action {
    val keypair = security.generateKeyPair
    Ok(Json.toJson(JwtSecurity(
      security.privateKeyToString(keypair.getPrivate),
      security.publicKeyToString(keypair.getPublic),
      security.generateJwtToken(keypair.getPrivate)
    )))
  }
}
