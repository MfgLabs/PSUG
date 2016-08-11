package com.mfglabs
package controllers

import commons._
import play.api.mvc._
import play.api.libs.json.{ Json, JsObject, JsNumber, JsString }

import com.mfglabs.precepte._, default._, Macros.callee, corescalaz._
import play.api.libs.json._

case class Application(
    metrics: Metrics,
    conf: Conf,
    ctx: Contexts,
    db: models.db.Database
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
}
