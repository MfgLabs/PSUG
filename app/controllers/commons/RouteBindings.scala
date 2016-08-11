package com.mfglabs
package controllers

import play.api.mvc._
import java.time._
import java.time.format._

object RouteBindings {

  private def msg(t: String) = (key: String, e: Exception) => s"Cannot parse parameter $key as $t: ${e.getMessage}"

  private val localDateFormat = DateTimeFormatter.ISO_LOCAL_DATE
  implicit object localDateQSBindable extends QueryStringBindable.Parsing[LocalDate](
    s => LocalDate.parse(s, localDateFormat), i => localDateFormat.format(i), msg("LocalDate")
  )
}