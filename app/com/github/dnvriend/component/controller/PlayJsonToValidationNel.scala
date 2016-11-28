package com.next.cp.om.port.adapter.controller.validation

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsResult}

import scalaz._
import Scalaz._

object PlayJsonToValidation {

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
}