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

      val from = Instant.now.minus(90, DAYS)

      for {
        acts <- strava.allActivities(token)(from)
      } yield {
        acts.leftMap { err =>
          r.mc.logger.info("error while loading strava info", Macros.params(err))
          InternalServerError
        }.map { m =>
          Ok(Write[JsValue](m))
        }.merge
      }
    }

}
