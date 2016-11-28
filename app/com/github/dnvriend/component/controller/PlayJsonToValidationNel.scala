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

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsResult}

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz._

object PlayJsonToValidationNel {

  def jsResultToValidationNel[A](jsResult: JsResult[A]): ValidationNel[String, A] = {
    def validationToString(path: JsPath, xs: Seq[ValidationError]): String = {
      val pathString = path.toString
      val errorsString = xs.flatMap(_.messages).mkString(",")
      s"'$pathString', '$errorsString'"
    }
    def validationErrorsToString(xs: Seq[(JsPath, Seq[ValidationError])]): String =
      xs.map((validationToString _).tupled).mkString(",")

    jsResult.asEither
      .validation
      .leftMap(validationErrorsToString)
      .leftMap(_.wrapNel)
  }

  implicit class JsResultOps[A](val that: JsResult[A]) extends AnyVal {
    def toValidationNel: ValidationNel[String, A] =
      jsResultToValidationNel(that)
  }

  implicit class OptionJsResultOps[A](val that: Option[JsResult[A]]) extends AnyVal {
    def toValidationNel: ValidationNel[String, A] =
      that.map(jsResultToValidationNel).getOrElse("No JsResult to validate".failureNel[A])
  }

  implicit class ValidationNelToFuture[A](val that: ValidationNel[String, A]) extends AnyVal {
    def toFuture: Future[ValidationNel[String, A]] = Future.successful(that)
  }

  implicit class DisjunctionNelToString[A](val that: Disjunction[NonEmptyList[String], A]) extends AnyVal {
    def toFuture: Future[Disjunction[String, A]] = Future.successful(that.leftMap(_.toList.mkString(",")))
  }

  implicit class ValidationNelOps[A](val that: ValidationNel[String, A]) extends AnyVal {
    def toDisjunction: Future[Disjunction[String, A]] = that.disjunction.toFuture
    def liftEither: DisjunctionT[Future, String, A] = toDisjunction.liftEither
  }

  implicit class FutureDisjunctionOps[A](val that: Future[Disjunction[String, A]]) extends AnyVal {
    def liftEither: DisjunctionT[Future, String, A] = EitherT(that)
  }

  implicit class FutureSeqOps[A](val that: Future[Seq[A]]) extends AnyVal {
    def liftEither(implicit ec: ExecutionContext): DisjunctionT[Future, String, Seq[A]] = EitherT(that.map(_.right[String]))
  }

  implicit class FutureOptionOps[A](val that: Future[Option[A]]) extends AnyVal {
    def liftEither(msg: String)(implicit ec: ExecutionContext): DisjunctionT[Future, String, A] = EitherT(that.map(_.toRightDisjunction(msg)))
  }
}