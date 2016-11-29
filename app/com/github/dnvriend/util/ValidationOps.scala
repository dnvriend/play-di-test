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

package com.github.dnvriend.util

import scala.concurrent.{ExecutionContext, Future}
import scalaz.Scalaz._
import scalaz._

object ValidationOps {
  implicit class ValidationNelToFuture[A](val that: ValidationNel[String, A]) extends AnyVal {
    def toFuture: Future[ValidationNel[String, A]] = Future.successful(that)
  }

  implicit class DisjunctionNelToString[A](val that: Disjunction[NonEmptyList[String], A]) extends AnyVal {
    def toFuture: Future[Disjunction[String, A]] = Future.successful(that.leftMap(_.toList.mkString(",")))
  }

  implicit class ValidationNelOps[A](val that: ValidationNel[String, A]) extends AnyVal {
    def toDisjunction: Future[Disjunction[String, A]] = that.disjunction.toFuture
    def liftEither: DisjunctionT[Future, String, A] = toDisjunction.liftEither
  }

  implicit class FutureDisjunctionOps[A](val that: Future[Disjunction[String, A]]) extends AnyVal {
    def liftEither: DisjunctionT[Future, String, A] = EitherT(that)
  }

  implicit class FutureSeqOps[A](val that: Future[Seq[A]]) extends AnyVal {
    def liftEither(implicit ec: ExecutionContext): DisjunctionT[Future, String, Seq[A]] = EitherT(that.map(_.right[String]))
  }

  implicit class FutureOptionOps[A](val that: Future[Option[A]]) extends AnyVal {
    def liftEither(msg: String)(implicit ec: ExecutionContext): DisjunctionT[Future, String, A] = EitherT(that.map(_.toRightDisjunction(msg)))
  }

  implicit class FutureUnitOps(val that: Future[Unit]) extends AnyVal {
    def liftEither(implicit ec: ExecutionContext): DisjunctionT[Future, String, Unit] =
      EitherT(that.map(_ => Disjunction.right[String, Unit](())))
  }
}
