package com.mfglabs

package object commons {
  object Env extends Enumeration {
    type Env = Value
    val Local, Dev, Test, Staging, Prod = Value
  }

  implicit val semiUnit = new scalaz.Semigroup[Unit] {
    def append(f1: Unit, f2: => Unit): Unit = ()
  }
}