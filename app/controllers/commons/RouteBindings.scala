package com.mfglabs
package controllers

object RouteBindings {
  private def msg(t: String) = (key: String, e: Exception) => s"Cannot parse parameter $key as $t: ${e.getMessage}"
}