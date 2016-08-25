package com.mfglabs
package models
package db

import org.scalatest._

class StravaSpec extends WordSpec with Matchers {
  import Helpers._

  import doobie.imports._
  import scalaz.concurrent.Task
  def checker(conf: commons.Conf) =
    new QueryChecker {
      import conf.Database._
      def transactor = DriverManagerTransactor[Task](driver, url, user, password)
    }

  "Strava SQL queries" should {
    "typecheck" in TestApp { amer =>
      val ch = checker(amer.conf)
      import ch._

      import Strava.Activity, Activity._
      import java.time.Instant

      val id = Id(1)
      val act = Activity(Date(Instant.now), Athlete(123), Distance(10000f))

      check(StravaWritesQueries.add(id, act))
    }
  }

}