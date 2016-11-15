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

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, PoisonPill}
import akka.stream.scaladsl.Source
import akka.stream.testkit.TestSubscriber
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorMaterializer, Materializer}
import akka.testkit.TestProbe
import akka.util.Timeout
import com.github.dnvriend.TestSpec
import org.scalatest.BeforeAndAfterAll

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

abstract class ComponentTestSpec extends TestSpec with BeforeAndAfterAll {
  implicit var system: ActorSystem = _
  implicit var ec: ExecutionContext = _
  implicit var mat: Materializer = _
  implicit val pc: PatienceConfig = PatienceConfig(timeout = 5.seconds)
  implicit val timeout = Timeout(5.seconds)

  def randomId = UUID.randomUUID.toString

  implicit class PimpedByteArray(self: Array[Byte]) {
    def getString: String = new String(self)
  }

  implicit class PimpedFuture[T](self: Future[T]) {
    def toTry: Try[T] = Try(self.futureValue)
  }

  implicit class SourceOps[A](src: Source[A, _]) {
    def testProbe(f: TestSubscriber.Probe[A] => Unit): Unit =
      f(src.runWith(TestSink.probe(system)))
  }

  def killActors(actors: ActorRef*): Unit = {
    val tp = TestProbe()
    actors.foreach { (actor: ActorRef) =>
      tp watch actor
      actor ! PoisonPill
      tp.expectTerminated(actor)
    }
  }

  override protected def beforeAll(): Unit = {
    system = ActorSystem()
    ec = system.dispatcher
    mat = ActorMaterializer()
  }

  override protected def afterAll(): Unit = {
    system.terminate().toTry should be a 'success
  }
}
