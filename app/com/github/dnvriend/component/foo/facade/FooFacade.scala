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