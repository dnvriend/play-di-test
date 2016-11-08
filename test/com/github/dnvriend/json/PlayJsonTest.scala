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
import com.github.dnvriend.model.Person
import play.api.libs.json.Json

class PlayJsonTest extends TestSpec {
  final val JsonString = """{"name":"foo","age":25}"""

  it should "marshall Person" in {
    Json.toJson(Person("foo", 25)).toString() shouldBe JsonString
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
}
