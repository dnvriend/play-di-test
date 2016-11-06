package com.github.dnvriend.component.bar
package model

final case class BarState(x: Int) {
  def handleEvent(event: BarEvent): BarState = event match {
    case BarDone(value) => this.copy(x = x + value)
  }
}
