package com.mfglabs
package models
package db

object StravaReadsQueries {

  import doobie.imports._
  import Strava._
  import Atoms._

  def activities: Query0[(Activity.Id, Activity)] = ???
}

trait StravaReads {
  import com.mfglabs.commons.Contexts._
  import com.mfglabs.precepte._, default._, Macros.callee

  val db: models.db.Database

  private val Q = StravaReadsQueries

}