package com.mfglabs
package models

import play.api.libs.json._
import jto.validation._
import playjson.Rules._
import validations._
import java.time.Instant

object Strava {

  case class ClientId(value: String) extends AnyVal
  case class ClientSecret(value: String) extends AnyVal
  case class Token(value: String) extends AnyVal

  case class Error(status: org.http4s.Status, message: JsValue)

  object Activity {
    case class Id(value: Long) extends AnyVal
    case class Date(value: Instant) extends AnyVal

    implicit val activityR =
      From[JsValue] { __ =>
        (
          (__ \ "id").read[Id] ~
          (__ \ "start_date").read[Date]
        ).tupled
      }
  }
}

import org.http4s._
import org.http4s.client._
import scalaz.std.string._
import syntax._
import scala.concurrent.Future
import scalaz.concurrent.Task
import scalaz.{ \/, -\/, \/- }

object WSCommons {
  def collectJsValue(msg: Message): DecodeResult[JsValue] =
    DecodeResult.success(Json.parse(msg.bodyAsText.foldMonoid.runLastOr("").unsafePerformSync)) // XXX: assume valid json

  implicit val jsvalueDecoder = EntityDecoder.decodeBy(MediaRange.`*/*`)(collectJsValue) // */
}

case class StravaComponent(
    conf: commons.Conf,
    client: Client,
    metrics: commons.Metrics,
    ctx: commons.Contexts
) {

  import com.mfglabs.precepte._, default._, corescalaz._, Macros.callee
  import scalaz.syntax.applicative._
  import commons.Metrics.Pre
  import scalaz.std.scalaFuture._
  import commons.Contexts._

  type ActivityDated = (Strava.Activity.Id, Strava.Activity.Date)

  def Timed[A](f: commons.MonitoringContext => Future[A])(implicit ctx: WSExeCtx, callee: Callee) =
    metrics.Timed(commons.Metrics.StravaCat)(f)

  def TimedM[A](f: commons.MonitoringContext => Pre[A])(implicit ctx: WSExeCtx, callee: Callee) =
    metrics.TimedM(commons.Metrics.StravaCat)(f)

  import org.http4s.Status.ResponseClass.Successful
  import ctx._
  import WSCommons._

  private def fetch[T](request: Task[Request])(f: Response => Task[T]): Pre[Strava.Error \/ T] =
    Timed { mc =>
      val result =
        client.fetch(request) {
          case Successful(res) =>
            f(res).map(\/-.apply _)
          case res =>
            mc.logger.error("Strava error", Macros.params(res))
            Task.now(-\/(Strava.Error(res.status, res.as[JsValue].unsafePerformSync)))
        }
      result.runFuture()
    }

  def activities(token: Strava.Token)(after: Instant): Pre[Strava.Error \/ List[ActivityDated]] =
    TimedM { mc =>
      mc.logger.debug("fetching Strava activities", Macros.params(after))
      val request = external.StravaRoutes.activities(conf.Strava.apiActivitiesPerPage)(token)(after)
      fetch(Task.now(request))(_.as[JsValue].map { (js: JsValue) =>
        Read[List[ActivityDated]](js).toOption.get.toList
      })
    }

  // load Activities Job.
  def allActivities(token: Strava.Token)(after: Instant): Pre[Strava.Error \/ List[ActivityDated]] =
    TimedM { mc =>
      mc.logger.debug("getting all Strava activities", Macros.params(after))
      def go(a: Instant): Pre[Strava.Error \/ List[ActivityDated]] =
        activities(token)(a).flatMap {
          case \/-(as) if as.length == conf.Strava.apiActivitiesPerPage =>
            val last = as.last._2
            go(last.value).map(_.map(as ::: _))
          case as => as.point[Pre]
        }
      go(after)
    }

}