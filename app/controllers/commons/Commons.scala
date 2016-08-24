package com.mfglabs
package controllers

import commons._
import Contexts.{ CtrlExeCtx, ctrlToEC }
import scalaz.std.scalaFuture._
import com.mfglabs.precepte.default.Callee

object CustomHeaders {
  val API_KEY_HEADER_NAME = "mesh-api-key"
}

import play.api.mvc._

trait Commons {
  self: Controller =>

  val metrics: Metrics
  val conf: Conf

  def Timed(implicit callee: Callee, ctx: CtrlExeCtx) =
    TimedAction(metrics, conf)

  import com.mfglabs.precepte._
  import com.mfglabs.precepte.default._

  private val initialState = ()
  private def version = default.Version(BuildInfo.version)
  private def environment =
    conf.environment match {
      case Env.Staging => Environment.Staging
      case Env.Prod => Environment.Production
      case _ => Environment.Dev
    }
  private def host = default.Host(java.net.InetAddress.getLocalHost().getHostName())

  val syntax = PreActionSyntax(initialState, version, environment, host)
}