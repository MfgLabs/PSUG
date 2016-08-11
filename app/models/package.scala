package com.mfglabs
package models

import play.api.libs.json.{ JsValue, JsObject }
import jto.validation._
import playjson.Writes._
import validations._

object Helper {

  def toJson(errs: Seq[(Path, Seq[ValidationError])]): JsValue = {
    (Path \ "errors").write[Seq[(Path, Seq[ValidationError])], JsObject].writes(errs)
  }
}