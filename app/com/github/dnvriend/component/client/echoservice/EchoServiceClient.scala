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

package com.github.dnvriend.component.client.echoservice

import play.api.libs.json.Format

import scala.concurrent.Future

trait EchoServiceClient {
  def get(): Future[Int]

  def getTls(): Future[Int]

  def basicAuth(username: String, password: String): Future[Int]

  def basicAuthTls(username: String, password: String): Future[Int]

  def post[A: Format](a: A): Future[Int]

  def postTls[A: Format](a: A): Future[Int]

  def put[A: Format](a: A): Future[Int]

  def delete(): Future[Int]

  def patch[A: Format](a: A): Future[Int]

  def echo[A: Format](a: A): Future[A]
}
