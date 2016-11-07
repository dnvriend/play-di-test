package com.github.dnvriend.component

import org.scalatest.{FlatSpec, Matchers, OptionValues}
import org.scalatestplus.play.WsScalaTestClient

abstract class TestSpec extends FlatSpec with Matchers with OptionValues with WsScalaTestClient {

}
