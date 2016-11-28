package com.github.dnvriend.component.controller

import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Action, Controller}

import scala.concurrent.{ExecutionContext, Future}
import scalaz._

abstract class ValidatingController extends Controller {
  def handleAndRespond[A: Writes](disjunction: Future[Disjunction[String, A]])(implicit ec: ExecutionContext) =
    Action.async(
      disjunction.map {
        case DRight(value)  => Ok(Json.toJson(value))
        case DLeft(message) => BadRequest(message)
      }
    )
}
