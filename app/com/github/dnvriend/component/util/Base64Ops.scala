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