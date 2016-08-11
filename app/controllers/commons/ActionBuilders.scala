package com.mfglabs

import scala.concurrent.{ Future, ExecutionContext }

import play.api.mvc._

import com.mfglabs.commons.{ Metrics, Conf }
import com.mfglabs.precepte._
import commons.MonitoringContext
import default._
import com.mfglabs.precepte._
import com.mfglabs.precepte.default._

package controllers {
  case class WithMCRequest[A](mc: MonitoringContext, request: Request[A]) extends WrappedRequest[A](request)
}

package object controllers {

  import Metrics.Pre

  def TimedAction(metrics: Metrics, _conf: Conf)(implicit callee: Callee, M: scalaz.Monad[Future], ec: ExecutionContext) =
    new PreActionFunction[Request, WithMCRequest, Future, Unit] {
      def invokeBlock[A](request: Request[A], block: WithMCRequest[A] => Pre[Result]) =
        metrics.TimedM(Category.Api) { st =>
          block(WithMCRequest(st, request))
        }
    }
}