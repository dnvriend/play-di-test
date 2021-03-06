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

package com.github.dnvriend.component.util

import java.io.{ByteArrayInputStream, InputStream}
import java.util.Base64

object Base64Ops {
  implicit class ByteArrayImplicits(val that: Array[Byte]) extends AnyVal {
    def encodeBase64: String = Base64.getEncoder.encodeToString(that)
    def toInputStream: InputStream = new ByteArrayInputStream(that)
    def asUtf8String: String = new String(that, "UTF-8")
  }

  implicit class StringImplicits(val that: String) extends AnyVal {
    def parseBase64: Array[Byte] = Base64.getDecoder.decode(that)
  }
}
