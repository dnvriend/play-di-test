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

package com.github.dnvriend.controller

import akka.stream.Materializer
import com.github.dnvriend.TestSpec
import com.github.dnvriend.component.ControllerSpec
import com.github.dnvriend.controller.mock.ExampleController
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Future

class ControllerTest extends ControllerSpec {
  "Example page index" should "be valid" in {
    val controller = new ExampleController()
    val result: Future[Result] = controller.index().apply(FakeRequest())
    val bodyText: String = contentAsString(result)
    bodyText shouldBe "ok"
  }

  /**
   * An EssentialAction underlies every Action.
   * Given a RequestHeader, an EssentialAction consumes
   * the request body (a ByteString) and returns a Result.
   *
   * An EssentialAction is a Handler, which means
   * it is one of the objects that Play uses to handle requests.
   */
  "an essential action" should "parse a JSON body" in {
    val action: EssentialAction = Action { request =>
      val value = (request.body.asJson.get \ "field").as[String]
      Ok(value)
    }

    val request = FakeRequest(POST, "/").withJsonBody(Json.parse("""{ "field": "value" }"""))
    val result = call(action, request)

    status(result) shouldBe OK
    contentAsString(result) shouldBe "value"
  }
}
