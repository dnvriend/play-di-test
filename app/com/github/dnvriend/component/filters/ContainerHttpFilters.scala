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

package com.github.dnvriend.component.filters

import akka.stream.Materializer
import com.google.inject.Inject
import play.api.Logger
import play.api.http.DefaultHttpFilters
import play.api.mvc.{Filter, RequestHeader, Result, Results}
import com.github.dnvriend.util.Base64Ops._

import scalaz._
import Scalaz._
import scala.compat.Platform
import scala.concurrent.{ExecutionContext, Future}

class ContainerHttpFilters @Inject() (
  loggingFilter: LoggingFilter,
  basicAuthFilter: BasicAuthFilter
) extends DefaultHttpFilters(loggingFilter, basicAuthFilter)

object BasicAuthFilter {
  final val UnAuthorized: Result =
    Results.Unauthorized.withHeaders(("WWW-Authenticate", "Basic realm=\"myRealm\""))

  def validateAuthHeader(authHeader: Option[String]): ValidationNel[String, String] =
    authHeader.toSuccessNel("No authorization header found")

  def validateAuth(auth: String): ValidationNel[String, String] =
    Option(auth).filter(_.trim.nonEmpty).toSuccessNel("Empty authentication string")

  def decodeAuth(auth: String): ValidationNel[String, String] =
    Validation.fromTryCatchNonFatal(auth.substring(6).parseBase64Str)
      .leftMap(t => s"Error decoding auth string '$auth': $t".wrapNel)

  def splitAuth(auth: String): ValidationNel[String, (String, String)] =
    Option(auth.split(":"))
      .filter(_.length == 2)
      .map(x => (x.head, x.drop(1).head))
      .toSuccessNel(s"Could not split auth: '$auth'")

  def validateUsername(username: String): ValidationNel[String, String] =
    Option(username).filter(_ == "username").toSuccessNel(s"Username '$username' is invalid")

  def validatePassword(password: String): ValidationNel[String, String] =
    Option(password).filter(_ == "password").toSuccessNel(s"Password '$password' is invalid")

  def validateAuthSequenced(auth: String): ValidationNel[String, (String, String)] =
    BasicAuthFilter.validateAuth(auth) *> BasicAuthFilter.splitAuth(auth)
}

class BasicAuthFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  override def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    val result = for {
      raw <- BasicAuthFilter.validateAuthHeader(requestHeader.headers.get("Authorization")).disjunction
      auth <- BasicAuthFilter.decodeAuth(raw).disjunction
      sequencedResult <- BasicAuthFilter.validateAuthSequenced(auth).disjunction
      (toValidateUsername, toValidatePassword) = sequencedResult
      username <- BasicAuthFilter.validateUsername(toValidateUsername).disjunction
      password <- BasicAuthFilter.validatePassword(toValidatePassword).disjunction
    } yield (username, password)

    result.rightMap {
      case (username, password) =>
        nextFilter(requestHeader.copy(headers = requestHeader.headers.add("auth" -> s"$username, $password")))
    }.leftMap { xs =>
      println("Errors: " + "\n- " + xs.toList.mkString("\n- "))
    }.getOrElse(Future.successful(BasicAuthFilter.UnAuthorized))
  }
}

class LoggingFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter {
  val logger = Logger(this.getClass)

  override def apply(nextFilter: (RequestHeader) => Future[Result])(requestHeader: RequestHeader): Future[Result] = {

    val startTime = Platform.currentTime
    nextFilter(requestHeader).map { result =>
      val endTime = Platform.currentTime
      val requestTime = endTime - startTime

      logger.info(s"${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms and returned ${result.header.status}")
      Logger.info(s"${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms and returned ${result.header.status}")
      println(s"${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms and returned ${result.header.status}")
      result
        .withHeaders("Request-Time" -> requestTime.toString)
    }
  }
}

