package com.mfglabs
package models
package db

object StravaReadsQueries {

  import doobie.imports._
  import Strava._
  import Atoms._

  def activities =
    sql"""
      SELECT id, start_date, athlete, distance
      FROM strava.activities
    """.query[(Activity.Id, Activity)]
}

trait StravaReads {
  import com.mfglabs.commons.Contexts._
  import com.mfglabs.precepte._, default._, Macros.callee

  val db: models.db.Database

  private val Q = StravaReadsQueries

  def activities(implicit ec: DBExeCtx) =
    db.WithCon { mc =>
      mc.logger.debug("listing all activities")
      Q.activities.list
    }
}