package com.mfglabs
package commons

import com.mfglabs.precepte._

case class MonitoringContext(st: default.ST[Unit]) {
  lazy val logback = Logback(env, "application")
  val env = st.managed.env
  val span = st.managed.span
  val path = st.managed.path
  lazy val logger = logback.Logger(span, path)
}

object MonitoringContext {
  def build(st: default.ST[Unit]): MonitoringContext = MonitoringContext(st)
}