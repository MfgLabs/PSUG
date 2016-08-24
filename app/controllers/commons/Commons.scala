package com.mfglabs
package controllers

import commons._
import scala.concurrent.Future
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

  import jto.validation._
  import play.api.libs.json.JsValue
  import com.mfglabs.models.Helper
  import Metrics.Pre
  import com.mfglabs.precepte._
  import com.mfglabs.precepte.default._
  import corescalaz._
  import scalaz.syntax.applicative._

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

  def Api =
    new PreActionFunction[Request, Request, Future, Unit] {
      def invokeBlock[A](request: Request[A], block: Request[A] => Pre[Result]): Pre[Result] = {
        val r =
          for {
            t <- request.headers.get(CustomHeaders.API_KEY_HEADER_NAME)
          } yield block(request)
        r.getOrElse((Forbidden: Result).point[Pre])
      }
    }

  def Parsing[A](f: WithMCRequest[JsValue] => A => Pre[Result])(implicit callee: Callee, ctx: CtrlExeCtx, r: RuleLike[JsValue, A]) =
    syntax.async(parse.json)(Api andThen Timed) { mc =>
      From[JsValue, A](mc.request.body).fold(
        err => BadRequest(Helper.toJson(err)).point[Pre],
        a => f(mc)(a)
      )
    }
}