package com.mfglabs

import play.api._
import play.api.ApplicationLoader.Context
import router.Routes

class DemoApplicationLoader extends ApplicationLoader {
  def load(context: Context) = new DemoComponents(context).application
}

import play.api.cache.EhCacheComponents
import models.db.Database

class DemoComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with EhCacheComponents
    with play.filters.gzip.GzipFilterComponents {

  import commons._

  // See https://www.playframework.com/documentation/2.5.x/SettingsLogger
  LoggerConfigurator(context.environment.classLoader).foreach {
    _.configure(context.environment)
  }

  val flyway = new org.flywaydb.play.PlayInitializer(configuration, environment, webCommands)

  lazy val conf = Conf(configuration)
  lazy val database = Database(ctx, conf, metrics)

  lazy val ctx = Contexts(actorSystem)
  lazy val metrics = InfluxMetrics(conf, ctx)

  lazy val httpClient = org.http4s.client.blaze.defaultClient
  lazy val strava = models.StravaComponent(conf, httpClient, metrics, ctx, database)

  lazy val applicationController = new controllers.Application(metrics, conf, ctx, database, strava)
  lazy val assets = new _root_.controllers.Assets(httpErrorHandler)

  lazy val router = new Routes(httpErrorHandler, applicationController)
}

object DemoComponents {
  def default = new DemoComponents(ApplicationLoader.createContext(Environment.simple()))
}