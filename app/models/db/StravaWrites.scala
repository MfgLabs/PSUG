package com.mfglabs
package models
package db

object StravaWritesQueries {
  import doobie.imports._
  import Atoms._

  def add(id: Strava.Activity.Id, act: Strava.Activity) = {
    val Strava.Activity(date, ath, dist) = act
    sql"""
      INSERT INTO strava.activities(id, athlete, distance, start_date)
      VALUES($id, $ath, $dist, $date)
    """.update
  }
}

trait StravaWrites {

  import com.mfglabs.precepte._, default._, Macros.callee
  import Database.WithTrans

  private val Q = StravaWritesQueries

  def add(id: Strava.Activity.Id, act: Strava.Activity) =
    WithTrans { mc =>
      mc.logger.info("Adding new activity", Macros.params(id, act))
      for {
        _ <- Q.add(id, act).run
      } yield id
    }
}