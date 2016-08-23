package com.mfglabs
package controllers

import play.api.mvc._
import models.Strava

object RouteBindings {
  private def msg(t: String) = (key: String, e: Exception) => s"Cannot parse parameter $key as $t: ${e.getMessage}"

  implicit object stravaTokenQSBindable extends QueryStringBindable.Parsing[Strava.Token](
    s => Strava.Token(s), _.value, msg("token")
  )
}