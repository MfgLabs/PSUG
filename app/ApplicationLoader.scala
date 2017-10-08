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

  override lazy val httpFilters = Seq(gzipFilter)

  val flyway = new org.flywaydb.play.PlayInitializer(configuration, environment, webCommands)

  val conf = Conf(configuration)

  val ctx = Contexts(actorSystem)
  val metrics = InfluxMetrics(conf, ctx)

  val database = Database(ctx, conf, metrics)

  val httpClient = org.http4s.client.blaze.defaultClient
  val strava = models.StravaComponent(conf, httpClient, metrics, ctx, database)

  val applicationController = new controllers.Application(metrics, conf, ctx, database, strava)
  val assets = new _root_.controllers.Assets(httpErrorHandler)

  val router = new Routes(httpErrorHandler, applicationController)
}

object DemoComponents {
  def default = new DemoComponents(ApplicationLoader.createContext(Environment.simple()))
}