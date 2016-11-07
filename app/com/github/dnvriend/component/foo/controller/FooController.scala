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

package com.github.dnvriend.component.foo.controller

import javax.inject._

import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import akka.actor.ActorSystem
import com.github.dnvriend.component.bar.DoBar
import com.github.dnvriend.component.bar.service.BarService
import com.github.dnvriend.component.foo.facade._

import scala.util.Random

class FooController @Inject() (facade: FooFacade, barService: BarService)(implicit system: ActorSystem, ec: ExecutionContext) extends Controller {
  def get = Action.async {
    barService.process(DoBar(Random.nextInt(100))).flatMap { event =>
      facade.foo().map { _ =>
        Ok(s"foo, ${ec.getClass.getName}, ${system.getClass.getName}, $event")
      }
    }
  }
}
