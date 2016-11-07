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

package com.github.dnvriend.component.bar.dao

import javax.inject._

import com.github.dnvriend.component.slick.SlickExecutionContext
import com.google.inject.ImplementedBy
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

@ImplementedBy(classOf[SlickBarDao])
trait BarDao {
  def now: Future[String]
}

/**
 * The database connection is opened in the usual way. All Plain SQL queries result in `DBIOActions`
 * that can be composed and run like any other action.
 *
 * Plain SQL queries in Slick are built via string interpolation using the `sql`, `sqlu` and `tsql`
 * interpolators which return  a `SQLActionBuilder`. They are available through the standard api._
 * imported from a Slick driver like eg: import slick.driver.PostgresDriver.api._`
 *
 * The following string interpolators are available:
 *
 *  - sql: The sql interpolator which returns a result set produced by a statement. The interpolator by itself
 *         does not produce a DBIO value. It needs to be followed by a call to .as to define the row type.
 *  - sqlu: The sqlu interpolator is used for DML statements which produce a row count instead of a result set.
 *          Therefore they are of type DBIO[Int].
 *  - tsql: Builds an invoker for a statement with computed types
 */
private[dao] class SlickBarDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: SlickExecutionContext) extends BarDao with HasDatabaseConfigProvider[JdbcProfile] {
  import driver.api._
  override def now: Future[String] =
    db.run(sql"""SELECT NOW()""".as[String]).map(_.head)
}
