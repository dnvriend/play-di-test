package com.github.dnvriend.component.controller

import scalaz._
import Scalaz._

object ControllerValidator {
  def validateUUID(fieldName: String, value: String): ValidationNel[String, String] =
    Validation.fromTryCatchNonFatal(java.util.UUID.fromString(value))
      .leftMap(t => s"Field '$fieldName' is not a UUID, its current value is: '$value'. The underlying error is: '${t.toString}'".wrapNel).rightMap(_ => value)

  def validateNonEmpty(fieldName: String, value: String): ValidationNel[String, String] =
    if (value.trim.isEmpty)
      s"Field '$fieldName' is empty".failureNel[String]
    else
      value.successNel[String]

  def validateNonEmptyAndUUID(fieldName: String, value: String): ValidationNel[String, String] =
    List(
      validateNonEmpty(fieldName, value),
      validateUUID(fieldName, value)
    ).sequenceU.rightMap(_ => value)

  def validateNonZero(fieldName: String, value: Long): ValidationNel[String, Long] =
    if (value == 0) s"Field '$fieldName' with value '$value' may not be zero".failureNel[Long] else value.successNel[String]

  def validateNonNegative(fieldName: String, value: Long): ValidationNel[String, Long] =
    if (value < 0) s"Field '$fieldName' with value '$value' may not be less than zero".failureNel[Long] else value.successNel[String]
}
