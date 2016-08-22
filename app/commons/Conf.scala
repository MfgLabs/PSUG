package com.mfglabs
package commons

import scala.util.Try
import play.api._

case class Conf(configuration: Configuration) {

  import java.net.InetAddress

  val config = configuration.underlying

  private def getOpt(path: String): Option[String] =
    if (config.hasPath(path))
      Some(config.getString(path))
    else
      None

  val environment = commons.Env.withName(config.getString("env"))
  val port = Integer.parseInt(Option(System.getProperty("https.port")).orElse(Option(System.getProperty("http.port"))).getOrElse("9000"))
  val host = Try(InetAddress.getLocalHost().getHostName).toOption

  val prefix = config.getString("api.prefix")

  import scalaz.std.option._
  import scalaz.syntax.applicative._
  import java.net.URL

  case class InfluxConf private[Conf] (url: URL, user: String, password: String, dbName: String)

  val Influx = (getOpt("influx.url") |@| getOpt("influx.user") |@| getOpt("influx.password") |@| getOpt("influx.db")) { (url, user, pass, db) =>
    InfluxConf(new URL(url), user, pass, db)
  }

  object Database {
    val driver = config.getString("db.default.driver")
    val user = config.getString("db.default.username")
    val password = config.getString("db.default.password")
    val host = config.getString("db.default.host")
    val port = config.getInt("db.default.port")
    val name = config.getString("db.default.name")
    val url = config.getString("db.default.url")
  }

  object Strava {
    val clientId = models.Strava.ClientId(config.getString("strava.client.id"))
    val clientSecret = models.Strava.ClientSecret(config.getString("strava.client.secret"))
    val apiRateLimit = config.getLong("strava.api.max-calls-per-seconds")
    val apiActivitiesPerPage = config.getInt("strava.api.activities-per-page")
  }
}
