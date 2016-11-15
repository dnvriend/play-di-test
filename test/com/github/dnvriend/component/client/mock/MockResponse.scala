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

package com.github.dnvriend.component.client.mock

import akka.util.ByteString
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSCookie, WSResponse}

import scala.xml.{Elem, XML}

case class MockResponse(status: Int, body: String, allHeaders: Map[String, Seq[String]] = Map.empty) extends WSResponse {
  override def underlying[T]: T = {
    println("underlying")
    ???
  }

  override def statusText: String = {
    println("statusText")
    ""
  }

  override def header(key: String): Option[String] = {
    println("header for key: " + key)
    allHeaders.get(key).map(_.mkString)
  }

  override def cookies: Seq[WSCookie] = {
    println("cookies")
    Seq.empty
  }

  override def cookie(name: String): Option[WSCookie] = {
    println("cookie for name: " + name)
    None
  }

  override def xml: Elem = {
    println("xml")
    XML.loadString(body)
  }

  override def json: JsValue = {
    println("json")
    Json.parse(body)
  }

  override def bodyAsBytes: ByteString = {
    println("bodyAsBytes")
    ByteString(body)
  }
}
