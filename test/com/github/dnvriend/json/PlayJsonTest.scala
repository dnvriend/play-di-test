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

package com.github.dnvriend.json

import com.github.dnvriend.TestSpec
import com.github.dnvriend.model.{Foo, Person}
import play.api.data.validation.ValidationError
import play.api.libs.json._

// https://www.playframework.com/documentation/2.5.x/ScalaJson
class PlayJsonTest extends TestSpec {
  final val JsonString = """{"name":"foo","age":25}"""

  it should "convert to Json AST" in {
    Json.toJson(Person("foo", 25)) shouldBe a[JsValue]
  }

  it should "marshall Person to json string" in {
    Json.toJson(Person("foo", 25)).toString() shouldBe JsonString
  }

  it should "marshall Person to json string with stringify" in {
    Json.stringify(Json.toJson(Person("foo", 25))) shouldBe JsonString
  }

  it should "marshal Option.empty[Person]" in {
    Json.toJson(Option.empty[Person]).toString shouldBe "null"
  }

  it should "marshal Some(Person)" in {
    Json.toJson(Some(Person("foo", 25))).toString shouldBe JsonString
  }

  it should "marshal a List.empty[Person]" in {
    Json.toJson(List.empty[Person]).toString shouldBe "[]"
  }

  it should "marshal a List[Person]" in {
    Json.toJson(List(Person("foo", 25), Person("bar", 30))).toString shouldBe """[{"name":"foo","age":25},{"name":"bar","age":30}]"""
  }

  it should "unmarshal JSON to Person" in {
    Json.parse(JsonString).as[Person] shouldBe Person("foo", 25)
  }

  it should "unmarshal JSON to Option[Person]" in {
    Json.parse(JsonString).asOpt[Person] shouldBe Option(Person("foo", 25))
  }

  it should "unmarshal JSON to Option[Foo] but fails with None" in {
    Json.parse(JsonString).asOpt[Foo] shouldBe None
  }

  it should "create a new Json structure" in {
    val json: JsValue = Json.parse(JsonString)
    Json.stringify(JsObject(Seq("data" -> json))) shouldBe """{"data":{"name":"foo","age":25}}"""
  }

  it should "parse watership down" in {
    Json.parse(
      """
        |{
        |  "name" : "Watership Down",
        |  "location" : {
        |    "lat" : 51.235685,
        |    "long" : -1.309197
        |  },
        |  "residents" : [ {
        |    "name" : "Fiver",
        |    "age" : 4,
        |    "role" : null
        |  }, {
        |    "name" : "Bigwig",
        |    "age" : 6,
        |    "role" : "Owsla"
        |  } ]
        |}
      """.stripMargin
    ) shouldBe a[JsValue]

    JsObject(Seq(
      "name" -> JsString("Watership Down"),
      "location" -> JsObject(Seq("lat" -> JsNumber(51.235685), "long" -> JsNumber(-1.309197))),
      "residents" -> JsArray(Seq(
        JsObject(Seq(
          "name" -> JsString("Fiver"),
          "age" -> JsNumber(4),
          "role" -> JsNull
        )),
        JsObject(Seq(
          "name" -> JsString("Bigwig"),
          "age" -> JsNumber(6),
          "role" -> JsString("Owsla")
        ))
      ))
    )) shouldBe a[JsValue]

    Json.obj(
      "name" -> "Watership Down",
      "location" -> Json.obj("lat" -> 51.235685, "long" -> -1.309197),
      "residents" -> Json.arr(
        Json.obj(
          "name" -> "Fiver",
          "age" -> 4,
          "role" -> JsNull
        ),
        Json.obj(
          "name" -> "Bigwig",
          "age" -> 6,
          "role" -> "Owsla"
        )
      )
    ) shouldBe a[JsValue]
  }

  //  it should "be an applicative" in {
  //    val validJson = """{"name":"foo","age":25}"""
  //    Json.parse(validJson).validate[Person] shouldBe JsSuccess(Person("foo", 25))
  //    val invalidJson = """{"name":"foo","age":"bar"}"""
  //    Json.parse(invalidJson).validate[Person] shouldBe a[JsError]
  //  }
}
