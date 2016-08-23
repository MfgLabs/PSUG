package com.mfglabs
package controllers

import commons._
import play.api.mvc._
import play.api.libs.json.{ Json, JsObject, JsNumber, JsString }
import play.api.libs.json._

import com.mfglabs.precepte._, default._, Macros.callee, corescalaz._

import jto.validation._
import playjson.Writes._

import models.Strava

case class Application(
    metrics: Metrics,
    conf: Conf,
    ctx: Contexts,
    db: models.db.Database,
    strava: models.StravaComponent
) extends Controller with Commons {

  import syntax._

  import ctx._
  import Contexts.ctrlToEC
  import scalaz.std.scalaFuture._

  def status = async(Timed) { _ =>
    db.test.map { _ =>
      Ok(Json.obj(
        "version" -> com.mfglabs.BuildInfo.version,
        "build-time" -> com.mfglabs.BuildInfo.builtAtString,
        "checks" -> JsObject(List(
          "name" -> JsString("demopsug (Postgresql)"),
          "test" -> JsString("SELECT 1"),
          "status" -> JsNumber(200),
          "message" -> JsString("")
        ))
      ))
    }
  }

  def load(token: Strava.Token) =
    async(Timed) { r =>
      import java.time.Instant
      import java.time.temporal.ChronoUnit._
      import scalaz.std.list._

      val from = Instant.now.minus(90, DAYS)
      val go =
        strava.allActivities(token)(from)
          .map(_.leftMap(_ => List[strava.ActivityAndId]()).merge)

      (for {
        _act <- trans(go)
        (id, act) = _act
        add = db.run(strava.add(id, act))(r.mc.st)
        _ <- trans(add.lift[List])
      } yield ()).run.map(_ => NoContent)
    }

  def list =
    async(Timed) { r =>
      for {
        acts <- strava.activities
      } yield Ok(Write[JsValue](acts))
    }

}
