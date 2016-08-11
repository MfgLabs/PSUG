package com.mfglabs
package models
package db

import org.scalatest._
import java.util.UUID

class DemoSpec extends WordSpec with Matchers {
  import Helpers._

  import doobie.imports._
  import scalaz.concurrent.Task
  def checker(conf: commons.Conf) =
    new QueryChecker {
      import conf.Database._
      def transactor = DriverManagerTransactor[Task](driver, url, user, password)
    }

  "Drafts SQL queries" should {
    "typecheck" in TestApp { amer =>
      val ch = checker(amer.conf)
      import ch._

      // TODO
    }
  }

}