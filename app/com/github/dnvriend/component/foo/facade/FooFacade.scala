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

package com.github.dnvriend.component.foo.facade

import com.google.inject._
import com.google.inject.name._
import scala.concurrent._
import play.api.libs.ws._
import com.github.dnvriend.component.foo.service._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor._

// see: https://www.playframework.com/documentation/2.5.x/ScalaDependencyInjection#Binding-annotations
// see: https://github.com/google/guice/wiki/BindingAnnotations
@ImplementedBy(classOf[FooFacadeImpl])
trait FooFacade {
  def foo(): Future[Unit]
}

@Singleton
class FooFacadeImpl @Inject() (ws: WSClient, serviceA: ServiceA, serviceB: ServiceB, serviceC: ServiceC, @Named("foo-actor") fooActor: ActorRef)(implicit ec: ExecutionContext, timeout: Timeout) extends FooFacade {
  override def foo(): Future[Unit] = for {
    _ <- serviceA.doA()
    _ <- serviceB.doB()
    _ <- serviceC.doC()
    _ <- fooActor ? "foo"
  } yield ()
}
