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

package com.github.dnvriend.component.bar
package controller

import com.google.inject._
import akka.actor.ActorSystem
import akka.util.Timeout
import com.github.dnvriend.component.bar.service.BarService
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class BarController @Inject() (barService: BarService)(implicit system: ActorSystem, ec: ExecutionContext, timeout: Timeout = Timeout(10.seconds)) extends Controller {
  def get = Action.async {
    barService.process(DoBar(1)).map {
      case event: BarDone => Ok(Json.toJson(event))
    }
  }
}
