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

package com.github.dnvriend.component

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.github.dnvriend.TestSpec
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import play.api.mvc.Results

import scala.concurrent.ExecutionContext

trait ControllerSpec extends TestSpec with BeforeAndAfterAll with ScalaFutures with Results {
  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = system.dispatcher

  override protected def afterAll(): Unit = {
    system.terminate().futureValue
  }
}
